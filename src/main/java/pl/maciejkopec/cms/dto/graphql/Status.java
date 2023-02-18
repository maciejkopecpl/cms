package pl.maciejkopec.cms.dto.graphql;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class Status implements Result {

  Integer status;
  String message;
}
