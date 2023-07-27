package project.volunteer.domain.reply.dao.queryDto;

import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;

public interface ReplyQueryDtoRepository {
    /**
     * - 댓글(부모)을 가장 처음에, 대댓글은 그 뒤에 정렬. 비지니스 로직에서 매핑시켜줄 예정.
     * grouping, rollup 등을 사용해서 쿼리를 통해 가져올수도 있지만 좋지 않은 거 같음.
     * - 사용자 프로필 이미지 같이 가져오기(기본 프로필, 업로드 프로필 고려)
     */
    List<CommentMapperDto> getCommentMapperDtos(RealWorkCode code, Long no);
}
