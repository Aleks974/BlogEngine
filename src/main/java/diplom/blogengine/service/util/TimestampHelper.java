package diplom.blogengine.service.util;

import java.time.LocalDateTime;
import java.util.TimeZone;

public class TimestampHelper {

    private final TimeZone serverTimeZone;

    public TimestampHelper(TimeZone serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public long toTimestampAtServerZone(LocalDateTime dateTime) {
        return dateTime.atZone(serverTimeZone.toZoneId()).toEpochSecond();
    }
}
