package com.traveltrove.betraveltrove.presentation.hotel;

import com.traveltrove.betraveltrove.dataaccess.hotel.Hotel;
import com.traveltrove.betraveltrove.dataaccess.hotel.HotelRepository;
import com.traveltrove.betraveltrove.presentation.mockserverconfigs.MockServerConfigHotelService;
import org.junit.jupiter.api.*;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HotelControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private HotelRepository hotelRepository;

    private MockServerConfigHotelService mockServerConfigHotelService;

    private final String INVALID_HOTEL_ID = "invalid-hotel-id";

    private final Hotel hotel1 = Hotel.builder()
            .id("1")
            .hotelId(UUID.randomUUID().toString())
            .name("Hotel 1")
            .url("url1")
            .cityId("1")
            .build();

    private final Hotel hotel2 = Hotel.builder()
            .id("2")
            .hotelId(UUID.randomUUID().toString())
            .name("Hotel 2")
            .url("url2")
            .cityId("2")
            .build();

    @BeforeAll
    public void setUp() {
        mockServerConfigHotelService = new MockServerConfigHotelService();
        mockServerConfigHotelService.startMockServer();
        mockServerConfigHotelService.registerGetHotelByIdEndpoint(hotel1.getHotelId());
        mockServerConfigHotelService.registerGetHotelByIdEndpoint(hotel2.getHotelId());
        mockServerConfigHotelService.registerGetHotelByIdWithInvalidHotelIdEndpoint(INVALID_HOTEL_ID);
    }

    @AfterAll
    public void stopServer() {
        mockServerConfigHotelService.stopMockServer();
    }

    @BeforeEach
    public void setupDB() {
        Publisher<Hotel> setupDB = hotelRepository.deleteAll()
                .thenMany(Flux.just(hotel1, hotel2))
                .flatMap(hotelRepository::save);

        StepVerifier.create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void whenGetAllHotels_thenReturnsAllHotels() {
        webTestClient.get()
                .uri("/api/v1/hotels")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(Hotel.class)
                .hasSize(2)
                .value(hotels -> {
                    assertEquals(2, hotels.size());
                    assertEquals(hotel1.getName(), hotels.get(0).getName());
                    assertEquals(hotel2.getName(), hotels.get(1).getName());
                });

        StepVerifier.create(hotelRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void whenGetHotelByHotelId_thenReturnsHotel() {
        webTestClient.get()
                .uri("/api/v1/hotels/" + hotel1.getHotelId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(Hotel.class)
                .isEqualTo(hotel1);

        StepVerifier.create(hotelRepository.findById(hotel1.getId()))
                .expectNext(hotel1)
                .verifyComplete();
    }

    @Test
    void whenGetHotelByInvalidHotelId_thenReturnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/hotels/" + INVALID_HOTEL_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetHotelsByCityId_thenReturnsHotels() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/hotels")
                        .queryParam("cityId", hotel1.getCityId())
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(Hotel.class)
                .hasSize(1)
                .value(hotels -> {
                    assertEquals(1, hotels.size());
                    assertEquals(hotel1.getName(), hotels.get(0).getName());
                });

        StepVerifier.create(hotelRepository.findAllByCityId(hotel1.getCityId()))
                .expectNext(hotel1)
                .verifyComplete();
    }

    @Test
    void whenGetHotelsByInvalidCityId_thenReturnsEmpty() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/hotels")
                        .queryParam("cityId", "invalid-city-id")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(Hotel.class)
                .hasSize(0);

        StepVerifier.create(hotelRepository.findAllByCityId("invalid-city-id"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenAddHotel_thenReturnsCreated() {
        Hotel newHotel = Hotel.builder()
                .hotelId(UUID.randomUUID().toString())
                .name("New Hotel")
                .url("new-url")
                .cityId("3")
                .build();

        webTestClient.post()
                .uri("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newHotel)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(Hotel.class)
                .isEqualTo(newHotel);

        StepVerifier.create(hotelRepository.findById(newHotel.getId()))
                .expectNext(newHotel)
                .verifyComplete();
    }

    @Test
    void whenUpdateHotel_thenReturnsUpdatedHotel() {
        Hotel updatedHotel = Hotel.builder()
                .id(hotel1.getId())
                .hotelId(hotel1.getHotelId())
                .name("Updated Hotel")
                .url("updated-url")
                .cityId(hotel1.getCityId())
                .build();

        webTestClient.put()
                .uri("/api/v1/hotels/" + hotel1.getHotelId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedHotel)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(Hotel.class)
                .isEqualTo(updatedHotel);

        StepVerifier.create(hotelRepository.findById(hotel1.getId()))
                .expectNext(updatedHotel)
                .verifyComplete();
    }

    @Test
    void whenUpdateHotelWithInvalidHotelId_thenReturnsNotFound() {
        Hotel updatedHotel = Hotel.builder()
                .id(hotel1.getId())
                .hotelId(hotel1.getHotelId())
                .name("Updated Hotel")
                .url("updated-url")
                .cityId(hotel1.getCityId())
                .build();

        webTestClient.put()
                .uri("/api/v1/hotels/" + INVALID_HOTEL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedHotel)
                .exchange()
                .expectStatus().isNotFound();

        StepVerifier.create(hotelRepository.findById(hotel1.getId()))
                .expectNext(hotel1)
                .verifyComplete();
    }

    @Test
    void whenDeleteHotel_thenReturnsNoContent() {
        webTestClient.delete()
                .uri("/api/v1/hotels/" + hotel1.getHotelId())
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(hotelRepository.findById(hotel1.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenDeleteHotelWithInvalidHotelId_thenReturnsNotFound() {
        webTestClient.delete()
                .uri("/api/v1/hotels/" + INVALID_HOTEL_ID)
                .exchange()
                .expectStatus().isNotFound();

        StepVerifier.create(hotelRepository.findById(hotel1.getId()))
                .expectNext(hotel1)
                .verifyComplete();
    }

}
