package pl.maciejkopec.cms.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import pl.maciejkopec.cms.domain.ModuleType;
import pl.maciejkopec.cms.dto.graphql.Result;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Module.ModuleBuilder.class)
public class Module implements Dto<String>, Result {
  String id;
  ModuleType type;
  String title;
  String data;
  @Builder.Default Integer order = 0;
}
