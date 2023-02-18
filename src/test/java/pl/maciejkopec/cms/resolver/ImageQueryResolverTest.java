package pl.maciejkopec.cms.resolver;

import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.http.HttpHeaders;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureGraphQlTester
@AutoConfigureWebTestClient
public class ImageQueryResolverTest {

  @Autowired
  private WebGraphQlTester graphQlTester;

  @MockBean
  private ImageRepository imageRepository;

  @BeforeEach
  public void setUp() {
    this.graphQlTester = this.graphQlTester
        .mutate()
        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY"))
        .build();
  }

  @Test
  public void shouldGetImage() {
    when(imageRepository.findById("id")).thenReturn(Mono.just(Document.valid()));

    graphQlTester.documentName("get-image").execute()
        .path("data.image.id").entity(String.class).isEqualTo("id")
        .path("data.image.image").entity(String.class).isEqualTo("image_id")
        .path("data.image.filename").entity(String.class).isEqualTo("filename");

  }

  @Test
  public void shouldReturn404() {
    when(imageRepository.findById(anyString())).thenReturn(Mono.empty());

    graphQlTester.documentName("get-image-404").execute()
        .path("data.image.status").entity(String.class).isEqualTo("404");

  }

  @Test
  public void shouldGetAllImages() {
    when(imageRepository.findAll()).thenReturn(Flux.just(Document.valid(), Document.minimum()));

    graphQlTester.documentName("get-images").execute()
        .path("data.images").entityList(Image.class)
        .hasSize(2)
        .containsExactly(
            Image.builder().id("id").image("image_id").filename("filename").build(),
            Image.builder().id("id").build()
        );

  }
}
