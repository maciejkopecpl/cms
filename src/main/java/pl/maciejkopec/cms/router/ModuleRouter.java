package pl.maciejkopec.cms.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.handler.ModuleHandler;

@Configuration
public class ModuleRouter {

  @Bean
  public RouterFunction<ServerResponse> moduleRoutes(final ModuleHandler moduleHandler) {

    return route()
        .path(
            "/modules",
            builder ->
                builder.nest(
                    accept(APPLICATION_JSON),
                    routes ->
                        routes
                            .GET("/{id}", moduleHandler::findById)
                            .GET("/{id}/", moduleHandler::findById)
                            .DELETE("/{id}", moduleHandler::deleteById)
                            .DELETE("/{id}/", moduleHandler::deleteById)
                            .DELETE("/", moduleHandler::deleteAll)
                            .DELETE("", moduleHandler::deleteAll)
                            .PUT("/{id}", moduleHandler::update)
                            .PUT("/{id}/", moduleHandler::update)
                            .GET("/", moduleHandler::findAll)
                            .GET("", moduleHandler::findAll)
                            .POST("/", moduleHandler::save)
                            .POST("", moduleHandler::save)
                            .POST("/bulk", moduleHandler::saveBulk))
                            .POST("/bulk/", moduleHandler::saveBulk))
        .build();
  }
}
