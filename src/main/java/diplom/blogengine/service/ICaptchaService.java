package diplom.blogengine.service;

import diplom.blogengine.api.response.CaptchaResponse;

public interface ICaptchaService {
    CaptchaResponse generateCaptchaData();

    void deleteExpiryCaptchas(long expireTimeout);
}
