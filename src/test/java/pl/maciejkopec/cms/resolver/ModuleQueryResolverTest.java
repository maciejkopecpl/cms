package pl.maciejkopec.cms.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.ModuleTestData.Document;

import com.graphql.spring.boot.test.GraphQLTest;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
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

@ExtendWith(SpringExtension.class)
@GraphQLTest
@ContextConfiguration(classes = {ImageMapperImpl.class, ModuleMapperImpl.class})
@AutoConfigureWebTestClient
public class ModuleQueryResolverTest {

  @Autowired private GraphQLTestTemplate graphQLTestTemplate;
  @MockBean private ImageRepository imageRepository;
  @MockBean private ModuleRepository moduleRepository;
  @MockBean private CommonMongoOperations commonMongoOperations;
  @MockBean private ReactiveGridFsTemplate gridFsTemplate;

  @Test
  public void shouldGetModule() throws IOException {
    when(moduleRepository.findById("id")).thenReturn(Mono.just(Document.valid()));

    final var response = graphQLTestTemplate.postForResource("graphql/get-module.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.module.id")).isEqualTo("id");
    assertThat(response.get("$.data.module.title")).isEqualTo("Title of module");
    assertThat(response.get("$.data.module.type")).isEqualTo("ICONS");
  }

  @Test
  public void shouldReturn404() throws IOException {
    when(moduleRepository.findById(anyString())).thenReturn(Mono.empty());

    final var response = graphQLTestTemplate.postForResource("graphql/get-module-404.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.get("$.data.module.status")).isEqualTo("404");
  }

  @Test
  public void shouldGetAllModules() throws IOException {
    when(moduleRepository.findAll(any(Sort.class)))
        .thenReturn(Flux.just(Document.valid(), Document.minimum()));

    final var response = graphQLTestTemplate.postForResource("graphql/get-modules.graphql");

    assertThat(response).isNotNull();
    assertThat(response.isOk()).isTrue();
    assertThat(response.getList("$.data.modules", Object.class)).hasSize(2);
    assertThat(response.get("$.data.modules[1].id")).isEqualTo("id");
    assertThat(response.get("$.data.modules[1].type")).isEqualTo("ICONS");
    assertThat(response.get("$.data.modules[1].title")).isNull();
  }
}
