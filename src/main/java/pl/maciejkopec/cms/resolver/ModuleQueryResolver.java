package pl.maciejkopec.cms.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.Result;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.mapper.ModuleMapper;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ModuleQueryResolver implements GraphQLQueryResolver {

  private final ModuleRepository repository;
  private final ModuleMapper mapper;

  public CompletableFuture<Result> module(final String id) {

    return repository
        .findById(id)
        .map(mapper::toDto)
        .map(Result.class::cast)
        .switchIfEmpty(
            Mono.just(
                Status.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .build()))
        .toFuture();
  }

  public CompletableFuture<List<Module>> modules() {
    return repository.findAll().map(mapper::toDto).collect(Collectors.toList()).toFuture();
  }
}
