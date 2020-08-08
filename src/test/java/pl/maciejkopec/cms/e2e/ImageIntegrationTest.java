package pl.maciejkopec.cms.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.maciejkopec.cms.data.ImageTestData.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Flux;

@SpringBootTest(
    properties = "spring.main.web-application-type=reactive",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ImageIntegrationTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @Autowired private WebTestClient webTestClient;
  @Autowired private ImageRepository imageRepository;
  @Autowired private ObjectMapper objectMapper;

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
  void shouldMatchGetImagesResponses() throws IOException {

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
        graphQLTestTemplate
            .postForResource("graphql/get-images.graphql")
            .getList("$.data.images", Image.class);

    assertThat(graphQLResponse).hasSize(4);

    assertThat(graphQLResponse).containsAll(fluxResponse);
  }

  @Test
  void shouldMatchGetImageResponses() throws IOException {
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

    final var variables = objectMapper.createObjectNode();
    variables.set("id", objectMapper.convertValue(document.getId(), JsonNode.class));

    final var graphQLResponse =
        graphQLTestTemplate
            .perform("graphql/get-image.graphql", variables)
            .get("$.data.image", Image.class);
    assertThat(graphQLResponse).isNotNull();

    assertThat(graphQLResponse).isEqualTo(fluxResponse);
  }
}
