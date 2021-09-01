package diplom.blogengine.service;

import diplom.blogengine.api.response.InitOptionsResponse;

import java.util.Map;

public interface IOptionsSettingsService {
    Map<String, Boolean> getGlobalSettings();
    InitOptionsResponse getInitOptions();
}
