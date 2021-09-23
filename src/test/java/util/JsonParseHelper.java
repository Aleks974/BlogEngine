package util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonParseHelper {

    private final ObjectMapper objectMapper;

    public JsonParseHelper() {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public <T> T retrieveResourceFromJson(String jsonStr, Class<T> clazz) throws IOException {
        return objectMapper.readValue(jsonStr, clazz);
    }

    public String convertToString(Object element) throws IOException {
        return objectMapper.writeValueAsString(element);
    }

}
