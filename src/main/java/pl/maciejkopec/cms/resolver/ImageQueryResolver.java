package pl.maciejkopec.cms.resolver;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.graphql.Result;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.mapper.ImageMapper;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImageQueryResolver implements GraphQLQueryResolver {

  private final ImageRepository repository;
  private final ImageMapper mapper;

  public CompletableFuture<Result> image(final String id) {
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

  public CompletableFuture<List<Image>> images() {
    return repository.findAll().map(mapper::toDto).collect(Collectors.toList()).toFuture();
  }
}
