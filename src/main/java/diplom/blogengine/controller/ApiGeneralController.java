package diplom.blogengine.controller;

import diplom.blogengine.service.IOptionsSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApiGeneralController {
//    private static Logger logger = LoggerFactory.getLogger(ApiGeneralController.class);
//    @Autowired
//    private ObjectMapper om;

    private final IOptionsSettingsService optionsSettingsService;

    public ApiGeneralController(IOptionsSettingsService optionsSettingsService) {
        this.optionsSettingsService = optionsSettingsService;
    }

    @GetMapping("/api/init")
    public ResponseEntity<?> initOptions() {
        return ResponseEntity.ok(optionsSettingsService.getInitOptions());
    }

    @GetMapping("/api/settings")
    public ResponseEntity<?> settings() {
        return ResponseEntity.ok(optionsSettingsService.getGlobalSettings());
    }
}
