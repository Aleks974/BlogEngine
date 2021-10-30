package diplom.blogengine.model;

import diplom.blogengine.api.request.GlobalSettingsRequest;

public enum SettingsCode {
    MULTIUSER_MODE {
        public boolean getValueFromRequest(GlobalSettingsRequest request) {
            return request.getMultiUserMode();
        }
    },

    POST_PREMODERATION {
        public boolean getValueFromRequest(GlobalSettingsRequest request) {
            return request.getPostPreModeration();
        }
    },

    STATISTICS_IS_PUBLIC {
        public boolean getValueFromRequest(GlobalSettingsRequest request) {
            return request.getStatisticsIsPublic();
        }
    };

    public abstract boolean getValueFromRequest(GlobalSettingsRequest request);
}
