package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.request.*;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.api.response.PostResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.model.*;
import diplom.blogengine.repository.*;
import diplom.blogengine.service.ModerationDecision;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityGraph;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ApiPostControllerRestTest extends ApiControllerRestTest {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CachedSettingsRepository settingsRepository;

    @Autowired
    PostsCounterStorage counterStorage;

    // /api/post/my
    @Test
    public void givenWrongOffset_whenSendGetMyPosts_then400BadRequest() throws Exception {
        String resourceUrl = "/api/post/my";
        String offset = "-1";
        URI uri = UriComponentsBuilder
                    .fromHttpUrl(host)
                    .path(resourceUrl)
                    .queryParam("offset", offset)
                    .build()
                    .toUri();
        ResponseEntity<?> responseEntity = testRestTemplate.getForEntity(uri, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenWrongLimit_whenSendGetMyPosts_then400BadRequest() throws Exception {
        String resourceUrl = "/api/post/my";
        String limit = "-1";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .queryParam("limit", limit)
                .build()
                .toUri();
        ResponseEntity<?> responseEntity = testRestTemplate.getForEntity(uri, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenWrongStatus_whenSendGetMyPosts_then400BadRequest() throws Exception {
        String resourceUrl = "/api/post/my";
        String status = "wrong";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .queryParam("status", status)
                .build()
                .toUri();
        ResponseEntity<?> responseEntity = testRestTemplate.getForEntity(uri, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenNotAuth_whenSendGetMyPosts_then401Unauthorized() throws Exception {
        String resourceUrl = "/api/post/my";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        ResponseEntity<?> responseEntity = testRestTemplate.getForEntity(uri, Void.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLogin_whenSendGetMyPostsPublished_thenOk() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        MultiplePostsResponse postsResponse = doGetMyPostsWithStatusAndAssert("published", cookie);

        int expectedPublishedMyPostsCount = 2;
        int actual = postsResponse.getPosts().size();
        assertEquals(expectedPublishedMyPostsCount, actual);

        assertEquals(expectedPublishedMyPostsCount, postsResponse.getCount());
    }

    @Test
    public void givenUserLogin_whenSendGetMyPostsPending_thenOk() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        MultiplePostsResponse postsResponse = doGetMyPostsWithStatusAndAssert("pending", cookie);

        int expectedPendingMyPostsCount = 1;
        int actual = postsResponse.getPosts().size();
        assertEquals(expectedPendingMyPostsCount, actual);

        assertEquals(expectedPendingMyPostsCount, postsResponse.getCount());
    }

    @Test
    public void givenUserLogin_whenSendGetMyPostsDeclined_thenOk() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        MultiplePostsResponse postsResponse = doGetMyPostsWithStatusAndAssert("declined", cookie);

        int expectedPendingMyPostsCount = 1;
        int actual = postsResponse.getPosts().size();
        assertEquals(expectedPendingMyPostsCount, actual);

        assertEquals(expectedPendingMyPostsCount, postsResponse.getCount());
    }

    @Test
    public void givenUserLogin_whenSendGetMyPostsInactive_thenOk() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        MultiplePostsResponse postsResponse = doGetMyPostsWithStatusAndAssert("inactive", cookie);

        int expectedInactiveMyPostsCount = 1;
        int actual = postsResponse.getPosts().size();
        assertEquals(expectedInactiveMyPostsCount, actual);

        assertEquals(expectedInactiveMyPostsCount, postsResponse.getCount());
    }

    // new post

    @Test
    public void givenNotAuthUserAndPostRequest_whenSendPostNewPost_then401Unauthorized() throws Exception {
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        String notAuthCookie = "";
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPost(request, notAuthCookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndIncorrectRequestData_whenSendPostNewPost_then400BadRequest()  throws Exception {
        sendIncorrectPostRequestAndAssertResultResponse(this::sendPostNewPost);
    }

    private void sendIncorrectPostRequestAndAssertResultResponse(SendHTTPRequest<PostDataRequest, ResponseEntity<ResultResponse>> method) {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        //
        long wrongTimestamp = -1;
        PostDataRequest request = testDataGenerator.generatePostDataRequest(req -> req.setTimestamp(wrongTimestamp));
        ResponseEntity<ResultResponse> responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        //
        int wrongActive = 2;
        request = testDataGenerator.generatePostDataRequest(req -> req.setActive(wrongActive));
        responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        //
        String nullTitle = null;
        request = testDataGenerator.generatePostDataRequest(req -> req.setTitle(nullTitle));
        responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        //
        String wrongShortTitle = "a";
        request = testDataGenerator.generatePostDataRequest(req -> req.setTitle(wrongShortTitle));
        responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        //
        String nullText = null;
        request = testDataGenerator.generatePostDataRequest(req -> req.setText(nullText));
        responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        //
        String wrongShortText = "text";
        request = testDataGenerator.generatePostDataRequest(req -> req.setText(wrongShortText));
        responseEntity = method.send(request, cookie);
        assertResultResponseBadRequest(responseEntity);
    }


    @Test
    public void givenUserLoginAndPostRequest_whenSendPostNewPost_thenPostCreatedAnd200Ok() throws Exception {
        deleteAllPosts();
        assertTrue(postRepository.findAll().isEmpty());

        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.POST_PREMODERATION);
        final String YES_VALUE = "YES";
        setting.setValue(YES_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        // create post
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        createNewPostAndAssertResponse(request, cookie);

        List<Post> posts = postRepository.findAll();
        assertNotNull(posts);
        int expectedSize = 1;
        assertEquals(expectedSize, posts.size());

        long createdPostId =  posts.get(0).getId();

        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("tags");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post createdPost = entityManager.find(Post.class, createdPostId, hints);

        String expectedNewPostTitle = request.getTitle();
        assertEquals(expectedNewPostTitle, createdPost.getTitle());

        ModerationStatus expectedModerationStatus = ModerationStatus.NEW;
        assertEquals(expectedModerationStatus, createdPost.getModerationStatus());

        String tagName = "Тэг";
        Set<Tag> expectedTags = Set.of(new Tag(tagName));
        assertEquals(expectedTags, createdPost.getTags());

        assertNotNull(tagRepository.findByName(tagName));
    }

    @Test
    public void givenUserLoginAndPostRequestAndNoPostModer_whenSendPostNewPost_thenPostCreatedWithAcceptedStatusAnd200Ok() throws Exception {
        deleteAllPosts();
        assertTrue(postRepository.findAll().isEmpty());

        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.POST_PREMODERATION);
        final String NO_VALUE = "NO";
        setting.setValue(NO_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        // create post
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        createNewPostAndAssertResponse(request, cookie);

        List<Post> posts = postRepository.findAll();
        int expectedSize = 1;
        assertEquals(expectedSize, posts.size());

        long createdPostId =  posts.get(0).getId();

        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("tags");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post createdPost = entityManager.find(Post.class, createdPostId, hints);

        ModerationStatus expectedModerationStatus = ModerationStatus.ACCEPTED;
        assertEquals(expectedModerationStatus, createdPost.getModerationStatus());
    }


    @Test
    public void givenUserLoginAndPostRequest_whenSendPostNewPost_thenPostCreatedWithSanitizedTitleAndTextAnd200Ok() throws Exception {
        deleteAllPosts();
        assertTrue(postRepository.findAll().isEmpty());

        // create post
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        PostDataRequest request = testDataGenerator.generatePostDataRequest();

        String titleWithTags = "The title must not <b>contains</b> <h1>any</h1> <script src='http://test.ru/script.js' /> tags<SCRIPT>var t = 3</SCRIPT>!";
        String textWithTags = "The text should <html><body><b>contains</b> only <H1>html tags</H1>. And must not contains prohibited <script src='http://test.ru/script.js' />ones.</body></html> Need to be sanitized! <object></object><head>script load</head>Text, <h2>text</h2>, <p>text</p>.";
        request.setTitle(titleWithTags);
        request.setText(textWithTags);
        createNewPostAndAssertResponse(request, cookie);

        List<Post> posts = postRepository.findAll();
        int expectedSize = 1;
        assertEquals(expectedSize, posts.size());

        long createdPostId =  posts.get(0).getId();
        Post createdPost = entityManager.find(Post.class, createdPostId);

        String expectedSanitizedTitle = "The title must not contains any tags!";
        assertEquals(expectedSanitizedTitle, createdPost.getTitle());
        String expectedSanitizedText = "The text should contains only <H1>html tags</H1>. And must not contains prohibited ones. Need to be sanitized! Text, <h2>text</h2>, <p>text</p>.";
        assertEquals(expectedSanitizedText, createdPost.getText());
        String expectedAnnounce = "The text should contains only html tags. And must not contains prohibited ones. Need to be sanitized! Text, text, text.";
        assertEquals(expectedAnnounce, createdPost.getAnnounce());
    }


    private void createNewPostAndAssertResponse(PostDataRequest request, String authCookie) {
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPost(request, authCookie);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResultResponse resultResponse = responseEntity.getBody();
        assertTrue(resultResponse.getResult());
    }

    // update post

    @Test
    public void givenNotAuthUserAndPostRequest_whenSendPutUpdatedPost_then401Unauthorized() throws Exception {
        long postId = 2;
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        String updatedTitle = "Обновленный заголовок";
        request.setTitle(updatedTitle);
        String notAuthCookie = "";
        ResponseEntity<ResultResponse> responseEntity = sendPutUpdatedPost(postId, request, notAuthCookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndNotAuthorPostIdAndPostRequest_whenSendPutUpdate_then403Forbidden() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long updatedPostId = 4;
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        ResponseEntity<ResultResponse> responseEntity = sendPutUpdatedPost(updatedPostId, request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndIncorrectRequestData_whenSendPutUpdatedPost_then400BadRequest()  throws Exception {
        sendIncorrectPostRequestAndAssertResultResponse(this::sendPutUpdatedPost2);
    }

    private ResponseEntity<ResultResponse> sendPutUpdatedPost2(PostDataRequest request, String cookie) {
        long updatedPostId = 2;
        return sendPutUpdatedPost(updatedPostId, request, cookie);
    }

    @Test
    public void givenLoginUserAndPostRequest_whenSendPutUpdatedPost_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long postId = 2;
        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("tags");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post post = entityManager.find(Post.class, postId, hints);
        Set<Tag> oldTags = post.getTags();
        int expectegTagsCount = 2;
        assertEquals(expectegTagsCount, oldTags.size());

        // update post
        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        String updatedTitle = "Обновленный заголовок";
        request.setTitle(updatedTitle);
        ResponseEntity<ResultResponse> responseEntity = sendPutUpdatedPost(postId, request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertTrue(resultResponse.getResult());

        // проверка обновления поста
        Post updatedPost = entityManager.find(Post.class, postId, hints);
        assertEquals(updatedTitle, updatedPost.getTitle());

        ModerationStatus expectedModerationStatus = ModerationStatus.NEW;
        assertEquals(expectedModerationStatus, updatedPost.getModerationStatus());

        String tagName = "Тэг";
        Set<Tag> expectedTags = Set.of(new Tag(tagName));
        assertEquals(expectedTags, updatedPost.getTags());
        assertNotNull(tagRepository.findByName(tagName));

        for (Tag oldTag : oldTags) {
            assertNotNull(tagRepository.findByName(oldTag.getName()));
        }
    }

    @Test
    public void givenUserLoginAndPostRequest_whenSendPutUpdatedPost_thenPostCreatedWithSanitizedTitleAndTextAnd200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        PostDataRequest request = testDataGenerator.generatePostDataRequest();
        String titleWithTags = "The title must not <b>contains</b> <h1>any</h1> <script src='http://test.ru/script.js' /> tags<SCRIPT>var t = 3</SCRIPT>!";
        String textWithTags = "The text should <html><body><b>contains</b> only <H1>html tags</H1>. And must not contains prohibited <script src='http://test.ru/script.js' />ones.</body></html> Need to be sanitized! <object></object><head>script load</head>Text, <h2>text</h2>, <p>text</p>.";
        request.setTitle(titleWithTags);
        request.setText(textWithTags);
        long postId = 2;
        ResponseEntity<ResultResponse> responseEntity = sendPutUpdatedPost(postId, request, cookie);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertTrue(resultResponse.getResult());

        Post updatedPost = entityManager.find(Post.class, postId);

        String expectedSanitizedTitle = "The title must not contains any tags!";
        assertEquals(expectedSanitizedTitle, updatedPost.getTitle());
        String expectedSanitizedText = "The text should contains only <H1>html tags</H1>. And must not contains prohibited ones. Need to be sanitized! Text, <h2>text</h2>, <p>text</p>.";
        assertEquals(expectedSanitizedText, updatedPost.getText());
        String expectedAnnounce = "The text should contains only html tags. And must not contains prohibited ones. Need to be sanitized! Text, text, text.";
        assertEquals(expectedAnnounce, updatedPost.getAnnounce());
    }

    // comments

    @Test
    public void givenNotAuthUserAndCommentRequest_whenSendPost_then401Unautorized() throws Exception {
        long postId = 2;
        PostCommentDataRequest request = testDataGenerator.generateCommentDataRequest(postId);
        String notAuthCookie = "";
        ResponseEntity<ResultResponse> responseEntity = sendPostComment(request, notAuthCookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndCommentRequestWithNotFoundPostId_whenSendPost_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        
        long postId = 20;
        PostCommentDataRequest request = testDataGenerator.generateCommentDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostComment(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndCommentRequestWithIncorrectParentId_whenSendPost_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long postId = 2;
        long parentId = 20;
        PostCommentDataRequest request = testDataGenerator.generateCommentDataRequest(postId, parentId);
        ResponseEntity<ResultResponse> responseEntity = sendPostComment(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndCommentRequest_whenSendPost_thenCommentCreatedAndOk() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long postId = 2;
        PostCommentDataRequest request = testDataGenerator.generateCommentDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostComment(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertNotEquals(resultResponse.getId(), 0L);

        // выборка eager post.comments за один select с помощью EntityGraph
        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("comments");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post post = entityManager.find(Post.class, postId, hints);
        boolean postedCommentFound = false;
        for (PostComment comment : post.getComments()) {
            if (comment.getText().equals(request.getText())){
                postedCommentFound = true;
            }
        }
        assertTrue(postedCommentFound);
    }

    // votes

    @Test
    public void givenNotAuthUserAndVoteRequest_whenSendPost_then401Unauthorized() throws Exception {
        long postId = 2;
        boolean isLikeRequest = true;
        VoteDataRequest request = testDataGenerator.generateVoteDataRequest(postId);
        String notAuthCookie = "";
        ResponseEntity<ResultResponse> responseEntity = sendPostVote(request, isLikeRequest, notAuthCookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndVoteRequestWithNotFoundPostId_whenSendPostLike_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long postId = 20;
        boolean isLikeRequest = true;
        VoteDataRequest request = testDataGenerator.generateVoteDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostVote(request, isLikeRequest, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }


    @Test
    public void givenUserLoginAndVoteRequest_whenSendPostLike_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        // eager select post.votes with one select
        long postId = 3;
        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("votes");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post post = entityManager.find(Post.class, postId, hints);
        assertTrue(post.getVotes().isEmpty());

        boolean isLikeRequest = true;
        VoteDataRequest request = testDataGenerator.generateVoteDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostVote(request, isLikeRequest, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertTrue(resultResponse.getResult());

        post = entityManager.find(Post.class, postId, hints);
        List<PostVote> votes = post.getVotes();
        int expectedSize = 1;
        assertEquals(expectedSize, votes.size());

        int expectedValue = 1;
        assertEquals(expectedValue, votes.get(0).getValue());
    }


    @Test
    public void givenUserLoginAndVoteRequest_whenSendPostDisLike_then200Ok() throws Exception {
        long postId = 3;

        // eager select post.votes with one select
        EntityGraph<Post> graph = entityManager.createEntityGraph(Post.class);
        graph.addAttributeNodes("votes");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);
        Post post = entityManager.find(Post.class, postId, hints);
        assertTrue(post.getVotes().isEmpty());

        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        boolean isLikeRequest = false;
        VoteDataRequest request = testDataGenerator.generateVoteDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostVote(request, isLikeRequest, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertTrue(resultResponse.getResult());

        post = entityManager.find(Post.class, postId, hints);
        List<PostVote> votes = post.getVotes();
        int expectedSize = 1;
        assertEquals(expectedSize, votes.size());

        int expectedValue = -1;
        assertEquals(expectedValue, votes.get(0).getValue());
    }


    @Test
    public void givenUserLoginAndVoteRequest_whenSendDoublePostLike_then200AndResultFalse() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        long postId = 3;
        boolean isLikeRequest = true;
        VoteDataRequest request = testDataGenerator.generateVoteDataRequest(postId);
        ResponseEntity<ResultResponse> responseEntity = sendPostVote(request, isLikeRequest, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertTrue(resultResponse.getResult());

        responseEntity = sendPostVote(request, isLikeRequest, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        resultResponse = responseEntity.getBody();
        assertNotNull(resultResponse);
        assertFalse(resultResponse.getResult());
    }


    // /api/post/moderation
    @Test
    public void givenNotAuth_whenSendGetModerationPosts_then401Unauthorized() throws Exception {
        String notAuth = "";
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(notAuth, b -> b);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUserAndNotModerator_whenSendGetModerationPosts_then401Unauthorized() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }


    @Test
    public void givenAuthUserAndIncorrectOffset_whenSendGetModerationPosts_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        int wrongOffset = -1;
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("offset", wrongOffset) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUserAndIncorrectLimit_whenSendGetModerationPosts_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        int wrongLimit = -1;
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("limit", wrongLimit) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUserAndIncorrectStatus_whenSendGetModerationPosts_then400BadRequest() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        String wrongStatus = "wrong";
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("status", wrongStatus) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUser_whenSendGetModerationPostsNew_then200Ok() {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        String status = "new";
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("status", status) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        MultiplePostsResponse response = responseEntity.getBody();
        assertNotNull(response);

        long expectedCount = 2;
        assertEquals(expectedCount, response.getCount());

        List<PostResponse> posts = response.getPosts();
        assertEquals(expectedCount, posts.size());

        Set<String> expectedTitles = Set.of("Пост1", "Пост5");
        for (PostResponse post : posts) {
            assertTrue(expectedTitles.contains(post.getTitle()));
        }
    }


    @Test
    public void givenAuthUser_whenSendGetModerationPostsAccepted_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        String status = "accepted";
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("status", status) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        MultiplePostsResponse response = responseEntity.getBody();
        assertNotNull(response);

        long expectedCount = 3;
        assertEquals(expectedCount, response.getCount());

        List<PostResponse> posts = response.getPosts();
        assertEquals(expectedCount, posts.size());

        Set<String> expectedTitles = Set.of("Пост2", "Пост3", "Пост4");
        for (PostResponse post : posts) {
            assertTrue(expectedTitles.contains(post.getTitle()));
        }
    }


    @Test
    public void givenAuthUser_whenSendGetModerationPostsDeclined_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        String status = "declined";
        ResponseEntity<MultiplePostsResponse> responseEntity = sendGetModerationPosts(cookie, b -> b.queryParam("status", status) );
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        MultiplePostsResponse response = responseEntity.getBody();
        assertNotNull(response);

        long expectedCount = 1;
        assertEquals(expectedCount, response.getCount());

        List<PostResponse> posts = response.getPosts();
        assertEquals(expectedCount, posts.size());

        Set<String> expectedTitles = Set.of("Пост6");
        for (PostResponse post : posts) {
            assertTrue(expectedTitles.contains(post.getTitle()));
        }
    }

    // /api/moderation
    @Test
    public void givenNotAuth_whenSendPostModeration_then401Unauthorized() throws Exception {
        String notAuth = "";
        long postId = 1;
        ModerationDecision decision = ModerationDecision.ACCEPT;
        PostModerationRequest request = new PostModerationRequest(postId, decision);

        ResponseEntity<ResultResponse> responseEntity = sendPostModeration(request, notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUserAndNotModerator_whenSendPostModeration_then401Unauthorized() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        long postId = 1;
        ModerationDecision decision = ModerationDecision.ACCEPT;
        PostModerationRequest request = new PostModerationRequest(postId, decision);

        ResponseEntity<ResultResponse> responseEntity = sendPostModeration(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }


    @Test
    public void givenAuthUser_whenSendPostModerationAccept_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);
        long postId = 1;
        Post post = entityManager.find(Post.class, postId);
        assertNotNull(post);
        ModerationStatus expectedStatus = ModerationStatus.NEW;
        assertEquals(expectedStatus, post.getModerationStatus());

        ModerationDecision decision = ModerationDecision.ACCEPT;
        PostModerationRequest request = new PostModerationRequest(postId, decision);

        ResponseEntity<ResultResponse> responseEntity = sendPostModeration(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertTrue(responseEntity.getBody().getResult());

        post = entityManager.find(Post.class, postId);
        assertNotNull(post);
        expectedStatus = ModerationStatus.ACCEPTED;
        assertEquals(expectedStatus, post.getModerationStatus());
    }

    @Test
    public void givenAuthUser_whenSendPostModerationDecline_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);
        long postId = 1;
        Post post = entityManager.find(Post.class, postId);
        assertNotNull(post);
        ModerationStatus expectedStatus = ModerationStatus.NEW;
        assertEquals(expectedStatus, post.getModerationStatus());

        ModerationDecision decision = ModerationDecision.DECLINE;
        PostModerationRequest request = new PostModerationRequest(postId, decision);

        ResponseEntity<ResultResponse> responseEntity = sendPostModeration(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertTrue(responseEntity.getBody().getResult());

        post = entityManager.find(Post.class, postId);
        assertNotNull(post);
        expectedStatus = ModerationStatus.DECLINED;
        assertEquals(expectedStatus, post.getModerationStatus());
    }


    // /api/post

    @Test
    public void givenAuthorLogin_whenSendGetPostById_thenReturnedViewCountNotChanged() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        long postId = 2;
        int requestCount = 5;
        int expectedViewCount = 1;
        cachedPostRepository.clearAllCache();
        //System.out.println(counterStorage.get(postId));

        // when
        for(int i = 0; i < requestCount; i++) {
            ResponseEntity<SinglePostResponse> responseEntity = sendGetPost(postId, cookie);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        }
        ResponseEntity<SinglePostResponse> responseEntity = sendGetPost(postId, cookie);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        SinglePostResponse response = responseEntity.getBody();
        assertNotNull(response);

        // then
        long actualViewCountFromResponse = response.getViewCount();
        assertEquals(expectedViewCount, actualViewCountFromResponse);

        Integer actualViewCountFromCache = counterStorage.get(postId);
        //System.out.println(actualViewCountFromCache);
        if (actualViewCountFromCache != null) {
            assertEquals(expectedViewCount, actualViewCountFromCache.intValue());
        }

        int timeToUpdateCountersFromCacheToDB = 10;
        TimeUnit.SECONDS.sleep(timeToUpdateCountersFromCacheToDB);

        //System.out.println(counterStorage.get(postId));
        long actualViewCountFromRepository1 = postRepository.findById(postId).get().getViewCount();
        assertEquals(expectedViewCount, actualViewCountFromRepository1);
    }


    // NOT TESTS //////////////////////////////////////////////////////////////////////////////////////////////////

    private MultiplePostsResponse doGetMyPostsWithStatusAndAssert(String status, String cookie) {
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        String resourceUrl = "/api/post/my";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .queryParam("status", status)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        ResponseEntity<MultiplePostsResponse> responseEntity = testRestTemplateLocal.exchange(uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                MultiplePostsResponse.class);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        MultiplePostsResponse postsResponse = responseEntity.getBody();
        assertNotNull(postsResponse);
        assertNotNull(postsResponse.getPosts());

        return postsResponse;
    }

    private  ResponseEntity<ResultResponse> sendPostNewPost(PostDataRequest request, String cookie) {
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        String resourceUrl = "/api/post";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        HttpEntity<PostDataRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplateLocal.postForEntity(uri, entity, ResultResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPutUpdatedPost(long id, PostDataRequest request, String cookie) {
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        String resourceUrl = "/api/post/" + id;
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        HttpEntity<PostDataRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplateLocal.exchange(uri, HttpMethod.PUT, entity, ResultResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPostComment(PostCommentDataRequest request, String cookie) {
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        String resourceUrl = "/api/comment/";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        HttpEntity<PostCommentDataRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplateLocal.exchange(uri, HttpMethod.POST, entity, ResultResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPostVote(VoteDataRequest request, boolean isLike, String cookie) {
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        String resourceUrl = "/api/post/";
        String votePath = isLike ? "like" : "dislike";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .path(votePath)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        HttpEntity<VoteDataRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplateLocal.postForEntity(uri, entity, ResultResponse.class);
    }

    private ResponseEntity<MultiplePostsResponse> sendGetModerationPosts(String cookie,
                                                                         UnaryOperator<UriComponentsBuilder> uriBuilder) {
        String resourceUrl = "/api/post/moderation";
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl);
        if (uriBuilder != null) {
            uriComponentsBuilder = uriBuilder.apply(uriComponentsBuilder);
        }

        URI uri = uriComponentsBuilder.build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        return testRestTemplateLocal.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), MultiplePostsResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPostModeration(PostModerationRequest request, String cookie) {
        String resourceUrl = "/api/moderation";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        //headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //headers.setContentType(MediaType.APPLICATION_JSON);
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        return testRestTemplateLocal.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), ResultResponse.class);
    }


    private void deleteAllPosts() {
        postRepository.deleteAll();
    }

}
