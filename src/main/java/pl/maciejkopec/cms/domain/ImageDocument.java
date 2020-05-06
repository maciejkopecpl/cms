package pl.maciejkopec.cms.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("images")
@Value
@Builder(toBuilder = true)
public class ImageDocument implements Domain<String> {
  @Id String id;
  String image;
  String alt;
}
