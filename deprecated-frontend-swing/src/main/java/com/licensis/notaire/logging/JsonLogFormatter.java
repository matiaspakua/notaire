package com.licensis.notaire.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatter JSON estructurado compatible con formato Prometheus/OpenMetrics.
 * 
 * Cada línea de log se emite como un objeto JSON con los siguientes campos:
 * <ul>
 * <li><b>timestamp</b> - ISO 8601 con zona UTC (ej:
 * 2026-02-27T19:18:28.123Z)</li>
 * <li><b>level</b> - Nivel de severidad del log (INFO, WARNING, SEVERE,
 * etc.)</li>
 * <li><b>logger</b> - Nombre del logger (clase de origen)</li>
 * <li><b>message</b> - Mensaje descriptivo del evento</li>
 * <li><b>thread</b> - ID del hilo que generó el log</li>
 * <li><b>source_class</b> - Clase donde se originó el log</li>
 * <li><b>source_method</b> - Método donde se originó el log</li>
 * <li><b>application</b> - Nombre de la aplicación (siempre
 * "notaire-frontend")</li>
 * <li><b>module</b> - Módulo de la aplicación (siempre "frontend-swing")</li>
 * <li><b>exception</b> - (opcional) Stack trace completo si hubo una
 * excepción</li>
 * <li><b>exception_class</b> - (opcional) Clase de la excepción</li>
 * <li><b>exception_message</b> - (opcional) Mensaje de la excepción</li>
 * </ul>
 *
 * @author matias
 */
public class JsonLogFormatter extends Formatter {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private static final String APPLICATION_NAME = "notaire-frontend";
    private static final String MODULE_NAME = "frontend-swing";

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');

        // timestamp en ISO 8601 UTC
        appendField(sb, "timestamp",
                ISO_FORMATTER.format(Instant.ofEpochMilli(record.getMillis())));
        sb.append(',');

        // level
        appendField(sb, "level", record.getLevel().getName());
        sb.append(',');

        // logger
        appendField(sb, "logger",
                record.getLoggerName() != null ? record.getLoggerName() : "unknown");
        sb.append(',');

        // message
        String message = formatMessage(record);
        appendField(sb, "message", escapeJson(message != null ? message : ""));
        sb.append(',');

        // thread
        sb.append("\"thread\":").append(record.getLongThreadID());
        sb.append(',');

        // source_class
        appendField(sb, "source_class",
                record.getSourceClassName() != null ? record.getSourceClassName() : "unknown");
        sb.append(',');

        // source_method
        appendField(sb, "source_method",
                record.getSourceMethodName() != null ? record.getSourceMethodName() : "unknown");
        sb.append(',');

        // application y module (labels estilo Prometheus)
        appendField(sb, "application", APPLICATION_NAME);
        sb.append(',');
        appendField(sb, "module", MODULE_NAME);

        // exception (si existe)
        if (record.getThrown() != null) {
            Throwable thrown = record.getThrown();
            sb.append(',');
            appendField(sb, "exception_class", thrown.getClass().getName());
            sb.append(',');
            appendField(sb, "exception_message",
                    escapeJson(thrown.getMessage() != null ? thrown.getMessage() : ""));
            sb.append(',');

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            thrown.printStackTrace(pw);
            pw.flush();
            appendField(sb, "exception", escapeJson(sw.toString()));
        }

        sb.append('}');
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    private void appendField(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":\"").append(value).append('"');
    }

    /**
     * Escapa caracteres especiales para JSON válido.
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
