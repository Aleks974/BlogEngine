package diplom.blogengine.controller;

import diplom.blogengine.service.sort.PostSortMode;
import diplom.blogengine.service.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@Validated
public class ApiPostController {
    private final String DEFAULT_OFFSET = "0";
    private final String DEFAULT_LIMIT = "10";
    private final int MAX_LIMIT = 100;
    private final String DEFAULT_MODE = "RECENT";

    private final IPostService postService;

    public ApiPostController(IPostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/post")
    public ResponseEntity<?> getPosts(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                      @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                      @RequestParam(required = false, defaultValue = DEFAULT_MODE) PostSortMode mode) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostsData(offset, limit, mode));
    }

}
