package diplom.blogengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import diplom.blogengine.service.util.IContentProcessor;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Column(name = "is_active", columnDefinition = "BIT(1)", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", columnDefinition = "ENUM('NEW', 'ACCEPTED', 'DECLINED')", nullable = false)
    private ModerationStatus moderationStatus;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "moderator_id", foreignKey = @ForeignKey(name="POSTS_MODERATOR_ID_FK"), nullable = true)
    private User moderator;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="POSTS_USER_ID_FK"), nullable = false)
    private User user;

    @Column(columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime time;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostVote> votes;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tag2post",
               joinColumns = { @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "TAG2POST_POST_ID_FK")) },
               inverseJoinColumns = { @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "TAG2POST_TAG_ID_FK")) } )
    private List<Tag> tags;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostComment> comments;

    @Transient
    private static final int ANNOUNCE_LENGTH = 150;

    public long getTimestamp(TimeZone timeZone) {
        Objects.requireNonNull(timeZone);
        LocalDateTime dateTime = Objects.requireNonNull(time, "Time is null for post " + id);
        return dateTime.atZone(timeZone.toZoneId()).toEpochSecond();
    }

    public String getAnnounce(IContentProcessor contentProcessor) {
        Objects.requireNonNull(contentProcessor);
        String announce = Objects.requireNonNull(text, "Text is null for post " + id);
        String safeAnnounce = contentProcessor.clearAllTags(announce);
        int len = safeAnnounce.length();
        return safeAnnounce.substring(0, len > ANNOUNCE_LENGTH ? ANNOUNCE_LENGTH : len);
    }

}
