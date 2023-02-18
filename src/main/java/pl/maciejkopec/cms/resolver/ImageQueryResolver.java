package pl.maciejkopec.cms.resolver;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.graphql.Result;
import pl.maciejkopec.cms.dto.graphql.Status;
import pl.maciejkopec.cms.mapper.ImageMapper;
import pl.maciejkopec.cms.repository.ImageRepository;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ImageQueryResolver {

  private final ImageRepository repository;
  private final ImageMapper mapper;

  @QueryMapping
  public Mono<Result> image(@Argument final String id) {
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
  public Mono<List<Image>> images() {
    return repository.findAll().map(mapper::toDto).collect(Collectors.toList());
  }
}
