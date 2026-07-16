package com.codewithkahlil.backend.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts Map<String conversationTitle, LinkedHashMap<String question, String response>>
 * to/from a single JSON text column, since JPA cannot map a nested Map directly.
 */
@Converter
public class ConversationMapConverter implements AttributeConverter<Map<String, LinkedHashMap<String, String>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, LinkedHashMap<String, String>>> TYPE =
            new TypeReference<>() {
            };

    @Override
    public String convertToDatabaseColumn(Map<String, LinkedHashMap<String, String>> attribute) {
        try {
            return attribute == null ? "{}" : MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize conversations to JSON", e);
        }
    }

    @Override
    public Map<String, LinkedHashMap<String, String>> convertToEntityAttribute(String dbData) {
        try {
            return (dbData == null || dbData.isBlank())
                    ? new LinkedHashMap<>()
                    : MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize conversations from JSON", e);
        }
    }
}
