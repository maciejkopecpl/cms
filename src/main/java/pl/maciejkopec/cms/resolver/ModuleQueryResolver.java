package pl.maciejkopec.cms.resolver;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.Result;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.mapper.ModuleMapper;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ModuleQueryResolver {

  private final ModuleRepository repository;
  private final ModuleMapper mapper;

  @QueryMapping
  public Mono<Result> module(@Argument final String id) {

    return repository
        .findById(id)
        .map(mapper::toDto)
        .map(Result.class::cast)
        .switchIfEmpty(
            Mono.just(
                Status.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .build()));
  }

  @QueryMapping
  public Mono<List<Module>> modules() {
    return repository
        .findAll(Sort.by("order"))
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }
}
