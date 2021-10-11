package diplom.blogengine.service.converter;

import diplom.blogengine.service.MyPostStatus;
import diplom.blogengine.service.PostSortMode;
import org.springframework.core.convert.converter.Converter;

public class MyPostStatusConverter implements Converter<String, MyPostStatus> {

    @Override
    public MyPostStatus convert(String s) {
        try {
            return MyPostStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Request parameter has invalid value");
        }
    }
}
