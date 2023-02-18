package pl.maciejkopec.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ValidationServiceTest {

  private ValidationService validationService;

  @BeforeEach
  void setUp() {
    Locale.setDefault(Locale.ENGLISH);

    validationService =
        new ValidationService(Validation.buildDefaultValidatorFactory().getValidator());
  }

  @Test
  void shouldReturnSuccess() {
    final var input = new TestObject("", "b");
    StepVerifier.create(validationService.validate(input)).expectNext(input).verifyComplete();
  }

  @Test
  void shouldReturnError() {
    final var input = new TestObject(null, "");
    StepVerifier.create(validationService.validate(input))
        .expectErrorSatisfies(
            throwable -> {
              assertThat(((ResponseStatusException) throwable).getStatusCode())
                  .isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(((ResponseStatusException) throwable).getReason())
                  .contains("`a` field must not be null", "`b` field must not be blank");
            })
        .verify();
  }

  private record TestObject(@NotNull String a, @NotBlank String b) {}
}
