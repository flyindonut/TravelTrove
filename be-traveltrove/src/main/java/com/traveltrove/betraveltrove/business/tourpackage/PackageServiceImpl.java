package com.traveltrove.betraveltrove.business.tourpackage;

import com.traveltrove.betraveltrove.business.airport.AirportService;
import com.traveltrove.betraveltrove.business.tour.TourService;
import com.traveltrove.betraveltrove.dataaccess.airport.Airport;
import com.traveltrove.betraveltrove.dataaccess.tourpackage.Package;
import com.traveltrove.betraveltrove.dataaccess.tourpackage.PackageRepository;
import com.traveltrove.betraveltrove.dataaccess.tourpackage.PackageStatus;
import com.traveltrove.betraveltrove.presentation.airport.AirportResponseModel;
import com.traveltrove.betraveltrove.presentation.tour.TourEventResponseModel;
import com.traveltrove.betraveltrove.presentation.tour.TourResponseModel;
import com.traveltrove.betraveltrove.presentation.tourpackage.PackageRequestModel;
import com.traveltrove.betraveltrove.presentation.tourpackage.PackageResponseModel;
import com.traveltrove.betraveltrove.utils.entitymodels.PackageEntityModelUtil;
import com.traveltrove.betraveltrove.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;

    private final TourService tourService;

    private final AirportService airportService;

    public PackageServiceImpl(PackageRepository packageRepository, TourService tourService, AirportService airportService) {
        this.packageRepository = packageRepository;
        this.tourService = tourService;
        this.airportService = airportService;
    }

    @Override
    public Flux<PackageResponseModel> getAllPackages(String tourId) {
        if (tourId == null) {
            log.info("No tourId provided, fetching all packages.");
            return packageRepository.findAll()
                    .map(PackageEntityModelUtil::toPackageResponseModel);
        }
        return getTour(tourId)
                .flatMapMany(tour -> {
                    log.info("Fetching packages for tourId={}", tourId);
                    return packageRepository.findPackagesByTourId(tourId)
                            .doOnComplete(() -> log.info("Finished fetching packages for tourId={}", tourId))
                            .doOnError(error -> log.error("Error fetching packages: {}", error.getMessage()))
                            .map(PackageEntityModelUtil::toPackageResponseModel);
                });
    }


    @Override
    public Mono<PackageResponseModel> getPackageByPackageId(String packageId) {
        return packageRepository.findPackageByPackageId(packageId)
                .map(PackageEntityModelUtil::toPackageResponseModel)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)));
    }

    @Override
    public Mono<PackageResponseModel> createPackage(Mono<PackageRequestModel> packageRequestModel) {
        return packageRequestModel
                .flatMap(requestModel -> Mono.zip(getTour(requestModel.getTourId()), getAirport(requestModel.getAirportId()))
                        .doOnNext(tuple -> validatePackageRequestModel(requestModel))
                        .flatMap(tuple -> {
                            Package pk = PackageEntityModelUtil.toPackage(requestModel, tuple.getT1(), tuple.getT2());
                            pk.setAvailableSeats(pk.getTotalSeats());
                            pk.setPackageStatus(PackageStatus.OPEN);
                            return packageRepository.save(pk)
                                    .map(PackageEntityModelUtil::toPackageResponseModel);
                        }));
    }

    @Override
    public Mono<PackageResponseModel> updatePackage(String packageId, Mono<PackageRequestModel> packageRequestModel) {
        return packageRepository.findPackageByPackageId(packageId)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                .flatMap(existingPackage -> packageRequestModel
                        .flatMap(requestModel -> {
                            validatePackageRequestModel(requestModel);
                            return Mono.zip(getTour(requestModel.getTourId()), getAirport(requestModel.getAirportId()))
                                    .flatMap(tuple -> {
                                        Package updatedPackage = PackageEntityModelUtil.toPackage(requestModel, tuple.getT1(), tuple.getT2());updatedPackage.setId(existingPackage.getId()); // Retain the DB ID
                                        updatedPackage.setPackageId(existingPackage.getPackageId()); // Retain the original packageId
                                        updatedPackage.setAvailableSeats(existingPackage.getAvailableSeats()); // Retain the available seats
                                        if (updatedPackage.getAvailableSeats() > existingPackage.getTotalSeats()) {
                                            return Mono.error(new IllegalArgumentException("Available seats cannot exceed total seats"));
                                        }
                                        return packageRepository.save(updatedPackage)
                                                .map(PackageEntityModelUtil::toPackageResponseModel);
                                    })
                                    .then(refreshPackageStatus(packageId));
                        }));
    }


    @Override
    public Mono<PackageResponseModel> deletePackage(String packageId) {
        return packageRepository.findPackageByPackageId(packageId)
                // get the package using the packageId and return a notfound exception if the package is not found
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                // then delete the package entity model
                .flatMap(pk -> packageRepository.delete(pk)
                        // then return the package response model using the package entity model we just deleted
                        .thenReturn(PackageEntityModelUtil.toPackageResponseModel(pk)));
    }

    @Override
    public Mono<PackageResponseModel> decreaseAvailableSeats(String packageId, Integer quantity) {
        return packageRepository.findPackageByPackageId(packageId)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                .flatMap(pk -> {
                    if (pk.getAvailableSeats() < quantity) {
                        return Mono.error(new IllegalArgumentException("Not enough available seats to decrease by " + quantity));
                    }
                    pk.setAvailableSeats(pk.getAvailableSeats() - quantity);
                    return packageRepository.save(pk)
                            .map(PackageEntityModelUtil::toPackageResponseModel);
                })
                .then(refreshPackageStatus(packageId));
    }

    @Override
    public Mono<PackageResponseModel> increaseAvailableSeats(String packageId, Integer quantity) {
        return packageRepository.findPackageByPackageId(packageId)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                .flatMap(pk -> {
                    if (pk.getAvailableSeats() + quantity > pk.getTotalSeats()) {
                        return Mono.error(new IllegalArgumentException("Not enough total seats to increase by " + quantity));
                    }
                    pk.setAvailableSeats(pk.getAvailableSeats() + quantity);
                    return packageRepository.save(pk)
                            .map(PackageEntityModelUtil::toPackageResponseModel);
                })
                .then(refreshPackageStatus(packageId));
    }

    @Override
    public Mono<PackageResponseModel> refreshPackageStatus(String packageId) {
        return packageRepository.findPackageByPackageId(packageId)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                .flatMap(pk -> {
                    if (pk.getEndDate().isBefore(java.time.LocalDate.now())) {  // if the package end date is before today, the status is automatically expired
                        pk.setPackageStatus(PackageStatus.EXPIRED);
                    } else if (pk.getPackageStatus() == PackageStatus.CLOSED) { // if the package status is closed, it remains closed
                        pk.setPackageStatus(PackageStatus.CLOSED);
                    } else if (pk.getAvailableSeats() == 0) {   // if the available seats is 0, the status is full
                        pk.setPackageStatus(PackageStatus.FULL);
                    } else if (pk.getAvailableSeats() < pk.getTotalSeats() * 0.1) { // if the available seats is less than 10% of the total seats, the status is near capacity
                        pk.setPackageStatus(PackageStatus.NEAR_CAPACITY);
                    } else if (pk.getAvailableSeats() < 0) { // if the available seats is less than 0, the status is closed and a notification is sent to the admin
                        pk.setPackageStatus(PackageStatus.CLOSED);
                        // send notification to admin
                    } else {
                        pk.setPackageStatus(PackageStatus.OPEN);
                    }
                    return packageRepository.save(pk)
                            .map(PackageEntityModelUtil::toPackageResponseModel);
                });
    }

    @Override
    public Mono<PackageResponseModel> updatePackageStatus(String packageId, PackageStatus packageStatus) {
        return packageRepository.findPackageByPackageId(packageId)
                .switchIfEmpty(Mono.error(new NotFoundException("Package not found: " + packageId)))
                .flatMap(pk -> {
                    pk.setPackageStatus(packageStatus);
                    return packageRepository.save(pk)
                            .map(PackageEntityModelUtil::toPackageResponseModel);
                });
    }

    // a method to get the tour using the tourService and the tourId and return a notfound exception if the tour is not found
    private Mono<String> getTour(String tourId) {
        return tourService.getTourByTourId(tourId)
                .doOnNext(tour -> log.info("Found tour: {}", tour))
                .doOnError(error -> log.error("Error fetching tour: {}", error.getMessage()))
                .switchIfEmpty(Mono.error(new NotFoundException("Tour not found: " + tourId)))
                .map(TourResponseModel::getTourId);
    }

    private Mono<String> getAirport(String airportId) {
        return Mono.justOrEmpty(airportId) // Guard against null `airportId`
                .flatMap(airportService::getAirportById)
                .switchIfEmpty(Mono.error(new NotFoundException("Airport not found: " + airportId)))
                .map(AirportResponseModel::getAirportId);
    }


    // a method to check that the package request model is valid
    private void validatePackageRequestModel(PackageRequestModel packageRequestModel) {
        // check that the packageRequestModel is not null
        if (packageRequestModel == null) {
            throw new IllegalArgumentException("Package request model is required");
        }
        // check that the packageRequestModel name is not null
        if (packageRequestModel.getName() == null) {
            throw new IllegalArgumentException("Package name is required");
        }
        // check that the packageRequestModel description is not null
        if (packageRequestModel.getDescription() == null) {
            throw new IllegalArgumentException("Package description is required");
        }
        // check that the packageRequestModel startDate is not null
        if (packageRequestModel.getStartDate() == null) {
            throw new IllegalArgumentException("Package start date is required");
        }
        // check that the packageRequestModel endDate is not null
        if (packageRequestModel.getEndDate() == null) {
            throw new IllegalArgumentException("Package end date is required");
        }
        // check that the packageRequestModel priceSingle is not null
        if (packageRequestModel.getPriceSingle() == null) {
            throw new IllegalArgumentException("Package price single is required");
        }
        // check that the packageRequestModel totalSeats is not null
        if (packageRequestModel.getTotalSeats() == null) {
            throw new IllegalArgumentException("Package total seats is required");
        }
    }
}
