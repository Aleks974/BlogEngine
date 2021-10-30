package diplom.blogengine.controller;

import diplom.blogengine.api.request.PostCommentDataRequest;
import diplom.blogengine.api.request.PostDataRequest;
import diplom.blogengine.api.request.PostModerationRequest;
import diplom.blogengine.api.request.VoteDataRequest;
import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.security.IAuthenticationService;
import diplom.blogengine.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.security.Principal;

@Slf4j
@RestController
@Validated
public class ApiPostController {
    private final String DEFAULT_OFFSET = "0";
    private final String DEFAULT_LIMIT = "10";
    private final int MAX_LIMIT = 100;
    private final String DEFAULT_MODE = "RECENT";
    private final String DEFAULT_MYPOSTS_STATUS = "PUBLISHED";
    private final String DEFAULT_MODERATION_STATUS = "NEW";
    private final int MAX_QUERY_LENGTH = 100;
    private final int DATE_LENGTH = 10;

    private final IPostService postService;
    private final IAuthenticationService authService;
    private final IOptionsSettingsService optionsSettingsService;

    public ApiPostController(IPostService postService,
                             IAuthenticationService authService,
                             IOptionsSettingsService optionsSettingsService) {
        this.postService = postService;
        this.authService = authService;
        this.optionsSettingsService = optionsSettingsService;
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
    public ResponseEntity<?> getCalendarByYear(@RequestParam(required = false) @Min(2000) @Max(2050) Integer year) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getCalendarDataByYear(year));
    }


    @GetMapping("/api/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable(required = true) long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getPostDataById(id, authService.getAuthenticatedUser()));
    }

    @GetMapping("/api/post/my")
    public ResponseEntity<?> getMyPosts(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                        @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                        @RequestParam(required = false, defaultValue = DEFAULT_MYPOSTS_STATUS) MyPostStatus status) {
        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(postService.getMyPostsData(offset, limit, status, authService.getAuthenticatedUser()));
    }

    @PostMapping("/api/post")
    public ResponseEntity<?> newPost(@RequestBody @Valid PostDataRequest postDataRequest, HttpServletRequest request) {
        log.debug("enter newPost()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        postDataRequest.setLocale(request.getLocale());
        boolean moderationIsEnabled = optionsSettingsService.postPremoderationIsEnabled();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.newPost(postDataRequest, authService.getAuthenticatedUser(), moderationIsEnabled));
    }

    @PutMapping("/api/post/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(required = true) @Positive long id,
                                        @RequestBody @Valid PostDataRequest postDataRequest, HttpServletRequest request) {
        log.debug("enter updatePost()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        postDataRequest.setLocale(request.getLocale());
        boolean moderationIsEnabled = optionsSettingsService.postPremoderationIsEnabled();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.updatePost(id, postDataRequest, authService.getAuthenticatedUser(), moderationIsEnabled));
    }



    @PostMapping("/api/comment")
    public ResponseEntity<?> newComment(@RequestBody @Valid PostCommentDataRequest commentDataRequest, HttpServletRequest request) {
        log.debug("enter newComment()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.newComment(commentDataRequest, authService.getAuthenticatedUser(), request.getLocale()));
    }

    @PostMapping("/api/post/{vote}")
    public ResponseEntity<?> newVote(@PathVariable(name="vote", required = true) VoteParameter voteParam,
                                     @RequestBody @Valid VoteDataRequest voteDataRequest,
                                     HttpServletRequest request) {
        log.debug("enter newLike()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.newVote(voteParam, voteDataRequest, authService.getAuthenticatedUser(), request.getLocale()));
    }

    @GetMapping("/api/post/moderation")
    public ResponseEntity<?> getModerationPosts(@RequestParam(required = false, defaultValue = DEFAULT_OFFSET) @PositiveOrZero int offset,
                                                @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) @PositiveOrZero @Max(MAX_LIMIT) int limit,
                                                @RequestParam(required = false, defaultValue = DEFAULT_MODERATION_STATUS) ModerationStatus status) {
        log.debug("enter getModerationPosts()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        if (!authService.getAuthenticatedUser().isModerator()) {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.getModerationPostsData(offset, limit, status, authService.getAuthenticatedUser()));
    }


    @PostMapping("/api/moderation")
    public ResponseEntity<?> moderatePost(@RequestBody @Valid PostModerationRequest postModerationRequest) {
        log.debug("enter moderatePost()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        if (!authService.getAuthenticatedUser().isModerator()) {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(postService.moderatePost(postModerationRequest, authService.getAuthenticatedUser()));
    }

    private ResponseEntity<?> unauthorizedResponse() {
        log.debug("enter unauthorizedResponse()");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
