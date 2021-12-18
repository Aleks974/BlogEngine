package diplom.blogengine.service.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentHelper {
    private final Pattern ALL_TAGS_PATTERN = Pattern.compile("(?i)</?[a-z0-9]+.*?/?>");
    private final Pattern PAIRED_TAGS_PATTERN = Pattern.compile("(?i)<(script|object|head).*?((</\\1>)|(/>))"); // использование захватываемых групп \\1
    private final Pattern ALL_TAGS_EXCEPT_PERMITTED_PATTERN;
    private final Pattern EXTRA_SPACES = Pattern.compile("\\s{2,}");
    private final Pattern NBSP = Pattern.compile("&nbsp;+");
    private final String EMPTY_STR = "";
    private final String ONE_SPACE = " ";

    public ContentHelper(String permittedTags) {
        ALL_TAGS_EXCEPT_PERMITTED_PATTERN = Pattern.compile("(?i)</?(?=[a-z0-9]+)(?!" + permittedTags + ").*?/?>"); // цепочка условий - сначала совпадения для первой скобки, потом для второй. Во второй - как раз условие Не равно
    }

    public String clearAllTags(String content) {
        return removeAllTags(content);
    }

    public String clearAllTagsExceptPermitted(String content) {
        return removeAllTagsExceptPermitted(content);
    }

    private String removeAllTags(String content) {
        content = clearPairedTags(content);
        return ALL_TAGS_PATTERN.matcher(content).replaceAll(EMPTY_STR);
    }

    private String removeAllTagsExceptPermitted(String content) {
        content = clearPairedTags(content);
        return ALL_TAGS_EXCEPT_PERMITTED_PATTERN.matcher(content).replaceAll(EMPTY_STR);
    }

    private String clearPairedTags(String content) {
        return PAIRED_TAGS_PATTERN.matcher(content).replaceAll(EMPTY_STR);
    }

    public String replaceSpacesAndNbspWithOneSpace(String content) {
        String replacedSpaces = EXTRA_SPACES.matcher(content).replaceAll(ONE_SPACE);
        return NBSP.matcher(replacedSpaces).replaceAll(ONE_SPACE);
    }
}
