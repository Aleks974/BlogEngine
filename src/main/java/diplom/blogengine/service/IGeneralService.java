package diplom.blogengine.service;

import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.StatisticsResponse;
import diplom.blogengine.security.UserDetailsExt;

import java.util.Locale;

public interface IGeneralService {
    StatisticsResponse getMyStatistics(long authUserId);

    StatisticsResponse getAllStatistics();

 }
