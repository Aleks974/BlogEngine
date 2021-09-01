package diplom.blogengine.service.sort;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class SortField {
    private String fieldName;
    private Sort.Direction direction;
    private boolean sortUnsafe;
}
