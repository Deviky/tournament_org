package com.deviky.Match_Service.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MatchStatusConverter implements AttributeConverter<MatchStatus, String> {

    @Override
    public String convertToDatabaseColumn(MatchStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public MatchStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim();
        if (normalized.chars().allMatch(Character::isDigit)) {
            int ordinal = Integer.parseInt(normalized);
            MatchStatus[] values = MatchStatus.values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
        }

        return MatchStatus.valueOf(normalized.toUpperCase());
    }
}
