package diplom.blogengine.service.converter;

import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.service.PostSortMode;
import org.springframework.core.convert.converter.Converter;

public class ModerationStatusConverter implements Converter<String, ModerationStatus> {

    @Override
    public ModerationStatus convert(String s) {
        try {
            return ModerationStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Request parameter has invalid value");
        }
    }
}
