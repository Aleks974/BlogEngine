package diplom.blogengine.service;

import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.StatisticsResponse;
import diplom.blogengine.model.dto.StatPostsDto;
import diplom.blogengine.model.dto.StatVotesDto;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.PostVoteRepository;
import diplom.blogengine.security.UserDetailsExt;
import diplom.blogengine.service.util.TimestampHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
public class GeneralService implements IGeneralService {
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final TimestampHelper timestampHelper;

    public GeneralService(PostRepository postRepository,
                          PostVoteRepository postVoteRepository,
                          TimestampHelper timestampHelper) {
        this.postRepository = postRepository;
        this.postVoteRepository = postVoteRepository;
        this.timestampHelper = timestampHelper;
    }

    @Override
    public StatisticsResponse getMyStatistics(long authUserId) {
        log.debug("enter getMyStatistics()");

        StatPostsDto statPosts = postRepository.getMyPostsStatistics(authUserId);
        log.debug("StatPostsDto: {}", statPosts.toString());

        StatVotesDto statVotes = postVoteRepository.getMyVotesStatistics(authUserId);
        log.debug("StatVotesDto: {}", statVotes.toString());

        return getStatisticsData(statPosts, statVotes);
    }

    @Override
    public StatisticsResponse getAllStatistics() {
        log.debug("enter getAllStatistics()");

        StatPostsDto statPosts = postRepository.getAllPostsStatistics();
        StatVotesDto statVotes = postVoteRepository.getAllVotesStatistics();
        return getStatisticsData(statPosts, statVotes);
    }

    private StatisticsResponse getStatisticsData(StatPostsDto statPosts, StatVotesDto statVotes) {
        log.debug("enter getStatisticsData()");

        Objects.requireNonNull(statPosts, "statPosts is null");
        Objects.requireNonNull(statVotes, "statVotes is null");

        long postsCount = statPosts.getPostsCount();
        long viewsCount = statPosts.getViewsCount();
        long firstPublication = statPosts.getFirstPublication() != null ?
                timestampHelper.toTimestampAtServerZone(statPosts.getFirstPublication()) : 0;
        long likesCount = statVotes.getLikesCount();
        long dislikesCount = statVotes.getDislikesCount();

        return StatisticsResponse
                .builder()
                .postsCount(postsCount)
                .likesCount(likesCount)
                .dislikesCount(dislikesCount)
                .viewsCount(viewsCount)
                .firstPublication(firstPublication)
                .build();
    }

}
