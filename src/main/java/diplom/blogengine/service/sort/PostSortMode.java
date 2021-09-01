package diplom.blogengine.service.sort;

public enum PostSortMode {
    RECENT,
    POPULAR,
    BEST,
    EARLY;

    public boolean isPopular() {
        return this == POPULAR;
    }

    public boolean isBest() {
        return this == BEST;
    }
}
