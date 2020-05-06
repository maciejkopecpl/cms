package pl.maciejkopec.cms.resolver;

import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.dto.graphql.UpdateImagePayload;
import pl.maciejkopec.cms.mapper.ImageMapper;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static pl.maciejkopec.cms.repository.Queries.byId;

@Component
@RequiredArgsConstructor
public class ImageMutationResolver implements GraphQLMutationResolver {

  private final ImageRepository repository;
  private final ImageMapper mapper;
  private final CommonMongoOperations commonMongoOperations;
  private final ReactiveGridFsTemplate gridFsTemplate;

  public CompletableFuture<Status> deleteImage(final String id) {
    return repository
        .findById(id)
        .flatMap(
            document -> gridFsTemplate.delete(byId(document.getImage())).then(Mono.just(document)))
        .flatMap(document -> repository.deleteById(document.getId()).then(Mono.just(document)))
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

  public CompletableFuture<UpdateImagePayload> updateImage(final Image dto) {

    return commonMongoOperations
        .getAndReplace(mapper.toDomain(dto), byId(dto.getId()), ImageDocument.class)
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
