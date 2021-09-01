package diplom.blogengine.service.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContentProcessor implements IContentProcessor {
    private final Pattern ALL_TAGS_PATTERN;

    public ContentProcessor() {
        ALL_TAGS_PATTERN = Pattern.compile("(?i)</?[a-z]+[^<>]*?/?>");
    }

    @Override
    public String clearAllTags(String content) {
        Matcher matcher = ALL_TAGS_PATTERN.matcher(content);
        return matcher.replaceAll("");
    }

}
