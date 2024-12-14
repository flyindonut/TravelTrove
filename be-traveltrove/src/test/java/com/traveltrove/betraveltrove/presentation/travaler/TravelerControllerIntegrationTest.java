package com.traveltrove.betraveltrove.presentation.travaler;

import com.traveltrove.betraveltrove.dataaccess.traveler.Traveler;
import com.traveltrove.betraveltrove.dataaccess.traveler.TravelerRepository;
import com.traveltrove.betraveltrove.presentation.mockserverconfigs.MockServerConfigTravelerService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TravelerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TravelerRepository travelerRepository;

    private MockServerConfigTravelerService mockServerConfigTravelerService;

    private final String INVALID_TRAVELER_ID = "invalid-traveler-id";

    private final Traveler traveler1 = Traveler.builder()
            .id("1")
            .travelerId(UUID.randomUUID().toString())
            .seq(1)
            .firstName("John")
            .lastName("Doe")
            .addressLine1("123 Street")
            .addressLine2("Apt 1")
            .city("Cityville")
            .state("Stateville")
            .email("john.doe@example.com")
            .countryId("1")
            .build();

    private final Traveler traveler2 = Traveler.builder()
            .id("2")
            .travelerId(UUID.randomUUID().toString())
            .seq(2)
            .firstName("Jane")
            .lastName("Smith")
            .addressLine1("456 Avenue")
            .addressLine2("Suite 2")
            .city("Townsville")
            .state("Province")
            .email("jane.smith@example.com")
            .countryId("2")
            .build();

    @BeforeAll
    public void startServer() {
        mockServerConfigTravelerService = new MockServerConfigTravelerService();
        mockServerConfigTravelerService.startMockServer();
        mockServerConfigTravelerService.registerGetTravelerByIdEndpoint(traveler1.getTravelerId(), traveler1);
        mockServerConfigTravelerService.registerGetTravelerByIdEndpoint(traveler2.getTravelerId(), traveler2);
        mockServerConfigTravelerService.registerGetTravelerByInvalidIdEndpoint(INVALID_TRAVELER_ID);
    }

    @AfterAll
    public void stopServer() {
        mockServerConfigTravelerService.stopMockServer();
    }

    @BeforeEach
    public void setupDB() {
        Publisher<Traveler> setupDB = travelerRepository.deleteAll()
                .thenMany(Flux.just(traveler1, traveler2))
                .flatMap(travelerRepository::save);

        StepVerifier.create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void whenGetAllTravelers_thenReturnAllTravelers() {
        webTestClient.get()
                .uri("/api/v1/travelers")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(Traveler.class)
                .hasSize(2)
                .value(travelers -> {
                    assertEquals(2, travelers.size());
                    assertEquals(traveler1.getFirstName(), travelers.get(0).getFirstName());
                    assertEquals(traveler2.getFirstName(), travelers.get(1).getFirstName());
                });

        StepVerifier.create(travelerRepository.findAll())
                .expectNextMatches(traveler -> traveler.getFirstName().equals(traveler1.getFirstName()))
                .expectNextMatches(traveler -> traveler.getFirstName().equals(traveler2.getFirstName()))
                .verifyComplete();
    }

    @Test
    void whenGetTravelerById_thenReturnTraveler() {
        webTestClient.get()
                .uri("/api/v1/travelers/" + traveler1.getTravelerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody(Traveler.class)
                .value(traveler -> assertEquals(traveler1.getFirstName(), traveler.getFirstName()));

        StepVerifier.create(travelerRepository.findTravelerByTravelerId(traveler1.getTravelerId()))
                .expectNextMatches(traveler -> traveler.getFirstName().equals(traveler1.getFirstName()))
                .verifyComplete();
    }

    @Test
    void whenGetTravelerByInvalidId_thenReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/travelers/" + INVALID_TRAVELER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenCreateTraveler_thenReturnCreatedTraveler() {
        Traveler newTraveler = Traveler.builder()
                .travelerId(UUID.randomUUID().toString())
                .seq(3)
                .firstName("Alice")
                .lastName("Johnson")
                .addressLine1("789 Boulevard")
                .addressLine2("Unit 3")
                .city("Metropolis")
                .state("Region")
                .email("alice.johnson@example.com")
                .countryId("3")
                .build();

        webTestClient.post()
                .uri("/api/v1/travelers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newTraveler)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Traveler.class)
                .value(savedTraveler -> {
                    assertNotNull(savedTraveler);
                    assertEquals(newTraveler.getFirstName(), savedTraveler.getFirstName());
                    assertEquals(newTraveler.getEmail(), savedTraveler.getEmail());
                });

        StepVerifier.create(travelerRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void whenUpdateTraveler_thenReturnUpdatedTraveler() {
        Traveler updatedTraveler = Traveler.builder()
                .firstName("Updated John")
                .lastName("Updated Doe")
                .addressLine1("New Address")
                .addressLine2("Updated Apt")
                .city("Updated City")
                .state("Updated State")
                .email("updated.john@example.com")
                .countryId("1")
                .build();

        webTestClient.put()
                .uri("/api/v1/travelers/{travelerId}", traveler1.getTravelerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedTraveler)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Traveler.class)
                .value(traveler -> assertEquals("Updated John", traveler.getFirstName()));

        StepVerifier.create(travelerRepository.findById(traveler1.getId()))
                .expectNextMatches(traveler -> traveler.getFirstName().equals("Updated John"))
                .verifyComplete();
    }

    @Test
    void whenDeleteTraveler_thenTravelerIsDeleted() {
        webTestClient.delete()
                .uri("/api/v1/travelers/{travelerId}", traveler1.getTravelerId())
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(travelerRepository.findById(traveler1.getId()))
                .verifyComplete();
    }

    @Test
    void whenDeleteTraveler_withInvalidId_thenReturnNotFound() {
        webTestClient.delete()
                .uri("/api/v1/travelers/{travelerId}", INVALID_TRAVELER_ID)
                .exchange()
                .expectStatus().isNotFound();
    }
}
