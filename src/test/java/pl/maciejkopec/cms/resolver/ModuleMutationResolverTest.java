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
import static org.mockito.Mockito.*;
import static pl.maciejkopec.cms.data.ModuleTestData.Document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ImageMapperImpl.class, ModuleMapperImpl.class})
@GraphQLTest
@Import(JacksonConfiguration.class)
public class ModuleMutationResolverTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @MockBean private ImageRepository imageRepository;
  @MockBean private ModuleRepository moduleRepository;
  @MockBean private CommonMongoOperations commonMongoOperations;
  @MockBean private ReactiveGridFsTemplate gridFsTemplate;

  @Test
  public void shouldSaveModule() throws Exception {
    when(moduleRepository.save(any())).thenReturn(Mono.just(Document.valid()));

    final var response = graphQLTestTemplate.postForResource("graphql/create-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.createModule.id")).isEqualTo("id");
    assertThat(response.get("$.data.createModule.module.id")).isEqualTo("id");
    assertThat(response.get("$.data.createModule.module.title")).isEqualTo("Title of module");
    assertThat(response.get("$.data.createModule.module.type")).isEqualTo("ICONS");
    assertThat(response.get("$.data.createModule.status.status")).isEqualTo("201");
    assertThat(response.get("$.data.createModule.status.message")).isEqualTo("Created");
  }

  @Test
  public void shouldReturn400WhenFailToSaveModule() throws IOException {
    when(moduleRepository.save(any())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/create-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.createModule.id")).isNull();
    assertThat(response.get("$.data.createModule.module.title")).isEqualTo("Title of module");
    assertThat(response.get("$.data.createModule.module.type")).isEqualTo("ICONS");
    assertThat(response.get("$.data.createModule.status.status")).isEqualTo("400");
    assertThat(response.get("$.data.createModule.status.message")).isEqualTo("Bad Request");
  }

  @Test
  public void shouldDeleteModule() throws IOException {
    when(moduleRepository.findById("id")).thenReturn(Mono.just(Document.valid()));
    when(moduleRepository.delete(any())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/delete-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.deleteModule.status")).isEqualTo("200");
    assertThat(response.get("$.data.deleteModule.message")).isEqualTo("OK");
  }

  @Test
  public void shouldReturn404WhenNotFoundForDeleteModule() throws IOException {
    when(moduleRepository.findById("id")).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/delete-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.deleteModule.status")).isEqualTo("404");
    assertThat(response.get("$.data.deleteModule.message")).isEqualTo("Not Found");

    verify(moduleRepository, never()).delete(any());
  }

  @Test
  public void shouldUpdateModule() throws IOException {
    when(commonMongoOperations.getAndReplace(any(), any(), any()))
        .thenReturn(Mono.just(Document.valid()));

    final var response = graphQLTestTemplate.postForResource("graphql/update-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.updateModule.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateModule.module.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateModule.module.title")).isEqualTo("Title of module");
    assertThat(response.get("$.data.updateModule.module.type")).isEqualTo("ICONS");
    assertThat(response.get("$.data.updateModule.status.status")).isEqualTo("200");
    assertThat(response.get("$.data.updateModule.status.message")).isEqualTo("OK");
  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() throws IOException {
    when(commonMongoOperations.getAndReplace(any(), any(), any())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/update-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.updateModule.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateModule.module.id")).isEqualTo("id");
    assertThat(response.get("$.data.updateModule.module.title")).isEqualTo("Title of module");
    assertThat(response.get("$.data.updateModule.module.type")).isEqualTo("ICONS");
    assertThat(response.get("$.data.updateModule.status.status")).isEqualTo("404");
    assertThat(response.get("$.data.updateModule.status.message")).isEqualTo("Not Found");
  }
}
