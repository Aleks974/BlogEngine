package diplom.blogengine.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Column(length = 255, nullable = false)
    private String name;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private List<Post> posts;
}
