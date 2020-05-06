package pl.maciejkopec.cms.repository;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.maciejkopec.cms.domain.ImageDocument;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class ImageRepositoryTest
    extends CrudRepositoryTest<ImageDocument, String, ImageRepository> {

  @Autowired private ImageRepository repository;

  @Override
  public ImageRepository repository() {
    return repository;
  }

  @Override
  public ImageDocument module() {
    return ImageDocument.builder().image(ObjectId.get().toHexString()).alt("test").build();
  }

  @Override
  public ImageDocument update(final ImageDocument module) {
    return module.toBuilder().alt("updated").build();
  }
}
