package diplom.blogengine.exception;

public class PostNotFoundException extends RuntimeException {
    private long postId;

    public PostNotFoundException(long postId) {
        super("Post " + postId + " not found");
        this.postId = postId;
    }

    public PostNotFoundException(String message, long postId) {
        super(message);
        this.postId = postId;
    }

    public long getPostId() {
        return postId;
    }
}
