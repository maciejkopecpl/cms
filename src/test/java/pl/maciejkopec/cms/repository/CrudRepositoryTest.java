package pl.maciejkopec.cms.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pl.maciejkopec.cms.domain.Domain;
import reactor.test.StepVerifier;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public abstract class CrudRepositoryTest<
    M extends Domain<ID>, ID, R extends ReactiveMongoRepository<M, ID>> {

  public abstract R repository();

  public abstract M module();

  public abstract M update(M module);

  @Test
  public void testCrud() {
    final var module = module();

    // Create
    log.info("Creating module");
    final var savedModule = repository().save(module).block();

    log.info(
        "Documents in database: `{}`", repository().findAll().collect(Collectors.toList()).block());

    // Read
    log.info("Reading module");
    assertThat(savedModule).isNotNull();
    final var readModule = repository().findById(savedModule.getId());

    StepVerifier.create(readModule)
        .assertNext(item -> assertThat(item).isEqualTo(savedModule))
        .expectComplete()
        .verify();

    // Update
    log.info("Updating module");
    final var updatedModule = repository().save(update(savedModule)).block();

    assertThat(updatedModule).isNotNull();
    StepVerifier.create(repository().findById(updatedModule.getId()))
        .assertNext(item -> assertThat(item).isNotEqualTo(savedModule))
        .expectComplete()
        .verify();

    log.info(
        "Documents in database: `{}`", repository().findAll().collect(Collectors.toList()).block());

    // DELETE
    log.info("Deleting module");
    repository().delete(updatedModule).block();

    final var deletedModule = repository().findById(updatedModule.getId());

    StepVerifier.create(deletedModule).verifyComplete();

    log.info(
        "Documents in database: `{}`", repository().findAll().collect(Collectors.toList()).block());
  }
}
