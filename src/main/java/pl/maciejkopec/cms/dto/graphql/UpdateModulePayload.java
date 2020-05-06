package pl.maciejkopec.cms.dto.graphql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import pl.maciejkopec.cms.dto.Module;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = UpdateModulePayload.UpdateModulePayloadBuilder.class)
public class UpdateModulePayload {
  String id;
  Module module;
  Status status;
}
