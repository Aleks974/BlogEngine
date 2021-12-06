package diplom.blogengine.service;

import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.service.util.CaptchaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.rmi.server.UID;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class CaptchaService implements ICaptchaService {
    private final String IMAGE_CONTENT_TYPE = "data:image/png;base64";
    private final int CAPTCHA_CODE_LENGTH = 3;

    private final CaptchaCodeRepository captchaCodeRepository;
    private final CaptchaGenerator captchaGenerator;
    private final AuthResponsesMapper responsesMapper;
    public CaptchaService(CaptchaCodeRepository captchaCodeRepository,
                          CaptchaGenerator captchaGenerator,
                          AuthResponsesMapper responsesMapper) {
        this.captchaCodeRepository = captchaCodeRepository;
        this.captchaGenerator = captchaGenerator;
        this.responsesMapper = responsesMapper;
    }

    @Override
    public CaptchaResponse generateCaptchaData() {
        log.debug("enter generateCaptchaData()");

        String code = captchaGenerator.genRandomString(CAPTCHA_CODE_LENGTH);
        String encodedCaptchaString = generateCaptchaString(code);
        String secretCode = UUID.randomUUID().toString();

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(code);
        captchaCode.setSecretCode(secretCode);
        captchaCode.setTime(LocalDateTime.now());
        captchaCodeRepository.saveAndFlush(captchaCode);

        return responsesMapper.captchaResponse(secretCode, encodedCaptchaString);
    }

    private String generateCaptchaString(String text) {
        String captchaEncoded = captchaGenerator.genCaptchaEncoded(text);
        return IMAGE_CONTENT_TYPE.concat(", ").concat(captchaEncoded);
    }

    public void deleteExpiryCaptchas(long expireTimeout) {
        log.debug("enter deleteExpiryCaptchas()");

        LocalDateTime time = LocalDateTime.now().minusSeconds(expireTimeout);
        captchaCodeRepository.deleteExpiryCaptchas(time);
    }
}

