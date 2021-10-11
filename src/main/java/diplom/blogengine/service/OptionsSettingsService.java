package diplom.blogengine.service;

import diplom.blogengine.api.response.InitOptionsResponse;
import diplom.blogengine.api.response.mapper.InitOptionsResponseMapper;
import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.repository.SettingsRepository;
import diplom.blogengine.service.util.cache.GlobalSettingsCacheHandler;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OptionsSettingsService implements IOptionsSettingsService {
    private final String YES_VALUE = "YES";

    private final SettingsRepository settingsRepository;
    private final InitOptionsResponseMapper initOptionsResponseMapper;
    private final GlobalSettingsCacheHandler globalSettingsCache;

    public OptionsSettingsService(SettingsRepository settingsRepository,
                           InitOptionsResponseMapper initOptionsResponseMapper,
                           GlobalSettingsCacheHandler globalSettingsCache) {
        this.settingsRepository = settingsRepository;
        this.initOptionsResponseMapper = initOptionsResponseMapper;
        this.globalSettingsCache = globalSettingsCache;
    }

    @Override
    public Map<String, Boolean> getGlobalSettings() {
        return globalSettingsCache.getCached().orElseGet(() -> {
            List<GlobalSetting> list = settingsRepository.findAll();
            return globalSettingsCache.cache(Collections.unmodifiableMap(convertListToMap(list)));
        });
    }

    private Map<String, Boolean> convertListToMap(List<GlobalSetting> list) {
        return list.stream().collect(
                Collectors.toMap(s -> s.getCode().toString(), s -> s.getValue().equalsIgnoreCase(YES_VALUE))
        );
    }

    @Override
    public InitOptionsResponse getInitOptions() {
        return initOptionsResponseMapper.initOptionsResponse();
    }

    @Override
    public boolean multiUserModeIsEnabled() {
        final String MULTIUSER_MODE_KEY = "MULTIUSER_MODE";
        Map<String, Boolean> settings = getGlobalSettings();
        Boolean modeAllowed = settings.get(MULTIUSER_MODE_KEY);
        return (modeAllowed != null) && modeAllowed;
    }

}
