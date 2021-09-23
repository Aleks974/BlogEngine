package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.model.SettingsCode;
import diplom.blogengine.repository.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql",
        "classpath:db/migration/V1_2__insert_global_settings.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class SettingsRepositoryTest {

    @Autowired
    private SettingsRepository settingsRepository;

    @Test
    public void getSetting_WhenSaveAndRetrieveSetting_thenOk() {
        // given
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.MULTIUSER_MODE);
        setting.setValue("YES");
        settingsRepository.save(setting);

        // when
        GlobalSetting foundSetting = settingsRepository.findById(setting.getId()).get();

        // then
        assertNotNull(foundSetting);
        assertEquals(setting, foundSetting);
    }

}
