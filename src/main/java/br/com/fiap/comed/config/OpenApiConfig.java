package br.com.fiap.comed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI comedOpenApi(@Value("${comed.versao}") String versao, @Value("${comed.ambiente}") String ambiente) {
        return new OpenAPI().info(new Info()
                .title("CoMed ESG API")
                .version(versao)
                .description("Copiloto de IA clínica com modelo de dados ESG em MongoDB. Ambiente: " + ambiente));
    }
}
