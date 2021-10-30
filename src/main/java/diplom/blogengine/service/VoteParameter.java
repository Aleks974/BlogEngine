package diplom.blogengine.service;


public enum VoteParameter {
    LIKE(1),
    DISLIKE(-1);

    private final int value;

    VoteParameter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
