package util;

import org.springframework.http.ResponseEntity;

@FunctionalInterface
public interface AssertFunctional {
    <T> void assertFunc(ResponseEntity<T> response);
}
