package pl.maciejkopec.cms.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.maciejkopec.cms.data.ModuleTestData.Document;

@SpringBootTest(
    properties = "spring.main.web-application-type=reactive",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ModuleIntegrationTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @Autowired private WebTestClient webTestClient;
  @Autowired private ModuleRepository moduleRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    moduleRepository
        .deleteAll()
        .thenMany(
            Flux.just("1", "2", "3", "4")
                .map(id -> Document.notSaved().toBuilder().title("Document #" + id).build())
                .flatMap(moduleRepository::save))
        .blockLast();
    moduleRepository
        .findAll()
        .collectList()
        .blockOptional()
        .orElse(List.of())
        .forEach(document -> log.info("Document in test database: {}", document));
  }

  @Test
  void shouldMatchGetModulesResponses() throws IOException {

    final var fluxResponse =
        webTestClient
            .get()
            .uri("/modules/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(Module.class)
            .getResponseBody()
            .collectList()
            .block();

    assertThat(fluxResponse).hasSize(4);

    final var graphQLResponse =
        graphQLTestTemplate
            .postForResource("graphql/get-modules.graphql")
            .getList("$.data.modules", Module.class);

    assertThat(graphQLResponse).hasSize(4);

    assertThat(graphQLResponse).containsAll(fluxResponse);
  }

  @Test
  void shouldMatchGetModuleResponses() throws IOException {
    final var document =
        moduleRepository.findAll().collectList().blockOptional().orElseThrow().get(0);
    assertThat(document).isNotNull();

    final var fluxResponse =
        webTestClient
            .get()
            .uri("/modules/{id}", document.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .returnResult(Module.class)
            .getResponseBody()
            .blockFirst();
    assertThat(fluxResponse).isNotNull();

    final var variables = objectMapper.createObjectNode();
    variables.set("id", objectMapper.convertValue(document.getId(), JsonNode.class));

    final var graphQLResponse =
        graphQLTestTemplate
            .perform("graphql/get-module.graphql", variables)
            .get("$.data.module", Module.class);
    assertThat(graphQLResponse).isNotNull();

    assertThat(graphQLResponse).isEqualTo(fluxResponse);
  }
}
