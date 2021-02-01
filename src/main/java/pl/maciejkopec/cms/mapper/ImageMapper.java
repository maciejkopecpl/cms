package pl.maciejkopec.cms.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.maciejkopec.cms.domain.ImageDocument;
import pl.maciejkopec.cms.dto.Image;
import pl.maciejkopec.cms.dto.SavedImage;
import pl.maciejkopec.cms.dto.graphql.CreateImagePayload;
import pl.maciejkopec.cms.dto.graphql.UpdateImagePayload;

@Mapper
public interface ImageMapper extends BaseMapper<Image, ImageDocument> {

  @Override
  ImageDocument toDomain(Image imageDto);

  @Override
  Image toDto(ImageDocument image);

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "objectId", target = "image", qualifiedByName = "objectIdToString")
  ImageDocument toDomain(SavedImage image);

  @Named("objectIdToString")
  static String objectIdToString(final ObjectId objectId) {
    return objectId.toHexString();
  }

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "image", source = ".")
  CreateImagePayload toCreatePayload(ImageDocument module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "image", source = ".")
  CreateImagePayload toCreatePayload(Image module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "image", source = ".")
  UpdateImagePayload toUpdatePayload(ImageDocument module);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "image", source = ".")
  UpdateImagePayload toUpdatePayload(Image module);
}
