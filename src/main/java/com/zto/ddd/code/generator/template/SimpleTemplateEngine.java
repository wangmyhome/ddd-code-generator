package com.zto.ddd.code.generator.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Simple template engine using pure Java for template variable replacement
 */
public class SimpleTemplateEngine {
    
    /**
     * Load template from resource file
     * @param templatePath template path
     * @return template content
     * @throws IOException if read fails
     */
    public static String loadTemplate(String templatePath, ClassLoader classLoader) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(templatePath);
        if (inputStream == null) {
            throw new IOException("Template not found: " + templatePath);
        }
        
        StringBuilder templateContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                templateContent.append(line).append("\n");
            }
        }
        
        return templateContent.toString();
    }
    
    /**
     * Process template, replace variables
     * @param template template content
     * @param variables variable mapping
     * @return processed content
     */
    public static String process(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            result = result.replace("${" + key + "}", value != null ? value.toString() : "");
        }
        return result;
    }
} 