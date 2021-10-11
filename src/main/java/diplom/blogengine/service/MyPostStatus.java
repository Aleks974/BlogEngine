package diplom.blogengine.service;

import diplom.blogengine.model.ModerationStatus;

public enum MyPostStatus {
    INACTIVE(false, null),
    PENDING(true, ModerationStatus.NEW),
    DECLINED(true, ModerationStatus.DECLINED),
    PUBLISHED(true, ModerationStatus.ACCEPTED);

    private final boolean isActiveFlag;
    private final ModerationStatus moderationStatus;

    MyPostStatus(boolean isActiveFlag, ModerationStatus moderationStatus) {
        this.isActiveFlag = isActiveFlag;
        this.moderationStatus = moderationStatus;
    }

    public boolean isActiveFlag() {
        return isActiveFlag;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }
}
