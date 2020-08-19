package pl.maciejkopec.cms.configuration;

import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthenticationToken extends AbstractAuthenticationToken {

  public static final String AUTHORIZED_CLIENT_ROLE = "ROLE_AUTHORIZED_CLIENT";
  private final String principal;
  private final String credentials;

  public AuthenticationToken(
      final String principal, final String credentials, final boolean authenticated) {
    super(List.of(new SimpleGrantedAuthority(AUTHORIZED_CLIENT_ROLE)));
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(authenticated);
  }

  public static AuthenticationToken authenticated(final Authentication authentication) {
    return new AuthenticationToken(
        authentication.getPrincipal().toString(), authentication.getCredentials().toString(), true);
  }

  public static AuthenticationToken preAuthenticated(
      final String principal, final String credentials) {
    return new AuthenticationToken(principal, credentials, false);
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
