package project.volunteer.domain.reply.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.reply.application.dto.CommentDetails;
import project.volunteer.domain.reply.application.dto.CommentReplyDetails;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.dao.queryDto.ReplyQueryDtoRepository;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
	private final ReplyRepository replyRepository;
	private final ReplyQueryDtoRepository replyQueryDtoRepository;

	@Override
    @Transactional
	public Long addComment(User user, RealWorkCode code, Long no, String content) {
		Reply reply = Reply.createComment(code, no, content);
		reply.setWriter(user);
		
		replyRepository.save(reply);
		return reply.getReplyNo();
	}

	@Override
	@Transactional
	public Long addCommentReply(User user, RealWorkCode code, Long no, Long parentNo, String content) {
		// 부모 댓글 유무 확인
		Reply findComment = validateAndGetParentReply(parentNo);
		
		// 부모 댓글이 1depth 댓글인지 확인
		validateParentReplyHasNotParent(findComment);

		Reply reply = Reply.createCommentReply(findComment, code, no, content);
		reply.setWriter(user);

		replyRepository.save(reply);
		return reply.getReplyNo();
	}

	@Override
	@Transactional
	public void editReply(Long replyNo, String content) {
		// 댓글 존재 여부 검증
		Reply findReply = validateAndGetReply(replyNo);

		findReply.editReply(content);
	}

	@Override
	@Transactional
	public void deleteReply(Long replyNo) {
		// 댓글 존재 여부 검증
		Reply findReply = validateAndGetReply(replyNo);

		replyRepository.delete(findReply);
	}

	@Override
	public List<CommentDetails> getCommentReplyListDto(RealWorkCode code, Long no) {
		List<CommentMapperDto> commentMapperDtos = replyQueryDtoRepository.getCommentMapperDtos(code, no);

		//부모-자식 댓글 매핑
		List<CommentDetails> commentDetailList = new ArrayList<>();
		Map<Long, CommentDetails> map = new HashMap<>();

		for(CommentMapperDto mapperDto : commentMapperDtos){
			//부모 댓글
			if(mapperDto.getParentNo()==null){
				CommentDetails commentDetails = CommentDetails.from(mapperDto);

				commentDetailList.add(commentDetails);
				map.put(mapperDto.getNo(), commentDetails);
			}
			//자식 댓글
			else{
				CommentReplyDetails commentReplyDetails = CommentReplyDetails.from(mapperDto);
				map.get(mapperDto.getParentNo()).addReplyDetails(commentReplyDetails);
			}
		}
		return commentDetailList;
	}

	private Reply validateAndGetReply (Long replyNo) {
		return replyRepository.findById(replyNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_REPLY,
						String.format("not found reply = [%d]", replyNo)));
	}
	private Reply validateAndGetParentReply (Long parentNo) {
		return replyRepository.findVaildParentReply(parentNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_PARENT_REPLY,
						String.format("not found parent reply replyno= [%d]", parentNo)));
	}
	private void validateParentReplyHasNotParent(Reply findComment) {
		if(findComment.getParent() != null) {
			throw new BusinessException(ErrorCode.ALREADY_HAS_PARENT_REPLY,
					String.format("already parent reply replyno=[%d]", findComment.getParent().getReplyNo()));
		}
	}
}
