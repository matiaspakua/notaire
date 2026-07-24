package com.licensis.notaire.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductionCredentialsGuard {

    private static final String PRODUCTION_ENVIRONMENT = "production";
    private static final String INSECURE_DEFAULT_VALUE = "admin";

    @Value("${app.environment:development}")
    private String environment;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${actuator.security.username}")
    private String actuatorUsername;

    @Value("${actuator.security.password}")
    private String actuatorPassword;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    @Value("${pgadmin.admin.password:admin}")
    private String pgAdminPassword;

    @Value("${grafana.admin.username:admin}")
    private String grafanaUsername;

    @Value("${grafana.admin.password:admin}")
    private String grafanaPassword;

    @PostConstruct
    public void validateCredentials() {
        if (!PRODUCTION_ENVIRONMENT.equalsIgnoreCase(environment)) {
            return;
        }

        List<String> insecureProperties = new ArrayList<>();
        addIfDefault(insecureProperties, "spring.datasource.username", datasourceUsername);
        addIfDefault(insecureProperties, "spring.datasource.password", datasourcePassword);
        addIfDefault(insecureProperties, "actuator.security.username", actuatorUsername);
        addIfDefault(insecureProperties, "actuator.security.password", actuatorPassword);
        addIfDefault(insecureProperties, "app.admin.username", adminUsername);
        addIfDefault(insecureProperties, "app.admin.password", adminPassword);
        addIfDefault(insecureProperties, "pgadmin.admin.password", pgAdminPassword);
        addIfDefault(insecureProperties, "grafana.admin.username", grafanaUsername);
        addIfDefault(insecureProperties, "grafana.admin.password", grafanaPassword);

        if (!insecureProperties.isEmpty()) {
            throw new IllegalStateException(
                    "Credenciales inseguras detectadas en producción: " + String.join(", ", insecureProperties)
                    + ". Configurá valores propios antes de iniciar la aplicación en este entorno.");
        }
    }

    private void addIfDefault(List<String> insecureProperties, String propertyName, String value) {
        if (INSECURE_DEFAULT_VALUE.equals(value)) {
            insecureProperties.add(propertyName);
        }
    }
}
