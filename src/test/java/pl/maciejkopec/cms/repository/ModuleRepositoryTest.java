package pl.maciejkopec.cms.repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.maciejkopec.cms.domain.ModuleDocument;

import static pl.maciejkopec.cms.data.ModuleTestData.Document;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class ModuleRepositoryTest
    extends CrudRepositoryTest<ModuleDocument, String, ModuleRepository> {

  @Autowired private ModuleRepository repository;

  @Override
  public ModuleRepository repository() {
    return repository;
  }

  @Override
  public ModuleDocument module() {
    return Document.valid();
  }

  @Override
  public ModuleDocument update(final ModuleDocument module) {
    return module.toBuilder()
        .title("updated")
        .data("{ \"latitude\": 37.774929, \"longitude\": -122.419418 }")
        .build();
  }
}
