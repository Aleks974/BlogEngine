package diplom.blogengine.service.util;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DdosAtackDefender {
    private final int SEC_IN_MINUTE = 60;
    private final int MAX_CAPTCHA_REQUESTS = 20;
    private final ConcurrentMap<String, List<LocalDateTime>> captchaRequestsIpCache = new ConcurrentHashMap<>();


    public boolean validateCaptchaRequest(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minuteBeforeNow = now.minusSeconds(SEC_IN_MINUTE);
        List<LocalDateTime> times = captchaRequestsIpCache.get(ip);
        if (times != null) {
            long requestCount = times.stream().filter(t -> t.isAfter(minuteBeforeNow)).count();
            if (requestCount > MAX_CAPTCHA_REQUESTS) {
                return false;
            }
        } else {
            times = new ArrayList<>();
            captchaRequestsIpCache.put(ip, times);
        }
        times.add(now);
        deleteOldEntries(minuteBeforeNow);
        return true;
    }

    private void deleteOldEntries(LocalDateTime minuteBeforeNow) {
        Iterator<String> it = captchaRequestsIpCache.keySet().iterator();
        while (it.hasNext()) {
            List<LocalDateTime> times = captchaRequestsIpCache.get(it.next());
            times.removeIf(t -> t.isBefore(minuteBeforeNow));
            if (times.size() == 0) {
                it.remove();
            }
        }
    }

}
