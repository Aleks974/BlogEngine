package diplom.blogengine.model;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "global_settings")
public class GlobalSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MULTIUSER_MODE', 'POST_PREMODERATION', 'STATISTICS_IS_PUBLIC')", length = 255, nullable = false, unique = true)
    private SettingsCode code;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String value;

}
