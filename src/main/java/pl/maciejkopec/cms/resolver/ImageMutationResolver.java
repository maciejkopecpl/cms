package pl.maciejkopec.cms.resolver;

import static pl.maciejkopec.cms.repository.Queries.byId;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.dto.graphql.UpdateImagePayload;
import pl.maciejkopec.cms.mapper.ImageMapper;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ImageMutationResolver {

  private final ImageRepository repository;
  private final ImageMapper mapper;
  private final CommonMongoOperations commonMongoOperations;
  private final ReactiveGridFsTemplate gridFsTemplate;

  @MutationMapping
  public Mono<Status> deleteImage(@Argument final String id) {
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
                .build());
  }

  @MutationMapping
  public Mono<UpdateImagePayload> updateImage(@Argument("image") final Image dto) {

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
                .build());
  }
}
