package diplom.blogengine.model;

import lombok.*;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "POSTCOMMENTS_USER_ID_FK"), nullable = false)
    private User user;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime time;


    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
}
