package project.volunteer.domain.notice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.response.NoticeDetailsResponse;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.api.dto.response.NoticeListResponse;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.mapper.NoticeFacade;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.common.dto.CommentContentParam;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class NoticeController {
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
        List<NoticeDetails> noticeListDto = noticeFacade.findVolunteerPostNoticeListDto(SecurityUtil.getLoginUserNo(), recruitmentNo);
        return ResponseEntity.ok(new NoticeListResponse(noticeListDto));
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity<NoticeDetailsResponse> noticeDetails(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){
        NoticeDetailsResponse response = noticeFacade.findVolunteerPostNoticeDetailsDto(SecurityUtil.getLoginUserNo(), recruitmentNo, noticeNo);

        return ResponseEntity.ok(response);
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/read")
    public ResponseEntity noticeRead(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){
        noticeFacade.readVolunteerPostNotice(SecurityUtil.getLoginUserNo(), recruitmentNo, noticeNo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}/cancel")
    public ResponseEntity noticeReadCancel(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){
        noticeFacade.readCancelVolunteerPostNotice(SecurityUtil.getLoginUserNo(), recruitmentNo, noticeNo);
        return ResponseEntity.ok().build();
    }


    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/comment")
    public ResponseEntity noticeCommentAdd(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @Valid @RequestBody CommentContentParam dto){
        noticeFacade.addVolunteerPostNoticeComment(SecurityUtil.getLoginUserNo(), recruitmentNo, noticeNo, dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{parentNo}/reply")
    public ResponseEntity noticeCommentReplyAdd(@PathVariable Long recruitmentNo,
                                                @PathVariable Long noticeNo,
                                                @PathVariable Long parentNo,
                                                @Valid @RequestBody CommentContentParam dto){
        noticeFacade.addVolunteerPostNoticeCommentReply(SecurityUtil.getLoginUserNo(), recruitmentNo, noticeNo, parentNo, dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
    @PutMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}")
    public ResponseEntity noticeReplyEdit(@PathVariable Long recruitmentNo,
                                          @PathVariable Long noticeNo,
                                          @PathVariable Long replyNo,
                                          @Valid @RequestBody CommentContentParam dto){
        noticeFacade.editVolunteerPostNoticeCommentOrReply(SecurityUtil.getLoginUserNo(), recruitmentNo, replyNo, dto.getContent());
        return ResponseEntity.ok().build();
    }
    @OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}")
    public ResponseEntity noticeReplyDelete(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @PathVariable Long replyNo){

        noticeFacade.deleteVolunteerPostNoticeCommentOrReply(recruitmentNo, noticeNo, replyNo);
        return ResponseEntity.ok().build();
    }
}
