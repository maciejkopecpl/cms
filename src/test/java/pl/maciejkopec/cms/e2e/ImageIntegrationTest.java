package pl.maciejkopec.cms.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.maciejkopec.cms.data.ImageTestData.Document;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Flux;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@AutoConfigureWebTestClient
public class ImageIntegrationTest {

  @Autowired
  private WebGraphQlTester graphQlTester;
  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private ImageRepository imageRepository;

  @BeforeEach
  void configureClients() {
    this.webTestClient =
        this.webTestClient.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY")
            .build();
    this.graphQlTester = this.graphQlTester
        .mutate()
        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY"))
        .build();
  }

  @BeforeEach
  void setUp() {
    imageRepository
        .deleteAll()
        .thenMany(
            Flux.just("1", "2", "3", "4")
                .map(id -> Document.notSaved().toBuilder().filename("Document-" + id).build())
                .flatMap(imageRepository::save))
        .blockLast();
    imageRepository
        .findAll()
        .collectList()
        .blockOptional()
        .orElse(List.of())
        .forEach(document -> log.info("Document in test database: {}", document));
  }

  @Test
  void shouldMatchGetImagesResponses() {

    final var fluxResponse =
        webTestClient
            .get()
            .uri("/images/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(Image.class)
            .getResponseBody()
            .collectList()
            .block();

    assertThat(fluxResponse).hasSize(4);

    final var graphQLResponse =
        graphQlTester.documentName("get-images").execute()
            .path("data.images").entityList(Image.class).get();

    assertThat(graphQLResponse)
        .hasSize(4)
        .containsAll(fluxResponse);
  }

  @Test
  void shouldMatchGetImageResponses() {
    final var document =
        imageRepository.findAll().collectList().blockOptional().orElseThrow().get(0);
    assertThat(document).isNotNull();

    final var fluxResponse =
        webTestClient
            .get()
            .uri("/images/{id}", document.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(Image.class)
            .getResponseBody()
            .blockFirst();
    assertThat(fluxResponse).isNotNull();

    final var graphQLResponse =
        graphQlTester.documentName("get-image").variable("id", document.getId()).execute()
            .path("data.image").entity(Image.class).get();

    assertThat(graphQLResponse)
        .isNotNull()
        .isEqualTo(fluxResponse);
  }
}
