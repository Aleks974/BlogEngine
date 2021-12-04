package diplom.blogengine.config;

import diplom.blogengine.repository.PasswordTokenRepository;
import diplom.blogengine.repository.PostsCounterStorage;
import diplom.blogengine.service.ICaptchaService;
import diplom.blogengine.service.util.schedule.DeleteExpiryResetPassswordTokensTask;
import diplom.blogengine.service.util.schedule.DeleteExpiryCaptchaTask;
import diplom.blogengine.service.util.schedule.ScheduledTasksHandler;
import diplom.blogengine.service.util.schedule.UpdateCountersToBackTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.concurrent.TimeUnit;

@Configuration
public class BlogSchedulerTasksConfig {

    @Bean
    public ScheduledTasksHandler scheduledTasksHandler() {
        int corePoolSize = 1;
        return new ScheduledTasksHandler(corePoolSize);
    }

    @Autowired
    public void scheduleUpdateCountersToBackDataBaseTask(ScheduledTasksHandler tasksHandler,
                                                         EntityManager entityManager,
                                                         TransactionTemplate txTemplate,
                                                         PostsCounterStorage counterStorage) {
        UpdateCountersToBackTask task = new UpdateCountersToBackTask(entityManager, txTemplate, counterStorage);
        int initialDelay = 10;
        int period = 10;
        tasksHandler.scheduleTask(task, initialDelay, period, TimeUnit.MINUTES);
    }

    @Autowired
    public void scheduleDeleteOldCaptchaTask(ScheduledTasksHandler tasksHandler,
                                                 ICaptchaService captchaService,
                                                 BlogSettings blogSettings) {
        DeleteExpiryCaptchaTask task = new DeleteExpiryCaptchaTask(captchaService, blogSettings.getCaptchaDeleteTimeout());
        int initialDelay = 60;
        int period = 60;
        tasksHandler.scheduleTask(task, initialDelay, period, TimeUnit.MINUTES);
    }

    @Autowired
    public void scheduleDeleteExpiryResetPasswordTokensTask(ScheduledTasksHandler tasksHandler,
                                                  PasswordTokenRepository tokenRepository) {
        DeleteExpiryResetPassswordTokensTask task = new DeleteExpiryResetPassswordTokensTask(tokenRepository);
        int initialDelay = 60 * 2;
        int period = 60 * 2;;
        tasksHandler.scheduleTask(task, initialDelay, period, TimeUnit.MINUTES);
    }
}
