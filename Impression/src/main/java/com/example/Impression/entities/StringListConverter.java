package com.example.Impression.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la conversion de la liste en JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }

        String trimmed = dbData.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            return new ArrayList<>();
        }

        try {
            // Cas standard: JSON array
            return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {
            });
        } catch (IOException parseException) {
            // Fallback tolérant: valeurs historiques non-JSON (ex: "Football,Handball" ou
            // "Football")
            try {
                // Si c'est une simple chaîne JSON ("Football")
                String single = objectMapper.readValue(trimmed, String.class);
                List<String> one = new ArrayList<>();
                if (single != null && !single.isEmpty()) {
                    one.add(single);
                }
                return one;
            } catch (IOException ignore) {
                // Dernier repli: découper par virgule et nettoyer
                List<String> list = new ArrayList<>();
                for (String part : trimmed.split(",")) {
                    String token = part.trim();
                    if (token.startsWith("[") && token.endsWith("]")) {
                        token = token.substring(1, token.length() - 1).trim();
                    }
                    if (token.startsWith("\"") && token.endsWith("\"")) {
                        token = token.substring(1, token.length() - 1);
                    }
                    if (!token.isEmpty() && !"null".equalsIgnoreCase(token)) {
                        list.add(token);
                    }
                }
                return list;
            }
        }
    }
}