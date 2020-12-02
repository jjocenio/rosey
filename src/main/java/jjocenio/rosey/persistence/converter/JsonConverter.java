package jjocenio.rosey.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new PersistenceException("Unable to convert value", ex);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (IOException ex) {
            throw new PersistenceException("Unable to convert value", ex);
        }
    }

}