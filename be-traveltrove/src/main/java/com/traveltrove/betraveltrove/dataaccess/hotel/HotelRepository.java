package com.traveltrove.betraveltrove.dataaccess.hotel;

import com.traveltrove.betraveltrove.dataaccess.hotel.Hotel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface HotelRepository extends ReactiveMongoRepository<Hotel, String> {
    public Mono<Hotel> findHotelByHotelId(String hotelId);
    public Flux<Hotel> findAllByCityId(String cityId);
}
