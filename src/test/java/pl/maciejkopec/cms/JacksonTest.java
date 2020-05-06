package pl.maciejkopec.cms;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.maciejkopec.cms.configuration.JacksonConfiguration;
import pl.maciejkopec.cms.domain.ModuleType;
import pl.maciejkopec.cms.dto.Mail;
import pl.maciejkopec.cms.dto.Module;

@ExtendWith(MockitoExtension.class)
public class JacksonTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    final JacksonConfiguration jackson2ObjectMapperBuilder = new JacksonConfiguration();
    objectMapper = jackson2ObjectMapperBuilder.jackson2ObjectMapperBuilder().build();
  }

  @Test
  void shouldSupportRecord() throws JsonProcessingException {
    final var mail = new Mail("a", "b", "c", "d");
    final var result = objectMapper.writeValueAsString(mail);

    assertThat(result).isEqualToIgnoringWhitespace(
        """
              {
                  "from": "a",
                  "name": "b",
                  "message": "c",
                  "token": "d"
              }
            """
    );

    assertThat(objectMapper.readValue(result, Mail.class)).isEqualTo(mail);
  }

  @Test
  public void testSerialization() throws JsonProcessingException {
    final Module module = Module.builder()
            .id("test")
            .type(ModuleType.CONTACT_FORM)
            .data("{\"latitude\": 37.774929, \"longitude\": -122.419418}")
            .build();
    final String serialized = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(module);

    final String expectedJson = """
            {
              "id" : "test",
              "type" : "CONTACT_FORM",
              "title" : null,
              "data" : "{\\"latitude\\": 37.774929, \\"longitude\\": -122.419418}",
              "order" : 0
            }
            """;

    assertThat(serialized).isEqualToIgnoringWhitespace(expectedJson);

    final Module moduleDto = objectMapper.readValue(expectedJson, Module.class);

    assertThat(moduleDto.getId()).isEqualTo("test");
  }

}
