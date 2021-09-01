package diplom.blogengine.repository;

import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.model.SettingsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<GlobalSetting, Long> {
    GlobalSetting findByCode(SettingsCode code);
}
