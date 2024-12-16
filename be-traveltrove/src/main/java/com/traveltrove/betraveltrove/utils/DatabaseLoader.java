package com.traveltrove.betraveltrove.utils;

import com.traveltrove.betraveltrove.dataaccess.airport.Airport;
import com.traveltrove.betraveltrove.dataaccess.airport.AirportRepository;
import com.traveltrove.betraveltrove.dataaccess.city.City;
import com.traveltrove.betraveltrove.dataaccess.city.CityRepository;
import com.traveltrove.betraveltrove.dataaccess.country.Country;
import com.traveltrove.betraveltrove.dataaccess.country.CountryRepository;
import com.traveltrove.betraveltrove.dataaccess.events.Event;
import com.traveltrove.betraveltrove.dataaccess.events.EventRepository;
import com.traveltrove.betraveltrove.dataaccess.tour.Tour;
import com.traveltrove.betraveltrove.dataaccess.tour.TourEvent;
import com.traveltrove.betraveltrove.dataaccess.tour.TourEventRepository;
import com.traveltrove.betraveltrove.dataaccess.tour.TourRepository;
import com.traveltrove.betraveltrove.dataaccess.tourpackage.PackageRepository;
import com.traveltrove.betraveltrove.dataaccess.traveler.Traveler;
import com.traveltrove.betraveltrove.dataaccess.traveler.TravelerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseLoader {

    // Countries :)
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @PostConstruct
    public void loadCountriesAndCities() {
        List<Country> sampleCountries = List.of(
                new Country(null, "ad633b50-83d4-41f3-866a-26452bdd6f33", "United States", "usa.png"),
                new Country(null, "b1db23af-4f2d-4138-b623-e906f5287e90", "Canada", "canada.png"),
                new Country(null, "118495e2-e1ce-4987-bb09-a95f93515bda", "France", "france.png"),
                new Country(null, "cf09374e-6d51-47ec-91e1-2b69a788457f", "Germany", "germany.png"),
                new Country(null, "dde33653-fc58-457c-9d33-a322a2a82835", "Italy", "italy.png"),
                new Country(null, "f05f8f2c-f86d-4cf5-8a9b-19e70f1b96d5", "Japan", "japan.png"),
                new Country(null, "b511c7ee-c0f3-4902-9bfa-d50f59ef2b3c", "Brazil", "brazil.png"),
                new Country(null, "d5d4e5e2-f960-4cf4-beed-c38104e5d5b4", "Australia", "australia.png"),
                new Country(null, "3cd2ad86-26cc-42ad-8b20-8b0b6e6d2a2e", "India", "india.png"),
                new Country(null, "877ec1c0-ffab-449e-a2ec-08f95db58f55", "China", "china.png")
        );
        List<City> sampleCities = List.of(
                new City(null, "b713c09a-9c3e-4b30-872a-4d89089badd0", "New York", sampleCountries.get(0).getCountryId()),
                new City(null, "11a85d86-3fc8-4504-b1e9-25fd87eba3cf", "Toronto", sampleCountries.get(1).getCountryId()),
                new City(null, "f5ad630f-830d-47a8-a26c-76b87163a7e4", "Paris", sampleCountries.get(2).getCountryId()),
                new City(null, "000f3f3a-8ee2-4690-be4d-a5bd38a5f06f", "Berlin", sampleCountries.get(3).getCountryId()),
                new City(null, "7f15fafc-85f4-4ba5-822b-27b7ddce6c37", "Rome", sampleCountries.get(4).getCountryId()),
                new City(null, "92537e75-3fc2-42af-b105-b6150395acbb", "Tokyo", sampleCountries.get(5).getCountryId()),
                new City(null, "affc7cc1-b3d8-4146-bc39-6e9ff1e66704", "Rio de Janeiro", sampleCountries.get(6).getCountryId()),
                new City(null, "f978f76c-abfc-4b25-ba32-9e1b085b5ab0", "Sydney", sampleCountries.get(7).getCountryId()),
                new City(null, "0361c975-fe28-4817-be51-6864b8a2bf38", "Mumbai", sampleCountries.get(8).getCountryId()),
                new City(null, "2fce64d8-dfa6-4abe-9a84-af7aaac1293f", "Beijing", sampleCountries.get(9).getCountryId())
        );

        countryRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleCountries))
                .flatMap(countryRepository::save)
                .doOnNext(country -> log.info("Preloaded country: {}", country))
                .subscribe(
                        success -> log.info("Database preloaded successfully."),
                        error -> log.error("Error preloading database: {}", error.getMessage())
                );

        cityRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleCities))
                .flatMap(cityRepository::save)
                .doOnNext(city -> log.info("Preloaded city: {}", city))
                .subscribe(
                        success -> log.info("Database preloaded successfully."),
                        error -> log.error("Error preloading database: {}", error.getMessage())
                );
    }

    // Events :)
    private final EventRepository eventRepository;

    @PostConstruct
    public void loadEvents() {
        List<Event> sampleEvents = List.of(
                Event.builder()
                        .id(null)
                        .eventId("91c940b1-24e8-463f-96ef-f54f7e4aaf1d")
                        .cityId("b713c09a-9c3e-4b30-872a-4d89089badd0") // New York
                        .countryId("ad633b50-83d4-41f3-866a-26452bdd6f33") // United States
                        .name("New York Summer Carnival")
                        .image("ny_summer_carnival.png")
                        .description("Experience the thrill of summer with live music, street parades, and food festivals across New York City.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("87b3b478-031c-49d2-b64e-b54a49578d8c")
                        .cityId("11a85d86-3fc8-4504-b1e9-25fd87eba3cf") // Toronto
                        .countryId("b1db23af-4f2d-4138-b623-e906f5287e90") // Canada
                        .name("Toronto Winter Lights Festival")
                        .image("toronto_winter_lights.png")
                        .description("Enjoy dazzling light displays and magical winter-themed experiences throughout Toronto’s iconic landmarks.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("e48b9f26-fec7-4ce0-8f98-010ee0e6bd6c")
                        .cityId("f5ad630f-830d-47a8-a26c-76b87163a7e4") // Paris
                        .countryId("118495e2-e1ce-4987-bb09-a95f93515bda") // France
                        .name("Paris Fashion Week")
                        .image("paris_fashion_week.png")
                        .description("Discover the world of high fashion and luxury with runway shows, designer exhibitions, and celebrity sightings.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("7a92bd77-c6e4-4f27-9bbe-a5dad2761950")
                        .cityId("000f3f3a-8ee2-4690-be4d-a5bd38a5f06f") // Berlin
                        .countryId("cf09374e-6d51-47ec-91e1-2b69a788457f") // Germany
                        .name("Berlin Tech Conference")
                        .image("berlin_tech_conference.png")
                        .description("Explore groundbreaking innovations and network with industry leaders at Europe’s largest tech conference.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("44475ce1-6358-4d2f-a497-4ca3032c495c")
                        .cityId("7f15fafc-85f4-4ba5-822b-27b7ddce6c37") // Rome
                        .countryId("dde33653-fc58-457c-9d33-a322a2a82835") // Italy
                        .name("Rome Food & Wine Festival")
                        .image("rome_food_wine_festival.png")
                        .description("Indulge in authentic Italian cuisine, world-class wines, and cultural performances in the heart of Rome.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("3ea4a674-3463-4fa2-a3cf-d882485963b6")
                        .cityId("92537e75-3fc2-42af-b105-b6150395acbb") // Tokyo
                        .countryId("f05f8f2c-f86d-4cf5-8a9b-19e70f1b96d5") // Japan
                        .name("Tokyo Cherry Blossom Festival")
                        .image("tokyo_cherry_blossom.png")
                        .description("Celebrate the arrival of spring with breathtaking cherry blossoms, cultural exhibits, and traditional performances.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("528398a9-91a1-4833-bdd1-d88b732b9be6")
                        .cityId("affc7cc1-b3d8-4146-bc39-6e9ff1e66704") // Rio de Janeiro
                        .countryId("b511c7ee-c0f3-4902-9bfa-d50f59ef2b3c") // Brazil
                        .name("Rio Carnival")
                        .image("rio_carnival.png")
                        .description("Join the world's biggest street party with samba parades, dazzling costumes, and endless celebrations in Rio.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("72927c9c-750f-4b37-9799-16d3b7a59725")
                        .cityId("f978f76c-abfc-4b25-ba32-9e1b085b5ab0") // Sydney
                        .countryId("d5d4e5e2-f960-4cf4-beed-c38104e5d5b4") // Australia
                        .name("Sydney New Year’s Eve Fireworks")
                        .image("sydney_fireworks.png")
                        .description("Ring in the new year with one of the world's most spectacular fireworks displays over Sydney Harbour.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("82c07322-37da-4635-bcae-3ae241036cf8")
                        .cityId("0361c975-fe28-4817-be51-6864b8a2bf38") // Mumbai
                        .countryId("3cd2ad86-26cc-42ad-8b20-8b0b6e6d2a2e") // India
                        .name("Mumbai Film Festival")
                        .image("mumbai_film_festival.png")
                        .description("Celebrate the art of cinema with film screenings, celebrity interviews, and cultural showcases in Mumbai.")
                        .build(),
                Event.builder()
                        .id(null)
                        .eventId("4e5bf569-27cb-494b-b7e5-9f9bf0ca66fe")
                        .cityId("2fce64d8-dfa6-4abe-9a84-af7aaac1293f") // Beijing
                        .countryId("877ec1c0-ffab-449e-a2ec-08f95db58f55") // China
                        .name("Beijing Dragon Boat Festival")
                        .image("beijing_dragon_boat.png")
                        .description("Experience the thrilling dragon boat races and traditional festivities along Beijing’s scenic waterways.")
                        .build()
        );

        eventRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleEvents))
                .flatMap(eventRepository::save)
                .doOnNext(event -> log.info("Preloaded event: {}", event))
                .subscribe(
                        success -> log.info("Events preloaded successfully."),
                        error -> log.error("Error preloading events: {}", error.getMessage())
                );
    }

    // Tours
    private final TourRepository tourRepository;

    @PostConstruct
    public void loadTours() {
        List<Tour> sampleTours = List.of(
                Tour.builder()
                        .id(null)
                        .tourId("7f54a45d-8c1d-432f-a5c8-1f93b89bfe29")
                        .name("Grand American Adventure")
                        .description("Explore the vast landscapes of the United States, from the Grand Canyon to the streets of New York City.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("90af5e42-b8a7-4b59-939f-8fa2c4e384d1")
                        .name("Canadian Rockies Expedition")
                        .description("Experience Canada’s majestic Rockies with breathtaking views, wildlife safaris, and serene mountain resorts.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("f1f4ac8e-4e24-4bb3-97d3-e5b7b5b7f912")
                        .name("Parisian Art & Culture Tour")
                        .description("Discover France’s iconic landmarks, including the Eiffel Tower, the Louvre Museum, and charming Parisian streets.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("83e6f7ba-0567-426a-b43f-456f947c576b")
                        .name("German Castle Escape")
                        .description("Journey through Germany’s historic castles, scenic villages, and stunning landscapes along the Romantic Road.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("d12a7bf7-dc94-4953-9189-68f6a70f7844")
                        .name("Italian Culinary Retreat")
                        .description("Savor Italy’s finest cuisine with wine tastings in Tuscany, pasta-making workshops, and cultural explorations in Rome.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("7e14cb83-3e2a-41c6-b26f-09ebd62a5e39")
                        .name("Japan Cherry Blossom Tour")
                        .description("Witness the stunning beauty of Japan’s cherry blossoms while visiting Kyoto's temples, Mt. Fuji, and Tokyo’s bustling streets.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("42cf8561-6f83-4f2f-becb-e3c72a1bb572")
                        .name("Brazilian Amazon Adventure")
                        .description("Embark on an unforgettable journey into the Amazon rainforest with guided eco-tours and immersive wildlife experiences.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("f4a9c2ef-8473-4b67-b527-44fcb3a6eec5")
                        .name("Australian Outback Expedition")
                        .description("Discover Australia’s rugged outback, from the stunning Uluru rock formation to unique wildlife encounters.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("7b82cb14-2ff5-4d8f-b84c-3bfb7b6cda1e")
                        .name("Indian Heritage Journey")
                        .description("Explore India’s cultural treasures, including the Taj Mahal, Jaipur’s palaces, and spiritual experiences along the Ganges.")
                        .build(),
                Tour.builder()
                        .id(null)
                        .tourId("6a237fda-4924-4c73-a6df-73c1e0c37af2")
                        .name("Chinese Silk Road Adventure")
                        .description("Travel along China’s historic Silk Road, visiting ancient cities, the Great Wall, and vibrant cultural heritage sites.")
                        .build()
        );

        tourRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleTours))
                .flatMap(tourRepository::save)
                .doOnNext(tour -> log.info("Preloaded tour: {}", tour))
                .subscribe(
                        success -> log.info("Tours preloaded successfully."),
                        error -> log.error("Error preloading tours: {}", error.getMessage())
                );
    }

    private final TourEventRepository tourEventRepository;

    @PostConstruct
    public void loadTourEvents() {
        List<TourEvent> sampleTourEvents = List.of(
                // Grand American Adventure
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("06f7bca2-4cf2-4d4f-a6b4-493c90f7b3da")
                        .seq(1)
                        .seqDesc("Start at New York Summer Carnival")
                        .tourId("7f54a45d-8c1d-432f-a5c8-1f93b89bfe29")
                        .eventId("91c940b1-24e8-463f-96ef-f54f7e4aaf1d")
                        .build(),

                // Canadian Rockies Expedition
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("9d2b17f6-4a3c-46c2-b9f1-8d5e3a1d2c68")
                        .seq(1)
                        .seqDesc("Explore Toronto's Winter Lights Festival")
                        .tourId("90af5e42-b8a7-4b59-939f-8fa2c4e384d1")
                        .eventId("87b3b478-031c-49d2-b64e-b54a49578d8c")
                        .build(),

                // Parisian Art & Culture Tour
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("e48a5f2e-fc72-4ce0-8f98-010ee0e6bd6c")
                        .seq(1)
                        .seqDesc("Visit Paris Fashion Week")
                        .tourId("f1f4ac8e-4e24-4bb3-97d3-e5b7b5b7f912")
                        .eventId("e48b9f26-fec7-4ce0-8f98-010ee0e6bd6c")
                        .build(),

                // German Castle Escape
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("7a92bd77-c6e4-4f27-9bbe-a5dad2761950")
                        .seq(1)
                        .seqDesc("Attend Berlin Tech Conference")
                        .tourId("83e6f7ba-0567-426a-b43f-456f947c576b")
                        .eventId("7a92bd77-c6e4-4f27-9bbe-a5dad2761950")
                        .build(),

                // Italian Culinary Retreat
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("d12a7bf7-dc94-4953-9189-68f6a70f7844")
                        .seq(1)
                        .seqDesc("Enjoy Rome Food & Wine Festival")
                        .tourId("d12a7bf7-dc94-4953-9189-68f6a70f7844")
                        .eventId("44475ce1-6358-4d2f-a497-4ca3032c495c")
                        .build(),

                // Japan Cherry Blossom Tour
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("7e14cb83-3e2a-41c6-b26f-09ebd62a5e39")
                        .seq(1)
                        .seqDesc("Join Tokyo Cherry Blossom Festival")
                        .tourId("7e14cb83-3e2a-41c6-b26f-09ebd62a5e39")
                        .eventId("3ea4a674-3463-4fa2-a3cf-d882485963b6")
                        .build(),

                // Brazilian Amazon Adventure
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("42cf8561-6f83-4f2f-becb-e3c72a1bb572")
                        .seq(1)
                        .seqDesc("Experience Rio Carnival")
                        .tourId("42cf8561-6f83-4f2f-becb-e3c72a1bb572")
                        .eventId("528398a9-91a1-4833-bdd1-d88b732b9be6")
                        .build(),

                // Australian Outback Expedition
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("f4a9c2ef-8473-4b67-b527-44fcb3a6eec5")
                        .seq(1)
                        .seqDesc("Enjoy Sydney Fireworks")
                        .tourId("f4a9c2ef-8473-4b67-b527-44fcb3a6eec5")
                        .eventId("72927c9c-750f-4b37-9799-16d3b7a59725")
                        .build(),

                // Indian Heritage Journey
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("7b82cb14-2ff5-4d8f-b84c-3bfb7b6cda1e")
                        .seq(1)
                        .seqDesc("Attend Mumbai Film Festival")
                        .tourId("7b82cb14-2ff5-4d8f-b84c-3bfb7b6cda1e")
                        .eventId("82c07322-37da-4635-bcae-3ae241036cf8")
                        .build(),

                // Chinese Silk Road Adventure
                TourEvent.builder()
                        .Id(null)
                        .tourEventId("6a237fda-4924-4c73-a6df-73c1e0c37af2")
                        .seq(1)
                        .seqDesc("Experience Beijing Dragon Boat Festival")
                        .tourId("6a237fda-4924-4c73-a6df-73c1e0c37af2")
                        .eventId("4e5bf569-27cb-494b-b7e5-9f9bf0ca66fe")
                        .build()
        );

        tourEventRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleTourEvents))
                .flatMap(tourEventRepository::save)
                .doOnNext(event -> log.info("Preloaded tour event: {}", event))
                .subscribe(
                        success -> log.info("Tour events preloaded successfully."),
                        error -> log.error("Error preloading tour events: {}", error.getMessage())
                );

    }


    private final AirportRepository airportRepository;
    @PostConstruct
    public void loadAirports() {
        List<Airport> sampleAirports = List.of(
                Airport.builder()
                        .id(null)
                        .airportId("d1e91f6c-723e-43b9-812f-2f3d3bfb4081")
                        .name("John F. Kennedy International Airport")
                        .cityId("b713c09a-9c3e-4b30-872a-4d89089badd0")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("f7a7e8e4-cc49-4027-8c4d-881e6179e6d2")
                        .name("Toronto Pearson International Airport")
                        .cityId("11a85d86-3fc8-4504-b1e9-25fd87eba3cf")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("b2c4f9ac-0fd6-4327-8e3a-f3c68bd3d62a")
                        .name("Charles de Gaulle Airport")
                        .cityId("f5ad630f-830d-47a8-a26c-76b87163a7e4")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("6e3b9f27-7cbe-47aa-b6e9-34f2cd5b5171")
                        .name("Berlin Brandenburg Airport")
                        .cityId("000f3f3a-8ee2-4690-be4d-a5bd38a5f06f")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("9273ecac-b84d-41e9-9d5f-28f0bd1e467b")
                        .name("Leonardo da Vinci–Fiumicino Airport")
                        .cityId("7f15fafc-85f4-4ba5-822b-27b7ddce6c37")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("e8f314c7-716b-4f19-a1d6-fc376b8c81ad")
                        .name("Narita International Airport")
                        .cityId("92537e75-3fc2-42af-b105-b6150395acbb")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("3f2e8bbd-84c3-4d3e-bc24-f173acd01be4")
                        .name("Rio de Janeiro–Galeão International Airport")
                        .cityId("affc7cc1-b3d8-4146-bc39-6e9ff1e66704")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("aa12fc4b-619e-4d8e-8563-5a09bc6f1ae1")
                        .name("Sydney Kingsford Smith Airport")
                        .cityId("f978f76c-abfc-4b25-ba32-9e1b085b5ab0")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("47c8f2e7-3d6b-402c-93b7-8a92ef43e6ab")
                        .name("Chhatrapati Shivaji Maharaj International Airport")
                        .cityId("0361c975-fe28-4817-be51-6864b8a2bf38")
                        .build(),
                Airport.builder()
                        .id(null)
                        .airportId("ea1f7a4e-2db7-4812-9e8f-dc4b5a1e7634")
                        .name("Beijing Capital International Airport")
                        .cityId("2fce64d8-dfa6-4abe-9a84-af7aaac1293f")
                        .build()
        );

        airportRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleAirports))
                .flatMap(airportRepository::save)
                .doOnNext(airport -> log.info("Preloaded airport: {}"))
                .subscribe(
                        success -> log.info("Airports preloaded successfully."),
                        error -> log.error("Error preloading airports: {}", error.getMessage())
                );
    }

    private final TravelerRepository travelerRepository;


    @PostConstruct
    public void loadTravelers() {
        List<Traveler> sampleTravelers = List.of(
                Traveler.builder()
                        .id(null)
                        .travelerId("d1e91f6c-723e-43b9-812f-2f3d3bfb4081")
                        .firstName("Adem")
                        .lastName("Bessam")
                        .email("adembessam@gmail.com")
                        .addressLine1("123 Main St")
                        .addressLine2("321 Main st")
                        .city("New York")
                        .state("NY")
                        .countryId("ad633b50-83d4-41f3-866a-26452bdd6f33")
                        .seq(1)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("b2e91f6c-723e-43b9-812f-2f3d3bfb4082")
                        .firstName("John")
                        .lastName("Doe")
                        .email("johndoe@gmail.com")
                        .addressLine1("456 Elm St")
                        .addressLine2("654 Elm St")
                        .city("Toronto")
                        .state("ON")
                        .countryId("b1db23af-4f2d-4138-b623-e906f5287e90")
                        .seq(2)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("c3e91f6c-723e-43b9-812f-2f3d3bfb4083")
                        .firstName("Jane")
                        .lastName("Smith")
                        .email("janesmith@gmail.com")
                        .addressLine1("789 Oak St")
                        .addressLine2("987 Oak St")
                        .city("Paris")
                        .state("IDF")
                        .countryId("118495e2-e1ce-4987-bb09-a95f93515bda")
                        .seq(3)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("d4e91f6c-723e-43b9-812f-2f3d3bfb4084")
                        .firstName("Hans")
                        .lastName("Müller")
                        .email("hansmuller@gmail.com")
                        .addressLine1("101 Pine St")
                        .addressLine2("201 Pine St")
                        .city("Berlin")
                        .state("BE")
                        .countryId("cf09374e-6d51-47ec-91e1-2b69a788457f")
                        .seq(4)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("e5e91f6c-723e-43b9-812f-2f3d3bfb4085")
                        .firstName("Maria")
                        .lastName("Rossi")
                        .email("mariarossi@gmail.com")
                        .addressLine1("202 Maple St")
                        .addressLine2("303 Maple St")
                        .city("Rome")
                        .state("RM")
                        .countryId("dde33653-fc58-457c-9d33-a322a2a82835")
                        .seq(5)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("f6e91f6c-723e-43b9-812f-2f3d3bfb4086")
                        .firstName("Yuki")
                        .lastName("Tanaka")
                        .email("yukitanaka@gmail.com")
                        .addressLine1("303 Cedar St")
                        .addressLine2("404 Cedar St")
                        .city("Tokyo")
                        .state("TK")
                        .countryId("f05f8f2c-f86d-4cf5-8a9b-19e70f1b96d5")
                        .seq(6)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("g7e91f6c-723e-43b9-812f-2f3d3bfb4087")
                        .firstName("Carlos")
                        .lastName("Silva")
                        .email("carlossilva@gmail.com")
                        .addressLine1("404 Birch St")
                        .addressLine2("505 Birch St")
                        .city("Rio de Janeiro")
                        .state("RJ")
                        .countryId("b511c7ee-c0f3-4902-9bfa-d50f59ef2b3c")
                        .seq(7)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("h8e91f6c-723e-43b9-812f-2f3d3bfb4088")
                        .firstName("Liam")
                        .lastName("Brown")
                        .email("liambrown@gmail.com")
                        .addressLine1("505 Spruce St")
                        .addressLine2("606 Spruce St")
                        .city("Sydney")
                        .state("NSW")
                        .countryId("d5d4e5e2-f960-4cf4-beed-c38104e5d5b4")
                        .seq(8)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("i9e91f6c-723e-43b9-812f-2f3d3bfb4089")
                        .firstName("Amit")
                        .lastName("Sharma")
                        .email("amitsharma@gmail.com")
                        .addressLine1("606 Willow St")
                        .addressLine2("707 Willow St")
                        .city("Mumbai")
                        .state("MH")
                        .countryId("3cd2ad86-26cc-42ad-8b20-8b0b6e6d2a2e")
                        .seq(9)
                        .build(),
                Traveler.builder()
                        .id(null)
                        .travelerId("j0e91f6c-723e-43b9-812f-2f3d3bfb4090")
                        .firstName("Wei")
                        .lastName("Li")
                        .email("weili@gmail.com")
                        .addressLine1("707 Ash St")
                        .addressLine2("808 Ash St")
                        .city("Beijing")
                        .state("BJ")
                        .countryId("877ec1c0-ffab-449e-a2ec-08f95db58f55")
                        .seq(10)
                        .build()
        );

        travelerRepository.deleteAll()
                .thenMany(Flux.fromIterable(sampleTravelers))
                .flatMap(travelerRepository::save)
                .doOnNext(traveler -> log.info("Preloaded traveler: {}"))
                .subscribe(
                        success -> log.info("Travelers preloaded successfully."),
                        error -> log.error("Error preloading travelers: {}", error.getMessage())
                );
    }

    // now for packages

    private final PackageRepository packageRepository;

    @PostConstruct
    public void loadPackages() {
        List<com.traveltrove.betraveltrove.dataaccess.tourpackage.Package> samplePackages = List.of(
                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("4f3a6bde-bc68-4b1e-835a-1e5aaf7b752d")
                        .name("New York Adventure Package")
                        .description("Experience the thrill of New York City with a complete package including flights and guided tours.")
                        .startDate(LocalDate.of(2024, 5, 15))
                        .endDate(LocalDate.of(2024, 5, 22))
                        .airportId("d1e91f6c-723e-43b9-812f-2f3d3bfb4081")
                        .tourId("7f54a45d-8c1d-432f-a5c8-1f93b89bfe29")
                        .priceSingle(1800.0)
                        .priceDouble(1600.0)
                        .priceTriple(1400.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("8e7a6dbc-cd45-44d2-9bfb-3419a6b4fa45")
                        .name("Toronto Winter Lights Escape")
                        .description("Explore the magic of Toronto’s Winter Lights Festival with this all-inclusive package.")
                        .startDate(LocalDate.of(2024, 1, 10))
                        .endDate(LocalDate.of(2024, 1, 17))
                        .airportId("f7a7e8e4-cc49-4027-8c4d-881e6179e6d2")
                        .tourId("90af5e42-b8a7-4b59-939f-8fa2c4e384d1")
                        .priceSingle(1500.0)
                        .priceDouble(1300.0)
                        .priceTriple(1100.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("2d1b4a3f-4e7f-42a7-a6fb-b4a1e7d9cf14")
                        .name("Parisian Fashion Week Package")
                        .description("Indulge in Paris Fashion Week with flights, accommodations, and VIP access.")
                        .startDate(LocalDate.of(2024, 3, 20))
                        .endDate(LocalDate.of(2024, 3, 27))
                        .airportId("b2c4f9ac-0fd6-4327-8e3a-f3c68bd3d62a")
                        .tourId("f1f4ac8e-4e24-4bb3-97d3-e5b7b5b7f912")
                        .priceSingle(2200.0)
                        .priceDouble(2000.0)
                        .priceTriple(1800.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("5c3b6d4e-9f42-4d1e-8ba4-df23cb19d8f4")
                        .name("Berlin Tech Conference Package")
                        .description("Join Europe’s largest tech conference with this exclusive package.")
                        .startDate(LocalDate.of(2024, 6, 5))
                        .endDate(LocalDate.of(2024, 6, 12))
                        .airportId("6e3b9f27-7cbe-47aa-b6e9-34f2cd5b5171")
                        .tourId("83e6f7ba-0567-426a-b43f-456f947c576b")
                        .priceSingle(1800.0)
                        .priceDouble(1600.0)
                        .priceTriple(1400.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("3e1a7d9b-5c4f-4a3b-a7f6-cbd4f8e2af16")
                        .name("Rome Culinary Experience")
                        .description("Savor authentic Italian cuisine and wines in this culinary retreat.")
                        .startDate(LocalDate.of(2024, 9, 15))
                        .endDate(LocalDate.of(2024, 9, 22))
                        .airportId("9273ecac-b84d-41e9-9d5f-28f0bd1e467b")
                        .tourId("d12a7bf7-dc94-4953-9189-68f6a70f7844")
                        .priceSingle(1900.0)
                        .priceDouble(1700.0)
                        .priceTriple(1500.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("9d3f8b7e-a6c1-4f42-b6d8-7a9b4e2d5cf3")
                        .name("Tokyo Blossom Festival Package")
                        .description("Experience Japan's iconic cherry blossoms and vibrant culture.")
                        .startDate(LocalDate.of(2024, 4, 1))
                        .endDate(LocalDate.of(2024, 4, 8))
                        .airportId("e8f314c7-716b-4f19-a1d6-fc376b8c81ad")
                        .tourId("7e14cb83-3e2a-41c6-b26f-09ebd62a5e39")
                        .priceSingle(2400.0)
                        .priceDouble(2200.0)
                        .priceTriple(2000.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("8f2d7e6b-b3c4-4f9e-b72d-f9a3c8e1af25")
                        .name("Rio Carnival Adventure")
                        .description("Join the world’s biggest carnival in Rio with this unforgettable package.")
                        .startDate(LocalDate.of(2024, 2, 10))
                        .endDate(LocalDate.of(2024, 2, 17))
                        .airportId("3f2e8bbd-84c3-4d3e-bc24-f173acd01be4")
                        .tourId("42cf8561-6f83-4f2f-becb-e3c72a1bb572")
                        .priceSingle(2000.0)
                        .priceDouble(1800.0)
                        .priceTriple(1600.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("7c5f9a6b-d8e4-4f9e-a6b7-3f2c1b5e9a25")
                        .name("Australian Outback Safari")
                        .description("Discover Australia’s rugged wilderness with this unique outback package.")
                        .startDate(LocalDate.of(2024, 7, 20))
                        .endDate(LocalDate.of(2024, 7, 27))
                        .airportId("aa12fc4b-619e-4d8e-8563-5a09bc6f1ae1")
                        .tourId("f4a9c2ef-8473-4b67-b527-44fcb3a6eec5")
                        .priceSingle(2100.0)
                        .priceDouble(1900.0)
                        .priceTriple(1700.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("9b5e2c1a-6f7d-4b9e-b8a7-d3f2b1c9a4e5")
                        .name("Indian Heritage Exploration")
                        .description("Explore India’s vibrant heritage and landmarks with this cultural package.")
                        .startDate(LocalDate.of(2024, 8, 1))
                        .endDate(LocalDate.of(2024, 8, 10))
                        .airportId("47c8f2e7-3d6b-402c-93b7-8a92ef43e6ab")
                        .tourId("7b82cb14-2ff5-4d8f-b84c-3bfb7b6cda1e")
                        .priceSingle(1700.0)
                        .priceDouble(1500.0)
                        .priceTriple(1300.0)
                        .build(),

                com.traveltrove.betraveltrove.dataaccess.tourpackage.Package.builder()
                        .id(null)
                        .packageId("6a4b3c1f-d7e9-4a8f-b7e2-c5a3b1d9f7c6")
                        .name("Silk Road Adventure")
                        .description("Experience the historic wonders of the Silk Road with guided tours and more.")
                        .startDate(LocalDate.of(2024, 10, 5))
                        .endDate(LocalDate.of(2024, 10, 15))
                        .airportId("ea1f7a4e-2db7-4812-9e8f-dc4b5a1e7634")
                        .tourId("6a237fda-4924-4c73-a6df-73c1e0c37af2")
                        .priceSingle(2200.0)
                        .priceDouble(2000.0)
                        .priceTriple(1800.0)
                        .build()
        );

        packageRepository.deleteAll()
                .thenMany(Flux.fromIterable(samplePackages))
                .flatMap(packageRepository::save)
                .doOnNext(pkg -> log.info("Preloaded package: {}"))
                .subscribe(
                        success -> log.info("Packages preloaded successfully."),
                        error -> log.error("Error preloading packages: {}", error.getMessage())
                );

    }
}

