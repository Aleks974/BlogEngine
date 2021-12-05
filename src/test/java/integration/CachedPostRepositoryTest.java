package integration;


import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.PostsCounterStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CachedPostRepositoryTest extends ApiControllerRestTest{

    @Autowired
    private PostRepository postRepository;

    @Autowired
    PostsCounterStorage counterStorage;

    //@Test
    public void givenResourceUrl_whenSendGetPostByIdParallel_thenReturnedViewCountCorrect() throws Exception {
        final long postId = 3L;
        final int threadCount = 10;
        final int requestCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<SinglePostResponse>> listFutures = new ArrayList<>();

        for(int i = 0; i < requestCount; i++) {
            Callable<SinglePostResponse> callable = () -> {
                ResponseEntity<SinglePostResponse> responseEntity = sendGetPost(postId);
                return responseEntity.getBody();
            };
            listFutures.add(executorService.submit(callable));
        }
        for (Future f : listFutures) {
            f.get();
        }

        ResponseEntity<SinglePostResponse> responseEntity = sendGetPost(postId);
        SinglePostResponse response = responseEntity.getBody();
        assertNotNull(response);

        long expectedViewCount = requestCount + 1;
        long actualViewCountFromResponse = response.getViewCount();
        assertEquals(expectedViewCount, actualViewCountFromResponse);

        long actualViewCountFromCache = counterStorage.get(postId);
        assertEquals(expectedViewCount, actualViewCountFromCache);

        int timeToUpdateCountersFromCacheToDB = 15;
        TimeUnit.SECONDS.sleep(timeToUpdateCountersFromCacheToDB);

        long actualViewCountFromRepository = postRepository.findById(postId).get().getViewCount();
        assertEquals(expectedViewCount, actualViewCountFromRepository);
    }
}
