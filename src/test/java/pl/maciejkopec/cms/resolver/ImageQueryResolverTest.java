package pl.maciejkopec.cms.resolver;

import com.graphql.spring.boot.test.GraphQLTest;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.maciejkopec.cms.mapper.ImageMapperImpl;
import pl.maciejkopec.cms.mapper.ModuleMapperImpl;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.ImageTestData.Document;

@ExtendWith(SpringExtension.class)
@GraphQLTest
@ContextConfiguration(classes = {ImageMapperImpl.class, ModuleMapperImpl.class})
@AutoConfigureWebTestClient
public class ImageQueryResolverTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @MockBean private ImageRepository imageRepository;
  @MockBean private ModuleRepository moduleRepository;
  @MockBean private CommonMongoOperations commonMongoOperations;
  @MockBean private ReactiveGridFsTemplate gridFsTemplate;

  @Test
  public void shouldGetImage() throws IOException {
    when(imageRepository.findById("id")).thenReturn(Mono.just(Document.valid()));

    final var response = graphQLTestTemplate.postForResource("graphql/get-image.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.image.id")).isEqualTo("id");
    assertThat(response.get("$.data.image.image")).isEqualTo("image_id");
    assertThat(response.get("$.data.image.alt")).isEqualTo("alt");
  }

  @Test
  public void shouldReturn404() throws IOException {
    when(imageRepository.findById(anyString())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/get-image-404.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.image.status")).isEqualTo("404");
  }

  @Test
  public void shouldGetAllImages() throws IOException {
    when(imageRepository.findAll()).thenReturn(Flux.just(Document.valid(), Document.minimum()));

    final var response = graphQLTestTemplate.postForResource("graphql/get-images.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.getList("$.data.images", Object.class)).hasSize(2);
    assertThat(response.get("$.data.images[1].id")).isEqualTo("id");
    assertThat(response.get("$.data.images[1].image")).isNull();
    assertThat(response.get("$.data.images[1].alt")).isNull();
  }
}
