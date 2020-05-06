package pl.maciejkopec.cms.configuration;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import graphql.schema.idl.NaturalEnumValuesProvider;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import pl.maciejkopec.cms.domain.ModuleType;

@Configuration
public class JacksonConfiguration implements WebFluxConfigurer {

  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder()
        .annotationIntrospector(
            new JacksonAnnotationIntrospector() {
              @Override
              public JsonPOJOBuilder.Value findPOJOBuilderConfig(final AnnotatedClass ac) {
                if (ac.hasAnnotation(JsonPOJOBuilder.class)) {
                  return super.findPOJOBuilderConfig(ac);
                }
                return new JsonPOJOBuilder.Value("build", "");
              }
            });
  }

  @Override
  public void configureHttpMessageCodecs(final ServerCodecConfigurer configurer) {
    final var objectMapper = jackson2ObjectMapperBuilder().build();
    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    ModuleType.CONTACT_FORM.name();
  }

  @Bean
  public RuntimeWiring.Builder runtimeWiring() {
    final NaturalEnumValuesProvider moduleValues = new NaturalEnumValuesProvider(ModuleType.class);

    return RuntimeWiring.newRuntimeWiring()
        .type("ModuleType", typeWiring -> typeWiring.enumValues(moduleValues));
  }
}
