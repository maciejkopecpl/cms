package pl.maciejkopec.cms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record Mail(
    @JsonProperty("from") @Email @NotBlank String from,
    @JsonProperty("name") @NotBlank String name,
    @JsonProperty("message") @NotBlank String message,
    @JsonProperty("token") @NotBlank String token) {

}
