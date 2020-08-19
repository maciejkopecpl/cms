package pl.maciejkopec.cms.configuration;

import static java.util.List.of;
import static java.util.Objects.requireNonNullElse;
import static pl.maciejkopec.cms.configuration.AuthenticationToken.preAuthenticated;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  private final String apiKey;

  public SecurityConfiguration(@Value("${application.api-key}") final String apiKey) {
    this.apiKey = apiKey;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http.csrf()
        .disable()
        .cors()
        .and()
        .csrf()
        .disable()
        .authorizeExchange()
        .pathMatchers("/mailer")
        .permitAll()
        .and()
        .authorizeExchange()
        .anyExchange()
        .authenticated()
        .and()
        .addFilterAt(securityWebFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
        .build();
  }

  @Bean
  public AuthenticationWebFilter securityWebFilter() {
    final AuthenticationWebFilter authenticationWebFilter =
        new AuthenticationWebFilter(this::checkApiKey);
    authenticationWebFilter.setRequiresAuthenticationMatcher(this::requireAuthorizationHeader);
    authenticationWebFilter.setServerAuthenticationConverter(this::preAuthenticate);
    authenticationWebFilter.setSecurityContextRepository(
        new WebSessionServerSecurityContextRepository());
    return authenticationWebFilter;
  }

  @NotNull
  private Mono<Authentication> preAuthenticate(final ServerWebExchange exchange) {
    return Mono.just(
        preAuthenticated(
            exchange.getRequest().getId(),
            exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)));
  }

  private Mono<MatchResult> requireAuthorizationHeader(final ServerWebExchange exchange) {
    return requireNonNullElse(
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION), "")
            .isBlank()
        ? MatchResult.notMatch()
        : MatchResult.match();
  }

  @NotNull
  private Mono<Authentication> checkApiKey(final Authentication authentication) {
    return apiKey.equals(authentication.getCredentials())
        ? Mono.just(AuthenticationToken.authenticated(authentication))
        : Mono.error(new BadCredentialsException("Invalid API key"));
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
