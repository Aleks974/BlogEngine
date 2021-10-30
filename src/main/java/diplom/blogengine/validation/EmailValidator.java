package diplom.blogengine.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<EmailConstraint, String> {
    private static final String EMAIL_PATTERN = "^[0-9a-z._-]+@([0-9a-z]+[-]*[0-9a-z]+.)+[a-z]{2,}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override    public void initialize(EmailConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }
        return pattern.matcher(s).matches();
    }
}
