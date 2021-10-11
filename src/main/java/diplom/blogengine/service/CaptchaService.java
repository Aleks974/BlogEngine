package diplom.blogengine.service;

import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.service.util.CaptchaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.rmi.server.UID;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class CaptchaService implements ICaptchaService {
    private final String IMAGE_CONTENT_TYPE = "data:image/png;base64";
    private final CaptchaCodeRepository captchaCodeRepository;
    private final CaptchaGenerator captchaGenerator;
    private final AuthResponsesMapper responsesMapper;
    private final BlogSettings blogSettings;

    public CaptchaService(CaptchaCodeRepository captchaCodeRepository,
                          CaptchaGenerator captchaGenerator,
                          AuthResponsesMapper responsesMapper,
                          BlogSettings blogSettings) {
        this.captchaCodeRepository = captchaCodeRepository;
        this.captchaGenerator = captchaGenerator;
        this.responsesMapper = responsesMapper;
        this.blogSettings = blogSettings;
    }

    @Override
    public CaptchaResponse generateCaptchaDataAndDeleteOld() {
        log.debug("enter generateCaptchaDataAndDeleteOld()");

        String code = captchaGenerator.genRandomString();
        String encodedCaptchaString = generateCaptchaString(code);
        String secretCode = UUID.randomUUID().toString();

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(code);
        captchaCode.setSecretCode(secretCode);
        captchaCode.setTime(LocalDateTime.now());
        captchaCodeRepository.saveAndFlush(captchaCode);

        deleteOldCaptchas();

        return responsesMapper.captchaResponse(secretCode, encodedCaptchaString);
    }

    private String generateCaptchaString(String text) {
        return IMAGE_CONTENT_TYPE.concat(", ").concat(captchaGenerator.genCaptchaEncoded(text));
    }

    private void deleteOldCaptchas() {
        log.debug("enter deleteOldCaptchas()");

        int timeout = blogSettings.getCaptchaDeleteTimeout();
        LocalDateTime time = LocalDateTime.now().minusSeconds(timeout);
        captchaCodeRepository.deleteOlderThan(time);
    }


}

