package pl.maciejkopec.cms.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.ModuleTestData.Document;
import static pl.maciejkopec.cms.data.ModuleTestData.Dto;
import static pl.maciejkopec.cms.repository.Queries.byId;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import pl.maciejkopec.cms.configuration.JacksonConfiguration;
import pl.maciejkopec.cms.configuration.SecurityConfiguration;
import pl.maciejkopec.cms.domain.ModuleDocument;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.mapper.ModuleMapperImpl;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ModuleRepository;
import pl.maciejkopec.cms.router.ModuleRouter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      ModuleRouter.class,
      ModuleHandler.class,
      ModuleMapperImpl.class,
      JacksonConfiguration.class,
      SecurityConfiguration.class
    })
@WebFluxTest
@AutoConfigureWebTestClient
public class ModuleHandlerTest {

  @MockitoBean
  private ModuleRepository repository;
  @Autowired private WebTestClient webTestClient;
  @MockitoBean private CommonMongoOperations commonMongoOperations;

  @BeforeEach
  void configureWebClient() {
    webTestClient =
        webTestClient.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY").build();
  }

  @Test
  public void shouldGetModule() {
    when(repository.findById("id")).thenReturn(Mono.just(Document.valid()));

    webTestClient
        .get()
        .uri("/modules/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Module.class)
        .isEqualTo(Dto.valid());
  }

  @Test
  public void shouldReturn404() {
    when(repository.findById(anyString())).thenReturn(Mono.empty());

    webTestClient
        .get()
        .uri("/modules/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void shouldGetAllModules() {
    when(repository.findAll(any(Sort.class)))
        .thenReturn(Flux.just(Document.valid(), Document.valid()));

    webTestClient
        .get()
        .uri("/modules/")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Module.class)
        .isEqualTo(List.of(Dto.valid(), Dto.valid()));
  }

  @Test
  public void shouldSaveModule() {
    when(repository.save(Document.notSaved())).thenReturn(Mono.just(Document.valid()));

    webTestClient
        .post()
        .uri("/modules/")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Dto.notSaved()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Module.class)
        .isEqualTo(Dto.valid());

    verify(repository).save(Document.notSaved());
  }

  @Test
  public void shouldReturnBadRequestOnSaveWithNoBody() {
    webTestClient
        .post()
        .uri("/modules/")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  public void shouldDeleteModule() {
    when(repository.deleteById("id")).thenReturn(Mono.empty());

    webTestClient
        .delete()
        .uri("/modules/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();

    verify(repository).deleteById("id");
  }

  @Test
  public void shouldUpdateModule() {
    when(commonMongoOperations.getAndReplace(Document.minimum(), byId("id"), ModuleDocument.class))
        .thenReturn(Mono.just(Document.minimum()));

    webTestClient
        .put()
        .uri("/modules/id")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Dto.minimum()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Module.class)
        .isEqualTo(Dto.minimum());
  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() {
    when(commonMongoOperations.getAndReplace(Document.minimum(), byId("id"), ModuleDocument.class))
        .thenReturn(Mono.empty());

    webTestClient
        .put()
        .uri("/modules/id")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Dto.minimum()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
