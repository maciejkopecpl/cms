package pl.maciejkopec.cms.handler;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.http.MediaType.asMediaType;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static pl.maciejkopec.cms.repository.Queries.byId;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.Module;
import pl.maciejkopec.cms.dto.SavedImage;
import pl.maciejkopec.cms.mapper.ImageMapper;
import pl.maciejkopec.cms.repository.CommonMongoOperations;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ImageHandler {

  private static final String ID = "id";
  private final ImageRepository repository;
  private final ImageMapper mapper;
  private final ReactiveGridFsTemplate gridFsTemplate;
  private final CommonMongoOperations commonMongoOperations;
  private final ReactiveMongoTemplate mongoTemplate;

  @NotNull
  public Mono<ServerResponse> upload(final ServerRequest request) {
    final var response =
        request
            .body(BodyExtractors.toMultipartData())
            .flux()
            .flatMap(this::saveImage)
            .map(mapper::toDomain)
            .flatMap(repository::insert)
            .map(mapper::toDto);

    return ok().body(response, Image.class);
  }

  private Flux<SavedImage> saveImage(final MultiValueMap<String, Part> values) {
    return Flux.merge(
        values.get("file").stream()
            .map(FilePart.class::cast)
            .map(
                file ->
                    gridFsTemplate
                        .store(file.content(), file.filename(), file.headers().getContentType())
                        .map(item -> new SavedImage(item, file.filename())))
            .collect(Collectors.toList()));
  }

  @NotNull
  public Mono<ServerResponse> deleteAll(final ServerRequest serverRequest) {
    final var response =
        Flux.concat(
            repository.deleteAll(),
            mongoTemplate.dropCollection("fs.files"),
            mongoTemplate.dropCollection("fs.chunks"));
    return ok().body(response, Module.class);
  }

  @NotNull
  public Mono<ServerResponse> get(final ServerRequest request) {
    return repository
        .findById(request.pathVariable(ID))
        .map(mapper::toDto)
        .flatMap(image -> ok().bodyValue(image))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  @NotNull
  public Mono<ServerResponse> findAll(final ServerRequest request) {
    final var response = repository.findAll().map(mapper::toDto);
    return ok().body(response, Image.class);
  }

  @NotNull
  public Mono<ServerResponse> showImage(final ServerRequest request) {
    return gridFsTemplate
        .findOne(byId(request.pathVariable(ID)))
        .flatMap(
            gridFSFile -> {
              final var dataBuffer =
                  Flux.from(gridFsTemplate.getResource(gridFSFile))
                      .flatMap(ReactiveGridFsResource::getDownloadStream);

              final var metadata = requireNonNullElse(gridFSFile.getMetadata(), new Document());
              final var mediaType =
                  asMediaType(
                      new MimeType(metadata.getString("type"), metadata.getString("subtype")));

              return ok().contentType(mediaType).body(dataBuffer, DataBuffer.class);
            });
  }

  @NotNull
  public Mono<ServerResponse> update(final ServerRequest request) {
    return request
        .bodyToMono(Image.class)
        .map(mapper::toDomain)
        .flatMap(
            document ->
                commonMongoOperations.getAndReplace(
                    document, byId(request.pathVariable(ID)), ImageDocument.class))
        .map(mapper::toDto)
        .flatMap(image -> ok().bodyValue(image))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  @NotNull
  public Mono<ServerResponse> delete(final ServerRequest request) {
    return repository
        .findById(request.pathVariable(ID))
        .flatMap(
            document -> gridFsTemplate.delete(byId(document.getImage())).then(Mono.just(document)))
        .flatMap(document -> repository.deleteById(document.getId()).then(Mono.just(document)))
        .flatMap(response -> ServerResponse.noContent().build())
        .switchIfEmpty(ServerResponse.notFound().build());
  }
}
