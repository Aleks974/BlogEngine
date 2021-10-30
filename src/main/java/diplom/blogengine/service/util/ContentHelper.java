package diplom.blogengine.service.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentHelper {
    private final Pattern ALL_TAGS_PATTERN = Pattern.compile("(?i)</?[a-z0-9]+.*?/?>");
    private final Pattern PROHIBITED_TAGS_PATTERN;
    private final String EMPTY_STR = "";

    public ContentHelper(String prohibitedTags) {
        PROHIBITED_TAGS_PATTERN = Pattern.compile("<(" + prohibitedTags + ").*?((</\\1>)|(/>))");
    }

    public String clearAllTags(String content) {
        return removeTags(removeProhibitedTags(content));
    }

    public String clearTags(String content) {
        return removeTags(content);
    }

    public String clearProhibitedTags(String content) {
        return removeProhibitedTags(content);
    }

    private String removeProhibitedTags(String content) {
        Matcher matcher = PROHIBITED_TAGS_PATTERN.matcher(content);
        return matcher.replaceAll(EMPTY_STR);
    }

    private String removeTags(String content) {
        Matcher matcher = ALL_TAGS_PATTERN.matcher(content);
        return matcher.replaceAll(EMPTY_STR);
    }
}
