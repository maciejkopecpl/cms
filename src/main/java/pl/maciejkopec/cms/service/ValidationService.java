package pl.maciejkopec.cms.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class ValidationService {

  private final Validator validator;

  public <INPUT> Mono<INPUT> validate(final INPUT input) {

    final String constraintViolations =
        validator.validate(input).stream()
            .map(v -> format("`%s` field %s", v.getPropertyPath(), v.getMessage()))
            .collect(Collectors.joining(" and "));
    return (constraintViolations.isEmpty())
        ? Mono.just(input)
        : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, constraintViolations));
  }
}
