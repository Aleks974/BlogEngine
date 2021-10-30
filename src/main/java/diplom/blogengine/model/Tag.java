package diplom.blogengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Column(length = 255, unique = true, nullable = false)
    private String name;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Post> posts;

    public Tag() {
    }

    public Tag(String name) {
        super();
        this.name = name;
    }

}
