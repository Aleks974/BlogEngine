package diplom.blogengine.repository;

import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.model.SettingsCode;
import diplom.blogengine.model.Tag;
import diplom.blogengine.model.dto.TagCountDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class CachedSettingsRepository {
    private final SettingsRepository settingsRepository;
    private final ConcurrentMap<String, List<GlobalSetting>> cacheStoreSettings = new ConcurrentHashMap<>();

    public CachedSettingsRepository(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void clearAllCache() {
        cacheStoreSettings.clear();
    }

    public List<GlobalSetting> findAll() {
        final String key = "settings";
        return cacheStoreSettings.computeIfAbsent(key, k -> Collections.unmodifiableList(settingsRepository.findAll()));
    }

    public GlobalSetting saveAndFlush(GlobalSetting setting) {
        return settingsRepository.saveAndFlush(setting);
    }

    public GlobalSetting findByCode(SettingsCode settingsCode) {
        return settingsRepository.findByCode(settingsCode);
    }
}
