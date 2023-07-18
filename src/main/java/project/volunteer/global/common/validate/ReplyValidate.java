package project.volunteer.global.common.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class ReplyValidate {
    private final ReplyRepository replyRepository;
    private final LogboardValidate logboardValidate;

    // 댓글 유무 확인
    public Reply validateAndGetReply (Long replyNo) {
        return replyRepository.findById(replyNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_REPLY,
                        String.format("not found reply = [%d]", replyNo)));
    }

    // 부모 글 유무 확인
    public Reply validateAndGetParentReply (Long parentNo) {
        return replyRepository.findVaildParentReply(parentNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_PARENT_REPLY,
                        String.format("not found parent reply replyno= [%d]", parentNo)));
    }

    // 부모 댓글이 1depth 댓글인지 확인
    public void vaildateParentReplyHasNotParent(Reply findComment) {
        if(findComment.getParent() != null) {
            throw new BusinessException(ErrorCode.ALREADY_HAS_PARENT_REPLY,
                    String.format("already parent reply replyno=[%d]", findComment.getParent().getReplyNo()));
        }
    }

    // 댓글 작성자 여부 체크
    public void vaildateEqualParamUserNoAndReplyFindUserNo(Long ParamUserNo, Reply findReply) {
        if(!ParamUserNo.equals(findReply.getWriter().getUserNo())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_REPLY,
                    String.format("forbidden replyno userno=[%d], replyno=[%d]", ParamUserNo, findReply.getReplyNo()));
        }
    }


    // 댓글의 모글(도메인) 검증
    public void validateRealWorkDomain(RealWorkCode code, Long no) {
        if(code == RealWorkCode.LOG){
            logboardValidate.validateAndGetLogboard(no);
        }

    }


}
