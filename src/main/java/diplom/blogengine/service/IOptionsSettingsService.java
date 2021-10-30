package diplom.blogengine.service;

import diplom.blogengine.api.request.GlobalSettingsRequest;
import diplom.blogengine.api.response.InitOptionsResponse;
import diplom.blogengine.api.response.ResultResponse;

import java.util.Map;

public interface IOptionsSettingsService {
    Map<String, Boolean> getGlobalSettings();

    InitOptionsResponse getInitOptions();

    boolean multiUserModeIsEnabled();

    boolean postPremoderationIsEnabled();

    boolean statisticsIsPublic();

    ResultResponse updateSettings(GlobalSettingsRequest globalSettingsRequest);
}
