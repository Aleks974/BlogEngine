package diplom.blogengine.controller;

import diplom.blogengine.service.ITagsService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;

@Slf4j
@RestController
@Validated
public class ApiTagController {
    private final int MAX_QUERY_LENGTH = 100;

    private final ITagsService tagsService;

    public ApiTagController(ITagsService tagsService) {
        this.tagsService = tagsService;
    }

    @GetMapping("/api/tag")
    public ResponseEntity<?> getTags(@RequestParam(required = false) @Length(max = MAX_QUERY_LENGTH) String query) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tagsService.getTagsData(query));
    }
}
