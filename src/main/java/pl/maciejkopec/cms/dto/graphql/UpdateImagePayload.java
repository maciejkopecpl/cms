package pl.maciejkopec.cms.dto.graphql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import pl.maciejkopec.cms.dto.Image;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class UpdateImagePayload {
  String id;
  Image image;
  Status status;
}
