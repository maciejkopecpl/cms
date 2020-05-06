package pl.maciejkopec.cms.configuration;

import static java.util.List.of;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http.csrf()
        .disable()
        .cors()
        .and()
        .authorizeExchange()
        .anyExchange()
        .permitAll()
        .and()
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${application.allowed-origins}") final List<String> allowedOrigins) {
    final var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(of("GET", "POST"));
    configuration.setAllowedHeaders(of("Content-Type"));

    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
