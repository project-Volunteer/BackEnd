package project.volunteer.domain.logboard.api;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.logboard.api.dto.request.CommentContentParam;
import project.volunteer.domain.logboard.api.dto.request.LogBoardRequestParam;
import project.volunteer.domain.logboard.api.dto.response.AddableLogboardListResponse;
import project.volunteer.domain.logboard.api.dto.response.LogboardDetailResponse;
import project.volunteer.domain.logboard.api.dto.response.LogboardList;
import project.volunteer.domain.logboard.api.dto.response.LogboardListResponse;
import project.volunteer.domain.logboard.application.LogboardService;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
public class LogboardController {
	private final LogboardService logboardService;
	private final ImageService imageService;
	private final FileService fileService;
	private final ImageRepository imageRepository;
	private final LogboardRepository logboardRepository;
	private final ScheduleParticipationDtoService spDtoService ;
	private final ReplyService replyService;
	
	@GetMapping("/logboard/schedule")
	public ResponseEntity<AddableLogboardListResponse> approvalSchedule() {
		List<ParsingCompleteSchedule> completeScheduleList = spDtoService.findCompleteScheduleList(
				SecurityUtil.getLoginUserNo(), ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		
		return ResponseEntity.ok(new AddableLogboardListResponse(completeScheduleList));
	}
	
	@PostMapping("/logboard")
	public ResponseEntity logboardAdd(@ModelAttribute @Valid LogBoardRequestParam dto) {
		Long logboardNo= logboardService.addLog(SecurityUtil.getLoginUserNo(), dto.getContent(), dto.getScheduleNo(), dto.getIsPublished());

		for(MultipartFile file : dto.getUploadImage()) {
			imageService.deleteImage(RealWorkCode.LOG, logboardNo);
			ImageParam uploadLogboardImg = new ImageParam(RealWorkCode.LOG, logboardNo, ImageType.UPLOAD, null, file);
			imageService.addImage(uploadLogboardImg);
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@GetMapping("/logboard/edit/{no}")
	public ResponseEntity<LogboardDetailResponse> logboardDetails(@PathVariable Long no) {
		List<String> imagePath = new ArrayList<>();
		
		LogboardDetail logboardDetail = logboardService.findLogboard(no);

		List<Image> logImages = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, no);
		for(Image logImage : logImages) {
			imagePath.add(logImage.getStorage().getImagePath());
		}
		
		logboardDetail.setPicture(imagePath);
		LogboardDetailResponse logboardDetailResponse = new LogboardDetailResponse(logboardDetail);
		
		return ResponseEntity.ok(logboardDetailResponse);
	}
	
	@PostMapping("/logboard/edit/{no}")
	public ResponseEntity logboardEdit(@ModelAttribute @Valid LogBoardRequestParam dto,
									   @PathVariable Long no) {
		// log 업데이트
		logboardService.editLog(no, SecurityUtil.getLoginUserNo(), dto.getContent(), dto.getScheduleNo(), dto.getIsPublished());


		// 기존 이미지 파일 삭제
		List<Image> targetDeleteImgs = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, no);
		for(Image image : targetDeleteImgs) {
			Storage storage = image.getStorage();
			fileService.deleteFile(storage.getFakeImageName());
		}

		// log 이미지 업데이트
		for(MultipartFile file : dto.getUploadImage()) {
			imageService.deleteImage(RealWorkCode.LOG, no);
			ImageParam uploadLogboardImg = new ImageParam(RealWorkCode.LOG, no, ImageType.UPLOAD, null, file);
			imageService.addImage(uploadLogboardImg);
		}
		
        return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/logboard/{no}")
	public ResponseEntity logboardDelete(@PathVariable Long no) {
		logboardService.deleteLog(SecurityUtil.getLoginUserNo(), no);
		
		return ResponseEntity.ok().build();
	}

	
	@GetMapping("/logboard")
	public ResponseEntity<LogboardListResponse> logboardList(@PageableDefault(size = 6) Pageable pageable,
															 @RequestParam String search_type,
															 @RequestParam(required = false) Long last_id) {
		// 봉사 로그 쿼리 결과
		Slice<LogboardListQuery> logboardQueryResults = logboardRepository.findLogboardDtos(pageable, search_type, SecurityUtil.getLoginUserNo(), last_id);
		List<LogboardListQuery> logboardQueryResultLists =  new ArrayList<>(logboardQueryResults.toList());
		
		List<LogboardList> logboardLists =  new ArrayList<>();
		
		// 봉사 로그 쿼리 결과로 response 리턴 객체 생성 로직
		for(LogboardListQuery l : logboardQueryResultLists) {
			// response 리턴용 봉사로그 개별 인스턴스 생성
			LogboardList logboardList = 
					new LogboardList(l.getNo(),l.getWriterNo(),l.getProfile(),l.getNickname(),
									l.getCreatedDay(),l.getVolunteeringCategory(),l.getContent(),
									l.getLikeCnt(),l.isLikeMe(),l.getCommentCnt());
			
			// 이미지 쿼리 조회 및 response 리턴용 이미지 개별 인스턴스 생성 
			List<Image> logboardImageList = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, l.getWriterNo());
			logboardList.setPicturesFromImageDomain(logboardImageList);
			
			logboardLists.add(logboardList);
		}
		
		LogboardListResponse logBoardListResponse = 
				new LogboardListResponse(
						logboardLists,
						logboardQueryResults.isLast(), 
						(logboardLists.isEmpty()) ? null:(logboardLists.get(logboardLists.size()-1).getNo())
				);
		
		return ResponseEntity.ok(logBoardListResponse);
	}
	
	@PostMapping("/logboard/{logNo}/comment")
	public ResponseEntity logboardCommentAdd(@RequestBody @Valid CommentContentParam dto,
											 @PathVariable Long logNo) {
		replyService.addComment(SecurityUtil.getLoginUserNo(), RealWorkCode.LOG, logNo, dto.getContent());

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}


	@PostMapping("/logboard/{logNo}/comment/{parentNo}/reply")
	public ResponseEntity logboardCommenReplytAdd(@RequestBody @Valid CommentContentParam dto,
												  @PathVariable Long logNo,
												  @PathVariable Long parentNo) {
		replyService.addCommentReply(SecurityUtil.getLoginUserNo(), RealWorkCode.LOG, logNo, parentNo, dto.getContent());

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}


	@OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
	@PutMapping("/logboard/{logNo}/comment/{replyNo}")
	public ResponseEntity logboardReplytEdit(@RequestBody @Valid CommentContentParam dto,
											 @PathVariable Long logNo,
											 @PathVariable Long replyNo) {
		replyService.editReply(SecurityUtil.getLoginUserNo(), replyNo, dto.getContent());

		return ResponseEntity.ok().build();
	}

	@OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
	@DeleteMapping("/logboard/{logNo}/comment/{replyNo}")
	public ResponseEntity logboardReplytDelete(@PathVariable Long logNo,
											   @PathVariable Long replyNo) {
		replyService.deleteReply(replyNo);

		return ResponseEntity.ok().build();
	}
	
}
