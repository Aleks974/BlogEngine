package diplom.blogengine.service.util.schedule;

import diplom.blogengine.service.ICaptchaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteExpiryCaptchaTask implements Runnable {
    private final ICaptchaService captchaService;
    private final long expiredTimeout;

    public DeleteExpiryCaptchaTask(ICaptchaService captchaService, long expiredTimeout) {
        this.captchaService = captchaService;
        this.expiredTimeout = expiredTimeout;
    }

    @Override
    public void run() {
        log.debug("enter DeleteExpiryCaptchaTask.run()");

        captchaService.deleteExpiryCaptchas(expiredTimeout);
    }
}
