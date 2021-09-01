package diplom.blogengine.service;

import diplom.blogengine.api.response.InitOptionsResponse;
import diplom.blogengine.api.response.mapper.InitOptionsResponseMapper;
import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.repository.SettingsRepository;
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

    OptionsSettingsService(SettingsRepository settingsRepository, InitOptionsResponseMapper initOptionsResponseMapper) {
        this.settingsRepository = settingsRepository;
        this.initOptionsResponseMapper = initOptionsResponseMapper;
    }

    @Override
    public Map<String, Boolean> getGlobalSettings() {
        List<GlobalSetting> list = settingsRepository.findAll();
        if (list != null) {
            return Collections.unmodifiableMap(convertListToMap(list));
        } else {
            return null;
        }
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


}
