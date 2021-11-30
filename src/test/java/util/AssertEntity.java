package util;

import org.springframework.http.ResponseEntity;

@FunctionalInterface
public interface AssertEntity {
    <T> void asserts(ResponseEntity<T> response);
}
