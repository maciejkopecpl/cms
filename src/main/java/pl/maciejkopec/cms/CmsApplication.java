package pl.maciejkopec.cms;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;
import pl.maciejkopec.cms.configuration.ApplicationProperties;

@SpringBootApplication(scanBasePackages = {"pl.maciejkopec.cms"})
@EnableWebFlux
@EnableConfigurationProperties(ApplicationProperties.class)
public class CmsApplication {

  public static void main(final String[] args) {
    ApplicationInsights.attach();
    SpringApplication.run(CmsApplication.class, args);
  }
}
