package diplom.blogengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Column(name = "is_moderator", columnDefinition = "BIT(1)", nullable = false)
    private boolean isModerator;

    @Column(name = "reg_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime regTime;

    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255, nullable = false, unique = true)
    private String name;

    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255, nullable = false)
    private String password;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES `users` (`id`)")),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES `roles` (`id`)")))
    private Set<Role> roles;

/*    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255, nullable = true)
    private String code;*/

    @Column(nullable = true, columnDefinition = "TEXT")
    private String photo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Post> posts;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<PostVote> votes;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "user")
    private List<PostComment> comments;

    public User() {

    }

    public User(long id) {
        this.id = id;
    }

}
