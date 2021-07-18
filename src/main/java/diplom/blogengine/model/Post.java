package diplom.blogengine.model;

import diplom.blogengine.ModerationStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Column(name = "is_active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", columnDefinition = "ENUM('NEW', 'ACCEPTED', 'DECLINED')", nullable = false)
    private ModerationStatus moderationStatus;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "moderator_id", foreignKey = @ForeignKey(name="POSTS_MODERATOR_ID_FK"), nullable = true)
    private User moderator;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="POSTS_USER_ID_FK"), nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostVote> votes = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tag2post",
               joinColumns = { @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "TAG2POST_POST_ID_FK")) },
               inverseJoinColumns = { @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "TAG2POST_TAG_ID_FK")) } )
    private List<Tag> tags = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostComment> comments = new ArrayList<>();
}
