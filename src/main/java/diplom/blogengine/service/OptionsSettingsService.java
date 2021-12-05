package diplom.blogengine.service;

import diplom.blogengine.api.request.GlobalSettingsRequest;
import diplom.blogengine.api.response.InitOptionsResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.mapper.InitOptionsResponseMapper;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.model.SettingsCode;
import diplom.blogengine.repository.CachedSettingsRepository;
import diplom.blogengine.repository.SettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OptionsSettingsService implements IOptionsSettingsService {
    private final SettingsRepository settingsRepository;
    private final CachedSettingsRepository cachedSettingsRepository;
    private final InitOptionsResponseMapper initOptionsResponseMapper;
    private final ResultResponseMapper resultResponseMapper;

    private static final String YES_VALUE = "YES";
    private static final String NO_VALUE = "NO";
    private static final String MULTIUSER_MODE_KEY = "MULTIUSER_MODE";
    private static final String POST_PREMODERATION_KEY = "POST_PREMODERATION";
    private static final String STATISTICS_IS_PUBLIC_KEY = "STATISTICS_IS_PUBLIC";
    private static final boolean MULTIUSER_MODE_DEFAULT = false;
    private static final boolean POST_PREMODERATION_DEFAULT = true;
    private static final boolean STATISTICS_IS_PUBLIC_DEFAULT = false;

    public OptionsSettingsService(SettingsRepository settingsRepository,
                                  CachedSettingsRepository cachedSettingsRepository,
                                  InitOptionsResponseMapper initOptionsResponseMapper,
                                  ResultResponseMapper resultResponseMapper) {
        this.settingsRepository = settingsRepository;
        this.cachedSettingsRepository = cachedSettingsRepository;
        this.initOptionsResponseMapper = initOptionsResponseMapper;
        this.resultResponseMapper = resultResponseMapper;
    }

    @Override
    public InitOptionsResponse getInitOptions() {
        log.debug("enter getInitOptions()");

        return initOptionsResponseMapper.initOptionsResponse();
    }

    @Override
    public Map<String, Boolean> getGlobalSettings() {
        log.debug("enter getGlobalSettings()");

        return getCachedSettingsOrInit();
    }

    private Map<String, Boolean> getCachedSettingsOrInit() {
        log.debug("enter getCachedSettingsOrInit()");
        return convertListToMap(cachedSettingsRepository.findAll());
    }

    private Map<String, Boolean> convertListToMap(List<GlobalSetting> list) {
        return list.stream().collect(
                Collectors.toMap(s -> s.getCode().toString(), s -> s.getValue().equalsIgnoreCase(YES_VALUE))
        );
    }

    @Override
    public boolean multiUserModeIsEnabled() {
        log.debug("enter multiUserModeIsEnabled()");

        Map<String, Boolean> settings = getCachedSettingsOrInit();
        return settings.getOrDefault(MULTIUSER_MODE_KEY, MULTIUSER_MODE_DEFAULT);
    }


    @Override
    public boolean postPremoderationIsEnabled() {
        log.debug("enter postPremoderationIsEnabled()");

        Map<String, Boolean> settings = getCachedSettingsOrInit();
        return settings.getOrDefault(POST_PREMODERATION_KEY, POST_PREMODERATION_DEFAULT);
    }

    @Override
    public boolean statisticsIsPublic() {
        log.debug("enter statisticsIsPublic()");

        Map<String, Boolean> settings = getCachedSettingsOrInit();
        return settings.getOrDefault(STATISTICS_IS_PUBLIC_KEY, STATISTICS_IS_PUBLIC_DEFAULT);
    }

    @Transactional
    @Override
    public ResultResponse updateSettings(GlobalSettingsRequest globalSettingsRequest) {
        Objects.requireNonNull(globalSettingsRequest);

        List<GlobalSetting> settings = cachedSettingsRepository.findAll();
        for (GlobalSetting setting : settings) {
            SettingsCode code = setting.getCode();
            String value = code.getValueFromRequest(globalSettingsRequest) ? YES_VALUE : NO_VALUE;
            setting.setValue(value);
        }
        settingsRepository.saveAll(settings);

        cachedSettingsRepository.clearAllCache();

        return resultResponseMapper.success();
    }

}
