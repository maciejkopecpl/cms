package pl.maciejkopec.cms.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class TestAppConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GraphQLTestTemplate graphQLTestUtils(
            final ResourceLoader resourceLoader,
            @Autowired(required = false) final TestRestTemplate restTemplate,
            @Value("${graphql.servlet.mapping:/graphql}") final String graphqlMapping,
            final ObjectMapper objectMapper) {
        return new GraphQLTestTemplate(resourceLoader, restTemplate, graphqlMapping, objectMapper);
    }
}
