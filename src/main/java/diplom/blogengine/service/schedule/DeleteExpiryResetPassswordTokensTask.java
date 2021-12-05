package diplom.blogengine.service.schedule;

import diplom.blogengine.repository.PasswordTokenRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class DeleteExpiryResetPassswordTokensTask implements Runnable {
    private final PasswordTokenRepository tokenRepository;

    public DeleteExpiryResetPassswordTokensTask(PasswordTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void run() {
        log.debug("enter DeleteExpiryResetPassswordTokensTask.run()");

        tokenRepository.deleteExpiryTokens(LocalDateTime.now());
    }
}
