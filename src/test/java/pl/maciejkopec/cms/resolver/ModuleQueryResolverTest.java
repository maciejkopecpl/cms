package pl.maciejkopec.cms.resolver;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.maciejkopec.cms.data.ModuleTestData.Document;
import pl.maciejkopec.cms.domain.ModuleType;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureGraphQlTester
@AutoConfigureWebTestClient
public class ModuleQueryResolverTest {

  @Autowired
  private WebGraphQlTester graphQlTester;
  @MockitoBean
  private ModuleRepository moduleRepository;
  @MockitoBean
  private CommonMongoOperations commonMongoOperations;
  @MockitoBean
  private ReactiveGridFsTemplate gridFsTemplate;

  @BeforeEach
  public void setUp() {
    this.graphQlTester = this.graphQlTester
        .mutate()
        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY"))
        .build();
  }

  @Test
  public void shouldGetModule() {
    when(moduleRepository.findById("id")).thenReturn(Mono.just(Document.valid()));

    graphQlTester.documentName("get-module").execute()
        .path("data.module").entity(Module.class).isEqualTo(
            Module.builder()
                .id("id")
                .title("Title of module")
                .data("{}")
                .type(ModuleType.ICONS)
                .build()
        );

  }

  @Test
  public void shouldReturn404() {
    when(moduleRepository.findById(anyString())).thenReturn(Mono.empty());

    graphQlTester.documentName("get-module-404").execute()
        .path("data.module").entity(Status.class).isEqualTo(
            Status.builder()
                .status(404)
                .message("Not Found")
                .build()
        );

  }

  @Test
  public void shouldGetAllModules() {
    when(moduleRepository.findAll(any(Sort.class)))
        .thenReturn(Flux.just(Document.valid(), Document.minimum()));

    graphQlTester.documentName("get-modules").execute()
        .path("data.modules").entityList(Module.class)
        .hasSize(2)
        .contains(
            Module.builder()
                .id("id")
                .title("Title of module")
                .data("{}")
                .type(ModuleType.ICONS)
                .build(),
            Module.builder()
                .id("id")
                .type(ModuleType.ICONS)
                .build()
        );

  }
}
