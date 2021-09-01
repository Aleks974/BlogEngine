package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.InitOptionsResponse;
import diplom.blogengine.config.BlogSettings;
import org.springframework.stereotype.Component;

@Component
public class InitOptionsResponseMapper {
    private final Object lock = new Object();
    private final BlogSettings blogSettings;
    private volatile InitOptionsResponse initOptionsResponse;


    public InitOptionsResponseMapper(BlogSettings blogSettings) {
        this.blogSettings = blogSettings;
    }

    public InitOptionsResponse initOptionsResponse() {
        InitOptionsResponse response = initOptionsResponse;
        if (response == null) {
            synchronized (lock) {
                if (initOptionsResponse == null) {
                    initOptionsResponse = response = InitOptionsResponse.builder()
                            .title(blogSettings.getTitle())
                            .subtitle(blogSettings.getSubtitle())
                            .phone(blogSettings.getPhone())
                            .email(blogSettings.getEmail())
                            .copyright(blogSettings.getCopyright())
                            .copyrightFrom(blogSettings.getCopyrightFrom())
                            .build();
                }
            }
        }
        return response;
    }

}
