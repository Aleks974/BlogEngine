package diplom.blogengine.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_votes")
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "POSTVOTES_USER_ID_FK"), nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "POSTVOTES_POST_ID_FK"), nullable = false)
    private Post post;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime time;

    @Column(columnDefinition = "TINYINT", nullable = false)
    private int value;

}
