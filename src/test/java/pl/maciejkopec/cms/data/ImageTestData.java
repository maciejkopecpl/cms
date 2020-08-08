package pl.maciejkopec.cms.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageTestData {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Dto {

    public static Image minimum() {
      return Image.builder().id("id").build();
    }

    public static Image valid() {
      return minimum().toBuilder().filename("filename").image("image_id").build();
    }

    public static Image notSaved() {
      return valid().toBuilder().id(null).build();
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Document {

    public static ImageDocument minimum() {
      return ImageDocument.builder().id("id").build();
    }

    public static ImageDocument valid() {
      return minimum().toBuilder().filename("filename").image("image_id").build();
    }

    public static ImageDocument notSaved() {
      return valid().toBuilder().id(null).build();
    }
  }
}
