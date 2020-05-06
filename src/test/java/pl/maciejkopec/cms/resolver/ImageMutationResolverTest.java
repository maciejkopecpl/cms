package pl.maciejkopec.cms.resolver;

import com.graphql.spring.boot.test.GraphQLTest;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.maciejkopec.cms.configuration.JacksonConfiguration;
import pl.maciejkopec.cms.mapper.ImageMapperImpl;
import pl.maciejkopec.cms.mapper.ModuleMapperImpl;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.maciejkopec.cms.data.ImageTestData.Document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ImageMapperImpl.class, ModuleMapperImpl.class})
@GraphQLTest
@Import(JacksonConfiguration.class)
public class ImageMutationResolverTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @MockBean private ImageRepository imageRepository;
  @MockBean private ModuleRepository moduleRepository;
  @MockBean private CommonMongoOperations commonMongoOperations;
  @MockBean private ReactiveGridFsTemplate gridFsTemplate;

  @Test
  public void shouldDeleteImage() throws IOException {
    when(imageRepository.findById("id")).thenReturn(Mono.just(Document.valid()));
    when(gridFsTemplate.delete(any())).thenReturn(Mono.empty());
    when(imageRepository.deleteById(anyString())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/delete-image.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.deleteImage.status")).isEqualTo("200");
    assertThat(response.get("$.data.deleteImage.message")).isEqualTo("OK");
  }

  @Test
  public void shouldReturn404WhenNotFoundForDeleteImage() throws IOException {
    when(imageRepository.findById("id")).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/delete-image.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.deleteImage.status")).isEqualTo("404");
    assertThat(response.get("$.data.deleteImage.message")).isEqualTo("Not Found");

    verify(imageRepository, never()).deleteById(anyString());
    verify(gridFsTemplate, never()).delete(any());
  }

  @Test
  public void shouldUpdateImage() throws IOException {
    when(commonMongoOperations.getAndReplace(any(), any(), any()))
        .thenReturn(Mono.just(Document.valid()));

    final var response = graphQLTestTemplate.postForResource("graphql/update-image.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.updateImage.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateImage.image.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateImage.image.image")).isEqualTo("image_id");
    assertThat(response.get("$.data.updateImage.image.alt")).isEqualTo("alt");
    assertThat(response.get("$.data.updateImage.status.status")).isEqualTo("200");
    assertThat(response.get("$.data.updateImage.status.message")).isEqualTo("OK");
  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() throws IOException {
    when(commonMongoOperations.getAndReplace(any(), any(), any())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/update-image.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.updateImage.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateImage.image.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateImage.image.image")).isEqualTo("image_id");
    assertThat(response.get("$.data.updateImage.image.alt")).isEqualTo("alt");
    assertThat(response.get("$.data.updateImage.status.status")).isEqualTo("404");
    assertThat(response.get("$.data.updateImage.status.message")).isEqualTo("Not Found");
  }
}
