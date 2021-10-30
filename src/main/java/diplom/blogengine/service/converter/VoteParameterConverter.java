package diplom.blogengine.service.converter;

import diplom.blogengine.service.VoteParameter;
import org.springframework.core.convert.converter.Converter;

public class VoteParameterConverter implements Converter<String, VoteParameter> {
    @Override
    public VoteParameter convert(String s) {
        try {
            return VoteParameter.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("vote.parameterIncorrect");
        }
    }
}