package pl.maciejkopec.cms.dto.graphql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import pl.maciejkopec.cms.dto.Module;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = CreateModulePayload.CreateModulePayloadBuilder.class)
public class CreateModulePayload {
  String id;
  Module module;
  Status status;
}
