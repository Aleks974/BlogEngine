package diplom.blogengine.service.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimestampHelper {

    private final TimeZone serverTimeZone;

    public TimestampHelper(TimeZone serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public long toTimestampAtServerZone(LocalDateTime dateTime) {
        return dateTime.atZone(serverTimeZone.toZoneId()).toEpochSecond();
    }

    public LocalDateTime toLocalDateTimeAtServerZone(long timestamp) {
        //Instant.ofEpochSecond(timestamp).atZone(serverTimeZone.toZoneId()).toLocalDateTime()
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), serverTimeZone.toZoneId());
    }

    public long genCurrentTimestamp() {
        return LocalDateTime.now().atZone(serverTimeZone.toZoneId()).toEpochSecond();
    }
}
