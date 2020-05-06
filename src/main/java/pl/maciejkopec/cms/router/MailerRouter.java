package pl.maciejkopec.cms.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.handler.MailerHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MailerRouter {

  @Bean
  public RouterFunction<ServerResponse> mailerRoutes(final MailerHandler mailerHandler) {

    return route().POST("/mailer", mailerHandler::send).build();
  }
}
