package diplom.blogengine.exception;

import lombok.Getter;

@Getter
public class InputParameterException extends RuntimeException {
    private String param;
    private Object value;

    public InputParameterException(String msg) {
        super(msg);
    }

    public InputParameterException(String msg, Object value) {
        this(msg, null, value);
    }

    public InputParameterException(String msg, String param, Object value) {
        this(msg);
        this.param = param;
        this.value = value;
    }

    public String getParam() {
        return param;
    }

    public Object getValue() {
        return value;
    }
}
