package diplom.blogengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;

@Data
@Entity
@Table(name = "post_comments")
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "POSTCOMMENTS_PARENT_ID_FK"))
    private PostComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "POSTCOMMENTS_POST_ID_FK"), nullable = false)
    private Post post;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "POSTCOMMENTS_USER_ID_FK"), nullable = false)
    private User user;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime time;


    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;


    public long getTimestamp(TimeZone timeZone) {
        Objects.requireNonNull(timeZone);
        LocalDateTime dateTime = Objects.requireNonNull(time, "Time is null for post " + id);
        return dateTime.atZone(timeZone.toZoneId()).toEpochSecond();
    }
}
