package pl.maciejkopec.cms.resolver;

import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.maciejkopec.cms.domain.ModuleDocument;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.graphql.CreateModulePayload;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.dto.graphql.UpdateModulePayload;
import pl.maciejkopec.cms.mapper.ModuleMapper;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ModuleRepository;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static pl.maciejkopec.cms.repository.Queries.byId;

@Component
@RequiredArgsConstructor
public class ModuleMutationResolver implements GraphQLMutationResolver {

  private final ModuleRepository repository;
  private final ModuleMapper mapper;
  private final CommonMongoOperations commonMongoOperations;

  public CompletableFuture<CreateModulePayload> createModule(final Module dto) {

    return repository
        .save(mapper.toDomain(dto))
        .map(
            module ->
                mapper.toCreatePayload(module).toBuilder()
                    .status(
                        Status.builder()
                            .status(HttpStatus.CREATED.value())
                            .message(HttpStatus.CREATED.getReasonPhrase())
                            .build())
                    .build())
        .defaultIfEmpty(
            mapper.toCreatePayload(dto).toBuilder()
                .status(
                    Status.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .build())
                .build())
        .toFuture();
  }

  public CompletableFuture<Status> deleteModule(final String id) {
    return repository
        .findById(id)
        .flatMap(document -> repository.delete(document).then(Mono.just(document)))
        .map(
            document ->
                Status.builder()
                    .status(HttpStatus.OK.value())
                    .message(HttpStatus.OK.getReasonPhrase())
                    .build())
        .defaultIfEmpty(
            Status.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(HttpStatus.NOT_FOUND.getReasonPhrase())
                .build())
        .toFuture();
  }

  public CompletableFuture<UpdateModulePayload> updateModule(final Module dto) {

    return commonMongoOperations
        .getAndReplace(mapper.toDomain(dto), byId(dto.getId()), ModuleDocument.class)
        .map(
            module ->
                mapper.toUpdatePayload(module).toBuilder()
                    .status(
                        Status.builder()
                            .status(HttpStatus.OK.value())
                            .message(HttpStatus.OK.getReasonPhrase())
                            .build())
                    .build())
        .defaultIfEmpty(
            mapper.toUpdatePayload(dto).toBuilder()
                .status(
                    Status.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .build())
                .build())
        .toFuture();
  }
}
