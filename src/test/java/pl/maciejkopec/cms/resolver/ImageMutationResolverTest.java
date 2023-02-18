package pl.maciejkopec.cms.resolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.ImageTestData.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.http.HttpHeaders;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureGraphQlTester
@AutoConfigureWebTestClient
public class ImageMutationResolverTest {

  @Autowired
  private WebGraphQlTester graphQlTester;
  @MockBean
  private ImageRepository imageRepository;
  @MockBean
  private CommonMongoOperations commonMongoOperations;
  @MockBean
  private ReactiveGridFsTemplate gridFsTemplate;

  @BeforeEach
  public void setUp() {
    this.graphQlTester = this.graphQlTester
        .mutate()
        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY"))
        .build();
  }

  @Test
  public void shouldDeleteImage() {
    when(imageRepository.findById("id")).thenReturn(Mono.just(Document.valid()));
    when(gridFsTemplate.delete(any())).thenReturn(Mono.empty());
    when(imageRepository.deleteById(anyString())).thenReturn(Mono.empty());

    graphQlTester.documentName("delete-image").execute()
        .path("data.deleteImage.status").entity(String.class).isEqualTo("200")
        .path("data.deleteImage.message").entity(String.class).isEqualTo("OK");

  }

  @Test
  public void shouldReturn404WhenNotFoundForDeleteImage() {
    when(imageRepository.findById("id")).thenReturn(Mono.empty());

    graphQlTester.documentName("delete-image").execute()
        .path("$.data.deleteImage.status").entity(String.class).isEqualTo("404")
        .path("$.data.deleteImage.message").entity(String.class).isEqualTo("Not Found");

    verify(imageRepository, never()).deleteById(anyString());
    verify(gridFsTemplate, never()).delete(any());
  }

  @Test
  public void shouldUpdateImage() {
    when(commonMongoOperations.getAndReplace(any(), any(), any()))
        .thenReturn(Mono.just(Document.valid()));

    graphQlTester.documentName("update-image").execute()
        .path("$.data.updateImage.id").entity(String.class).isEqualTo("id")
        .path("$.data.updateImage.image.id").entity(String.class).isEqualTo("id")
        .path("$.data.updateImage.image.image").entity(String.class).isEqualTo("image_id")
        .path("$.data.updateImage.image.filename").entity(String.class).isEqualTo("filename")
        .path("$.data.updateImage.status.status").entity(String.class).isEqualTo("200")
        .path("$.data.updateImage.status.message").entity(String.class).isEqualTo("OK");
  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() {
    when(commonMongoOperations.getAndReplace(any(), any(), any())).thenReturn(Mono.empty());

    graphQlTester.documentName("update-image").execute()
        .path("$.data.updateImage.id").entity(String.class).isEqualTo("id")
        .path("$.data.updateImage.image.id").entity(String.class).isEqualTo("id")
        .path("$.data.updateImage.image.image").entity(String.class).isEqualTo("image_id")
        .path("$.data.updateImage.image.filename").entity(String.class).isEqualTo("filename")
        .path("$.data.updateImage.status.status").entity(String.class).isEqualTo("404")
        .path("$.data.updateImage.status.message").entity(String.class).isEqualTo("Not Found");
  }
}
