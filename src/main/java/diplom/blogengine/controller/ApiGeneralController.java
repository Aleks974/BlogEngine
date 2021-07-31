package diplom.blogengine.controller;

import diplom.blogengine.service.IInitOptionsService;
import diplom.blogengine.service.ISettingsService;
import diplom.blogengine.service.ITagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class ApiGeneralController {
//    private static Logger logger = LoggerFactory.getLogger(ApiGeneralController.class);
//    @Autowired
//    private ObjectMapper om;

    private final IInitOptionsService initOptionsService;
    private final ISettingsService settingsService;
    private final ITagsService tagsService;

    ApiGeneralController(IInitOptionsService initOptionsService, ISettingsService settingsService, ITagsService tagsService) {
        this.initOptionsService = initOptionsService;
        this.settingsService = settingsService;
        this.tagsService = tagsService;
    }

    @GetMapping("/api/init")
    public Map<String, String> initOptions() {
        return initOptionsService.getInitOptions();
    }

    @GetMapping("/api/settings")
    public Map<String, String> settings() {
        return settingsService.getSettings();
    }

    @GetMapping("/api/tag")
    public String tags() {
        return tagsService.getTags();
    }
}
