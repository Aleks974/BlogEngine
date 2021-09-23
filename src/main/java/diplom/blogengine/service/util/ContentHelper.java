package diplom.blogengine.service.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentHelper {
    private final Pattern ALL_HTML_TAGS_PATTERN;

    public ContentHelper() {
        ALL_HTML_TAGS_PATTERN = Pattern.compile("(?i)</?[a-z]+[^<>]*?/?>");
    }

    public String clearHtml(String content) {
        Matcher matcher = ALL_HTML_TAGS_PATTERN.matcher(content);
        return matcher.replaceAll("");
    }

}
