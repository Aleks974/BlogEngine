package diplom.blogengine.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InitOptionsResponse {
    private String title;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;
}
