package diplom.blogengine.api.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ResponseHelper {
    public static ResponseEntity<?> unauthorizedResponse() {
        log.debug("enter unauthorizedResponse()");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
