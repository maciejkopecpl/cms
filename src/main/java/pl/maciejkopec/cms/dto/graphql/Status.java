package pl.maciejkopec.cms.dto.graphql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Status.StatusBuilder.class)
public class Status implements Result {
  Integer status;
  String message;
}
