package project.volunteer.domain.notice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.notice.api.dto.NoticeAdd;
import project.volunteer.domain.notice.api.dto.NoticeDetailsResponse;
import project.volunteer.domain.notice.api.dto.NoticeEdit;
import project.volunteer.domain.notice.api.dto.NoticeListResponse;
import project.volunteer.domain.notice.application.NoticeDtoService;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeDtoService noticeDtoService;

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PostMapping("/{recruitmentNo}/notice")
    public ResponseEntity noticeAdd(@PathVariable Long recruitmentNo, @RequestBody @Valid NoticeAdd dto){

        noticeService.addNotice(recruitmentNo, dto);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity noticeEdit(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo, @RequestBody @Valid NoticeEdit dto){

        noticeService.editNotice(recruitmentNo, noticeNo, dto);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}")
    public ResponseEntity noticeDelete(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){

        noticeService.deleteNotice(recruitmentNo, noticeNo);
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

        NoticeDetails noticeDto = noticeDtoService.findNoticeDto(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());
        //댓글 details dto 추가 필요

        return ResponseEntity.ok(new NoticeDetailsResponse(noticeDto)); //댓글 details dto 추가 필요
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PostMapping("/{recruitmentNo}/notice/{noticeNo}/read")
    public ResponseEntity noticeRead(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){

        noticeService.readNotice(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @DeleteMapping("/{recruitmentNo}/notice/{noticeNo}/cancel")
    public ResponseEntity noticeReadCancel(@PathVariable Long recruitmentNo, @PathVariable Long noticeNo){

        noticeService.readCancelNotice(recruitmentNo, noticeNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

}
