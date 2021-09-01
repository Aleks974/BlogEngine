package diplom.blogengine.service.converter;

import diplom.blogengine.service.sort.PostSortMode;
import org.springframework.core.convert.converter.Converter;

public class PostSortModeConverter implements Converter<String, PostSortMode> {

    @Override
    public PostSortMode convert(String s) {
        try {
            return PostSortMode.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Request parameter has invalid value");
        }
    }
}
