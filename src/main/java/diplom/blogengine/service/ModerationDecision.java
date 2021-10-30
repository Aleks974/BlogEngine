package diplom.blogengine.service;

import diplom.blogengine.model.ModerationStatus;

public enum ModerationDecision {
    ACCEPT(ModerationStatus.ACCEPTED),
    DECLINE(ModerationStatus.DECLINED);

    private final ModerationStatus moderationStatus;

    ModerationDecision(ModerationStatus moderationStatus){
        this.moderationStatus = moderationStatus;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }
}