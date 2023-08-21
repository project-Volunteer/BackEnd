package project.volunteer.global.common.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Component
@RequiredArgsConstructor
//TODO: 댓글 서비스 레이어 재사용으로 리팩토링 필요
public class ReplyValidate {
    private final ReplyRepository replyRepository;

    // 댓글 유무 확인
    public Reply validateAndGetReply (Long replyNo) {
        return replyRepository.findById(replyNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_REPLY,
                        String.format("not found reply = [%d]", replyNo)));
    }

    // 댓글 작성자 여부 체크
    public void vaildateEqualParamUserNoAndReplyFindUserNo(Long ParamUserNo, Reply findReply) {
        if(!ParamUserNo.equals(findReply.getWriter().getUserNo())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_REPLY,
                    String.format("forbidden replyno userno=[%d], replyno=[%d]", ParamUserNo, findReply.getReplyNo()));
        }
    }

}
