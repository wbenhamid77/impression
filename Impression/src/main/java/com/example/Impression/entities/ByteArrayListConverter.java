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
public class ByteArrayListConverter implements AttributeConverter<List<byte[]>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<byte[]> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            // Convertir les byte[] en base64 pour le stockage JSON
            List<String> base64List = new ArrayList<>();
            for (byte[] bytes : attribute) {
                if (bytes != null) {
                    base64List.add(java.util.Base64.getEncoder().encodeToString(bytes));
                }
            }
            return objectMapper.writeValueAsString(base64List);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la conversion de la liste en JSON", e);
        }
    }

    @Override
    public List<byte[]> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            List<String> base64List = objectMapper.readValue(dbData, new TypeReference<List<String>>() {
            });
            List<byte[]> result = new ArrayList<>();
            for (String base64 : base64List) {
                if (base64 != null && !base64.isEmpty()) {
                    result.add(java.util.Base64.getDecoder().decode(base64));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la conversion du JSON en liste", e);
        }
    }
}