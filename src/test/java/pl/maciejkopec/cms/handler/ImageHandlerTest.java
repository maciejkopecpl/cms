package pl.maciejkopec.cms.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.ImageTestData.Document;
import static pl.maciejkopec.cms.data.ImageTestData.Dto;
import static pl.maciejkopec.cms.repository.Queries.byId;

import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import pl.maciejkopec.cms.configuration.JacksonConfiguration;
import pl.maciejkopec.cms.configuration.SecurityConfiguration;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.mapper.ImageMapperImpl;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import pl.maciejkopec.cms.router.ImageRouter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      ImageRouter.class,
      ImageHandler.class,
      ImageMapperImpl.class,
      JacksonConfiguration.class,
      SecurityConfiguration.class
    })
@WebFluxTest
@AutoConfigureWebTestClient
public class ImageHandlerTest {

  @MockBean private ImageRepository repository;
  @Autowired private WebTestClient webTestClient;
  @MockBean private CommonMongoOperations commonMongoOperations;
  @MockBean private ReactiveGridFsTemplate gridFsTemplate;

  @Test
  public void shouldGetImage() {
    when(repository.findById("id")).thenReturn(Mono.just(Document.valid()));

    webTestClient
        .get()
        .uri("/images/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Image.class)
        .isEqualTo(Dto.valid());
  }

  @Test
  public void shouldReturn404() {
    when(repository.findById(anyString())).thenReturn(Mono.empty());

    webTestClient
        .get()
        .uri("/images/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void shouldGetAllImages() {
    when(repository.findAll()).thenReturn(Flux.just(Document.valid(), Document.valid()));

    webTestClient
        .get()
        .uri("/images/")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Image.class)
        .isEqualTo(List.of(Dto.valid(), Dto.valid()));
  }

  @Test
  public void shouldSaveImage() {
    final ObjectId objectId = ObjectId.get();
    final ImageDocument notSavedDocument =
        Document.minimum().toBuilder().id(null).image(objectId.toHexString()).build();
    final ImageDocument savedWithImage = notSavedDocument.toBuilder().id("id").build();

    when(gridFsTemplate.store(any(), eq("image.png"), any(Object.class)))
        .thenReturn(Mono.just(objectId));
    when(repository.insert(notSavedDocument)).thenReturn(Mono.just(savedWithImage));

    final MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder
        .part("file", new ByteArrayResource("just some test data".getBytes()))
        .filename("image.png");

    webTestClient
        .post()
        .uri("/images/")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Image.class)
        .isEqualTo(Dto.minimum().toBuilder().image(objectId.toHexString()).build());

    verify(repository).insert(notSavedDocument);
    verify(gridFsTemplate).store(any(), eq("image.png"), any(Object.class));
  }

  @Test
  public void shouldDeleteImage() {
    when(repository.findById("id")).thenReturn(Mono.just(Document.valid()));
    when(gridFsTemplate.delete(byId("image_id"))).thenReturn(Mono.empty());
    when(repository.deleteById("id")).thenReturn(Mono.empty());

    webTestClient
        .delete()
        .uri("/images/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(repository).deleteById("id");
    verify(gridFsTemplate).delete(byId("image_id"));
  }

  @Test
  public void shouldReturn404WhenNoImageForDeleteImage() {
    when(repository.findById("id")).thenReturn(Mono.empty());

    webTestClient
        .delete()
        .uri("/images/id")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();

    verify(repository, never()).deleteById(anyString());
    verify(gridFsTemplate, never()).delete(any());
  }

  @Test
  public void shouldUpdateImage() {
    when(commonMongoOperations.getAndReplace(Document.minimum(), byId("id"), ImageDocument.class))
        .thenReturn(Mono.just(Document.minimum()));

    webTestClient
        .put()
        .uri("/images/id")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Dto.minimum()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Image.class)
        .isEqualTo(Dto.minimum());
  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() {
    when(commonMongoOperations.getAndReplace(Document.minimum(), byId("id"), ImageDocument.class))
        .thenReturn(Mono.empty());

    webTestClient
        .put()
        .uri("/images/id")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Dto.minimum()))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
