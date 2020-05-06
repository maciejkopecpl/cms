package pl.maciejkopec.cms.repository;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CommonMongoOperations {
  private final ReactiveMongoTemplate mongoTemplate;

  @NotNull
  public <T> Mono<T> getAndReplace(final T document, final Query query, final Class<T> clazz) {
    return mongoTemplate.update(clazz).matching(query).replaceWith(document).findAndReplace();
  }
}
