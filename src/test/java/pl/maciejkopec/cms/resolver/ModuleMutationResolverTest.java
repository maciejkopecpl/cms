package pl.maciejkopec.cms.resolver;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.maciejkopec.cms.data.ModuleTestData.Document;
import pl.maciejkopec.cms.domain.ModuleType;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.CreateModulePayload;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.dto.graphql.UpdateModulePayload;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureGraphQlTester
@AutoConfigureWebTestClient
public class ModuleMutationResolverTest {

  @Autowired
  private WebGraphQlTester graphQlTester;

  @MockitoBean
  private ModuleRepository moduleRepository;
  @MockitoBean
  private CommonMongoOperations commonMongoOperations;

  @BeforeEach
  public void setUp() {
    this.graphQlTester = this.graphQlTester
        .mutate()
        .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "FAKE_API_KEY"))
        .build();
  }

  @Test
  public void shouldSaveModule() {
    when(moduleRepository.save(any())).thenReturn(Mono.just(Document.valid()));

    graphQlTester.documentName("create-module").execute()
        .path("data.createModule").entity(CreateModulePayload.class)
        .isEqualTo(
            CreateModulePayload.builder()
                .id("id")
                .module(
                    Module.builder()
                        .id("id")
                        .title("Title of module")
                        .type(ModuleType.ICONS)
                        .build()
                )
                .status(
                    Status.builder()
                        .status(201)
                        .message("Created")
                        .build()
                )
                .build()
        );

  }

  @Test
  public void shouldReturn400WhenFailToSaveModule() {
    when(moduleRepository.save(any())).thenReturn(Mono.empty());

    graphQlTester.documentName("create-module").execute()
        .path("data.createModule").entity(CreateModulePayload.class)
        .isEqualTo(
            CreateModulePayload.builder()
                .id(null)
                .module(
                    Module.builder()
                        .id(null)
                        .title("Title of module")
                        .type(ModuleType.ICONS)
                        .build()
                )
                .status(
                    Status.builder()
                        .status(400)
                        .message("Bad Request")
                        .build()
                )
                .build()
        );
  }

  @Test
  public void shouldDeleteModule() {
    when(moduleRepository.findById("id")).thenReturn(Mono.just(Document.valid()));
    when(moduleRepository.delete(any())).thenReturn(Mono.empty());

    graphQlTester.documentName("delete-module").execute()
        .path("data.deleteModule").entity(Status.class)
        .isEqualTo(
            Status.builder()
                .status(200)
                .message("OK")
                .build()

        );

  }

  @Test
  public void shouldReturn404WhenNotFoundForDeleteModule() {
    when(moduleRepository.findById("id")).thenReturn(Mono.empty());

    graphQlTester.documentName("delete-module").execute()
        .path("data.deleteModule").entity(Status.class)
        .isEqualTo(
            Status.builder()
                .status(404)
                .message("Not Found")
                .build()

        );

    verify(moduleRepository, never()).delete(any());
  }

  @Test
  public void shouldUpdateModule() {
    when(commonMongoOperations.getAndReplace(any(), any(), any()))
        .thenReturn(Mono.just(Document.valid()));

    graphQlTester.documentName("update-module").execute()
        .path("data.updateModule").entity(UpdateModulePayload.class)
        .isEqualTo(
            UpdateModulePayload.builder()
                .id("id")
                .module(
                    Module.builder()
                        .id("id")
                        .title("Title of module")
                        .type(ModuleType.ICONS)
                        .build()
                )
                .status(
                    Status.builder()
                        .status(200)
                        .message("OK")
                        .build()
                )
                .build()
        );

  }

  @Test
  public void shouldReturn404WhenNotFoundForUpdate() {
    when(commonMongoOperations.getAndReplace(any(), any(), any())).thenReturn(Mono.empty());

    graphQlTester.documentName("update-module").execute()
        .path("data.updateModule").entity(UpdateModulePayload.class)
        .isEqualTo(
            UpdateModulePayload.builder()
                .id("id")
                .module(
                    Module.builder()
                        .id("id")
                        .title("Title of module")
                        .type(ModuleType.ICONS)
                        .build()
                )
                .status(
                    Status.builder()
                        .status(404)
                        .message("Not Found")
                        .build()
                )
                .build()
        );
  }
}
