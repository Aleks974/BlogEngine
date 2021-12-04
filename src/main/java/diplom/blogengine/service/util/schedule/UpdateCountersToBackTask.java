package diplom.blogengine.service.util.schedule;

import diplom.blogengine.repository.PostsCounterStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Arrays;
import java.util.Set;

@Slf4j
public class UpdateCountersToBackTask implements Runnable {
    private final EntityManager entityManager;
    private final TransactionTemplate txTemplate;
    private final PostsCounterStorage counterStorage;

    public UpdateCountersToBackTask(EntityManager entityManager, TransactionTemplate txTemplate, PostsCounterStorage counterStorage) {
        this.entityManager = entityManager;
        this.txTemplate = txTemplate;
        this.counterStorage = counterStorage;
    }

    @Override
    public void run() {
        updateCountersToBackDataBase();
    }

    private void updateCountersToBackDataBase() {
        log.debug("enter updateCountersToBackDataBase()");

        Set<Long> updatedIds = counterStorage.getAndClearUpdatedIds();

        int successUpdate = 0;
        for (Long postId : updatedIds) {
            int counterValue = counterStorage.get(postId);
            try {
                txTemplate.execute(t -> {
                    entityManager
                        .createNativeQuery("UPDATE posts SET view_count = ? WHERE id = ?")
                        .setParameter(1, counterValue)
                        .setParameter(2, postId)
                        .executeUpdate();
                    t.flush();
                    return null;
                });
                successUpdate++;
            } catch (Exception ex) {
                log.error(ex.toString());
                log.error(Arrays.toString(ex.getStackTrace()));
            }

        }
        if (successUpdate > 0) {
            log.debug("counters in back database have been updated for posts count: {}", successUpdate);
        }
    }
}
