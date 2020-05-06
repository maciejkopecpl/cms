package pl.maciejkopec.cms.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.handler.ImageHandler;

@Configuration
public class ImageRouter {

  private static final MediaType ANY_IMAGE = MediaType.parseMediaType("image/*");

  @Bean
  public RouterFunction<ServerResponse> imageRoutes(final ImageHandler imageHandler) {

    return route()
        .path(
            "/images",
            builder -> {
              builder
                  .route(GET("/{id}").and(accept(ANY_IMAGE)), imageHandler::showImage)
                  .route(POST("/").and(accept(MULTIPART_FORM_DATA)), imageHandler::create)
                  .nest(
                      accept(APPLICATION_JSON),
                      routes ->
                          routes
                              .route(GET("/{id}"), imageHandler::get)
                              .route(DELETE("/{id}"), imageHandler::delete)
                              .route(PUT("/{id}"), imageHandler::update)
                              .route(GET("/"), imageHandler::findAll));
            })
        .build();
  }
}
