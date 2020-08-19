package pl.maciejkopec.cms.e2e;

import static java.util.stream.Stream.of;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    properties = "spring.main.web-application-type=reactive",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SecurityIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldRejectRequestWith401() {

    of(
            webTestClient.get().uri("/images/"),
            webTestClient.put().uri("/images/id"),
            webTestClient.post().uri("/images/"),
            webTestClient.delete().uri("/images/id"),
            webTestClient.get().uri("/modules/"),
            webTestClient.put().uri("/modules/id"),
            webTestClient.post().uri("/modules/"),
            webTestClient.delete().uri("/modules/id"))
        .forEach(uri -> uri.exchange().expectStatus().isUnauthorized());
  }
}
