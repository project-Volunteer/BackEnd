package project.volunteer.domain.notice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.response.NoticeDetailsResponse;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.api.dto.response.NoticeListResponse;
import project.volunteer.domain.notice.application.NoticeDtoService;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.mapper.NoticeFacade;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.reply.application.dto.CommentDetails;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.dto.CommentContentParam;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeDtoService noticeDtoService;
    private final ReplyService replyService;
    private final NoticeFacade noticeFacade;

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PostMapping("/{recruitmentNo}/notice")
    public ResponseEntity noticeAdd(@PathVariable Long recruitmentNo, @RequestBody @Valid NoticeAdd dto){

        noticeFacade.registerVolunteerPostNotice(recruitmentNo, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity noticeEdit(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @RequestBody @Valid NoticeEdit dto){
        noticeFacade.editVolunteerPostNotice(recruitmentNo, noticeNo, dto);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity noticeDelete(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){
        noticeFacade.deleteVolunteerPostNotice(recruitmentNo, noticeNo);
        return ResponseEntity.ok().build();
    }




    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/notice")
    public ResponseEntity<NoticeListResponse> noticeList(@PathVariable Long recruitmentNo){

        List<NoticeDetails> noticeDtos = noticeDtoService.findNoticeDtos(recruitmentNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(new NoticeListResponse(noticeDtos));
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity<NoticeDetailsResponse> noticeDetails(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){
        //공지사항 상세 조회
        NoticeDetails noticeDto = noticeDtoService.findNoticeDto(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());

        //댓글 리스트 조회
        List<CommentDetails> commentReplyList = replyService.getCommentReplyList(RealWorkCode.NOTICE, noticeNo);

        return ResponseEntity.ok(new NoticeDetailsResponse(noticeDto, commentReplyList));
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/read")
    public ResponseEntity noticeRead(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){

        noticeService.readNotice(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}/cancel")
    public ResponseEntity noticeReadCancel(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){

        noticeService.readCancelNotice(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }


    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/comment")
    public ResponseEntity noticeCommentAdd(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @Valid @RequestBody CommentContentParam dto){

        noticeService.addNoticeComment(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo(), dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{parentNo}/reply")
    public ResponseEntity noticeCommentReplyAdd(@PathVariable Long recruitmentNo,
                                                @PathVariable Long noticeNo,
                                                @PathVariable Long parentNo,
                                                @Valid @RequestBody CommentContentParam dto){

        noticeService.addNoticeCommentReply(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo(), parentNo, dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
    @PutMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}")
    public ResponseEntity noticeReplyEdit(@PathVariable Long recruitmentNo,
                                          @PathVariable Long noticeNo,
                                          @PathVariable Long replyNo,
                                          @Valid @RequestBody CommentContentParam dto){

        noticeService.editNoticeReply(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo(), replyNo, dto.getContent());
        return ResponseEntity.ok().build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}")
    public ResponseEntity noticeReplyDelete(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @PathVariable Long replyNo){

        noticeService.deleteNoticeReply(recruitmentNo, noticeNo, replyNo);
        return ResponseEntity.ok().build();
    }

}
