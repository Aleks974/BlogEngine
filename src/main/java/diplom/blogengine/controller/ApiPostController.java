package diplom.blogengine.controller;

import diplom.blogengine.service.sort.PostSortMode;
import diplom.blogengine.service.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Slf4j
@RestController
@Validated
public class ApiPostController {
    private final String DEFAULT_OFFSET = "0";
    private final String DEFAULT_LIMIT = "10";
    private final int MAX_LIMIT = 100;
    private final String DEFAULT_MODE = "RECENT";
    private final int MAX_QUERY_LENGTH = 100;
    private final int DATE_LENGTH = 10;

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


    @GetMapping("/api/post/search")
    public ResponseEntity<?> getPostsByQuery(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                      @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                      @RequestParam(required = false) @Size(max = MAX_QUERY_LENGTH) String query) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostsDataByQuery(offset, limit, query));
    }


    @GetMapping("/api/post/byDate")
    public ResponseEntity<?> getPostsByDate(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                            @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                            @RequestParam(required = true) @Size(min = DATE_LENGTH, max = DATE_LENGTH) String date) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostsDataByDate(offset, limit, date));
    }


    @GetMapping("/api/post/byTag")
    public ResponseEntity<?> getPostsByTag(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                            @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                            @RequestParam(required = true) @Size(max = MAX_QUERY_LENGTH) String tag) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostsDataByTag(offset, limit, tag));
    }


    @GetMapping("/api/calendar")
    public ResponseEntity<?> getCalendarByYear(@RequestParam(required = false) @Min(2020) @Max(2050) Integer year) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getCalendarDataByYear(year));
    }


    @GetMapping("/api/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable(required = true) long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostDataById(id));
    }

    @GetMapping("/api/post/test")
    public ResponseEntity<?> test() {
        postService.test();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).build();
    }
}
