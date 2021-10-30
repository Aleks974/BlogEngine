package diplom.blogengine.service.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import diplom.blogengine.exception.InputParameterException;
import diplom.blogengine.service.ModerationDecision;

import java.io.IOException;

public class ModerationDecisionDeserializer extends JsonDeserializer<ModerationDecision> {
    @Override
    public ModerationDecision deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                                        throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        if (node == null) {
            return null;
        }

        String str = node.textValue();
        if (str == null) {
            return null;
        }

        try {
            return ModerationDecision.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InputParameterException("moderation.parameterIncorrect");
        }
    }


}
