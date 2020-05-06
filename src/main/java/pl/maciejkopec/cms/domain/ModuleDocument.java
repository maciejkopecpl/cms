package pl.maciejkopec.cms.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("modules")
@Value
@Builder(toBuilder = true)
public class ModuleDocument implements Domain<String> {
  @Id String id;
  ModuleType type;
  String title;
  String data;
  Integer order;
}
