package diplom.blogengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    private static final int EXPIRATION_IN_MIN = 60 * 2;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255, nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calcExpiryDate();
    }

    private LocalDateTime calcExpiryDate() {
        return LocalDateTime.now().plus(EXPIRATION_IN_MIN, ChronoUnit.MINUTES);
    }

    public boolean isExpired() {
        if (expiryDate == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
