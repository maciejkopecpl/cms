package pl.maciejkopec.cms.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;
import pl.maciejkopec.cms.dto.graphql.Result;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Image.ImageBuilder.class)
public class Image implements Dto<String>, Result {
  String id;
  String image;
  String filename;
}
