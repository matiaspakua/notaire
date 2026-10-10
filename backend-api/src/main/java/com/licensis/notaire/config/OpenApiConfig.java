package com.licensis.notaire.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        var errorSchema = new Schema<>()
                .addProperty("error", new Schema<String>().type("string"))
                .addProperty("message", new Schema<String>().type("string"))
                .addProperty("timestamp", new Schema<String>().type("string").format("date-time"))
                .addProperty("path", new Schema<String>().type("string"));

        var jsonContent = new Content().addMediaType(
                "application/json", new MediaType().schema(errorSchema));

        return new OpenAPI()
                .info(new Info()
                        .title("Notaire API")
                        .version("1.0.0")
                        .description("Sistema of management notarial — API REST backend. "
                                + "Todos los recursos siguen el prefijo `/api/v1/`. "
                                + "The respuestas of error remain the esquema "
                                + "`{error, message, timestamp, path}`.")
                        .contact(new Contact()
                                .name("Licensis")
                                .url("https://www.licensis.com")
                                .email("info@licensis.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desarrollo local"),
                        new Server().url("http://localhost:8081").description("Docker local")
                ))
                .components(new Components()
                        .addResponses("400", new ApiResponse()
                                .description("Invalid request — parámetros or cuerpo malformado")
                                .content(jsonContent))
                        .addResponses("404", new ApiResponse()
                                .description("Recurso no encontrado")
                                .content(jsonContent))
                        .addResponses("409", new ApiResponse()
                                .description("Conflicto — the recurso already exists or viola a restricción")
                                .content(jsonContent))
                        .addResponses("500", new ApiResponse()
                                .description("Error interno of the server")
                                .content(jsonContent))
                );
    }
}
