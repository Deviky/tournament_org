package com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class AlgorithmParams {

    // Валидация на основе аннотаций
    public void validate() {
        for (Field field : this.getClass().getDeclaredFields()) {
            AlgorithmParamConfig meta = field.getAnnotation(AlgorithmParamConfig.class);
            if (meta == null) continue;

            field.setAccessible(true);
            Object value;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (meta.required() && value == null) {
                throw new IllegalArgumentException(field.getName() + " is required");
            }

            if (value instanceof String s) {
                if (s.length() < meta.minLength())
                    throw new IllegalArgumentException(field.getName() + " length < " + meta.minLength());
                if (s.length() > meta.maxLength())
                    throw new IllegalArgumentException(field.getName() + " length > " + meta.maxLength());
                if (meta.allowedValues().length > 0 && !Arrays.asList(meta.allowedValues()).contains(s))
                    throw new IllegalArgumentException(field.getName() + " value not allowed");
            }

            if (value instanceof Number n) {
                if (n.longValue() < meta.min())
                    throw new IllegalArgumentException(field.getName() + " < " + meta.min());
                if (n.longValue() > meta.max())
                    throw new IllegalArgumentException(field.getName() + " > " + meta.max());
            }
        }
    }

    // JSON descriptor
    public ObjectNode getJsonView() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        for (Field field : this.getClass().getDeclaredFields()) {
            AlgorithmParamConfig meta = field.getAnnotation(AlgorithmParamConfig.class);
            if (meta == null) continue;

            ObjectNode fieldNode = mapper.createObjectNode();
            fieldNode.put("name", field.getName());
            fieldNode.put("type", field.getType().getSimpleName());
            fieldNode.put("label", meta.label());
            fieldNode.put("required", meta.required());
            fieldNode.put("description", meta.description());
            fieldNode.put("defaultValue", meta.defaultValue());
            fieldNode.put("min", meta.min());
            fieldNode.put("max", meta.max());
            fieldNode.put("minLength", meta.minLength());
            fieldNode.put("maxLength", meta.maxLength());

            // allowed values
            if (meta.allowedValues().length > 0) {
                fieldNode.putPOJO("allowedValues", meta.allowedValues());
            }

            root.set(field.getName(), fieldNode);
        }

        return root;
    }
}
