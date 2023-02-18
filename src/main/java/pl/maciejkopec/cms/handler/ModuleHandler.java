package pl.maciejkopec.cms.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static pl.maciejkopec.cms.repository.Queries.byId;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.domain.ModuleDocument;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.mapper.ModuleMapper;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ModuleHandler {

  private static final String ID = "id";
  private final ModuleRepository repository;
  private final ModuleMapper mapper;
  private final CommonMongoOperations commonMongoOperations;

  @NotNull
  public Mono<ServerResponse> findById(final ServerRequest request) {
    return repository
        .findById(request.pathVariable(ID))
        .map(mapper::toDto)
        .flatMap(module -> ok().bodyValue(module))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  @NotNull
  public Mono<ServerResponse> findAll(final ServerRequest request) {
    final var modules = repository.findAll(Sort.by("order")).map(mapper::toDto);
    return ok().body(modules, Module.class);
  }

  @NotNull
  public Mono<ServerResponse> save(final ServerRequest request) {
    return request
        .bodyToMono(Module.class)
        .map(mapper::toDomain)
        .flatMap(repository::save)
        .map(mapper::toDto)
        .flatMap(module -> ok().bodyValue(module))
        .switchIfEmpty(ServerResponse.badRequest().build());
  }

  @NotNull
  public Mono<ServerResponse> saveBulk(final ServerRequest request) {
    return request
        .bodyToFlux(Module.class)
        .map(mapper::toDomain)
        .flatMap(repository::save)
        .map(mapper::toDto)
        .as(Flux::collectList)
        .flatMap(modules -> ok().bodyValue(modules))
        .switchIfEmpty(ServerResponse.badRequest().build());
  }

  @NotNull
  public Mono<ServerResponse> deleteById(final ServerRequest request) {
    final Mono<Void> module = repository.deleteById(request.pathVariable(ID));
    return ok().body(module, Module.class);
  }

  @NotNull
  public Mono<ServerResponse> deleteAll(final ServerRequest serverRequest) {
    final Mono<Void> module = repository.deleteAll();
    return ok().body(module, Module.class);
  }

  @NotNull
  public Mono<ServerResponse> update(final ServerRequest request) {
    return request
        .bodyToMono(Module.class)
        .map(mapper::toDomain)
        .flatMap(
            document ->
                commonMongoOperations.getAndReplace(
                    document, byId(request.pathVariable(ID)), ModuleDocument.class))
        .map(mapper::toDto)
        .flatMap(module -> ok().bodyValue(module))
        .switchIfEmpty(ServerResponse.notFound().build());
  }
}
