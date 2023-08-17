package project.volunteer.domain.logboard.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.websocket.server.PathParam;

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
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;
import project.volunteer.domain.logboard.api.dto.response.*;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;
import project.volunteer.domain.reply.application.dto.CommentDetails;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.user.api.dto.response.UserInfo;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.dto.CommentContentParam;
import project.volunteer.domain.logboard.api.dto.request.LogBoardRequestParam;
import project.volunteer.domain.logboard.application.LogboardService;
import project.volunteer.domain.logboard.application.dto.LogboardEditDetail;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.image.domain.Storage;
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
	private final ReplyRepository replyRepository;
	private final UserService userService;
	
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
			ImageParam uploadLogboardImg = new ImageParam(RealWorkCode.LOG, logboardNo, file);
			imageService.addImage(uploadLogboardImg);
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@GetMapping("/logboard/edit/{no}")
	public ResponseEntity<LogboardEditDetailResponse> logboardDetails(@PathVariable Long no) {
		List<String> imagePath = new ArrayList<>();
		
		LogboardEditDetail logboardDetail = logboardService.findLogboard(no);

		List<Image> logImages = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, no);
		for(Image logImage : logImages) {
			imagePath.add(logImage.getStorage().getImagePath());
		}
		
		logboardDetail.setPicture(imagePath);
		LogboardEditDetailResponse logboardDetailResponse = new LogboardEditDetailResponse(logboardDetail);
		
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
			ImageParam uploadLogboardImg = new ImageParam(RealWorkCode.LOG, no, file);
			imageService.addImage(uploadLogboardImg);
		}
		
        return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/logboard/{no}")
	public ResponseEntity logboardDelete(@PathVariable Long no) {
		logboardService.deleteLog(SecurityUtil.getLoginUserNo(), no);
		
		return ResponseEntity.ok().build();
	}


	@GetMapping("/logboard/{no}")
	public ResponseEntity<LogboardDetailResponse> logboardDetail(@PathVariable Long no) {
		List<String> imagePath = new ArrayList<>();
		LogboardDetail logboardDetail = logboardService.detailLog(no);

		UserInfo userInfo = userService.findUserInfo(logboardDetail.getWriterNo());
		logboardDetail.setWriterInfo(userInfo);

		List<Image> logImages = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, no);
		for(Image logImage : logImages) {
			imagePath.add(logImage.getStorage().getImagePath());
		}
		logboardDetail.setPicture(imagePath);

		List<CommentDetails> commentReplyList = replyService.getCommentReplyListDto(RealWorkCode.LOG, no);

		return ResponseEntity.ok(new LogboardDetailResponse(logboardDetail, commentReplyList));
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
									l.getLikeCnt(),l.isLikeMe());
			
			// 이미지 쿼리 조회 및 response 리턴용 이미지 개별 인스턴스 생성 
			List<Image> logboardImageList = imageRepository.findImagesByCodeAndNo(RealWorkCode.LOG, l.getWriterNo());
			logboardList.setPicturesFromImageDomain(logboardImageList);

			List<Reply> findReplyList = replyRepository.findReplyList(RealWorkCode.LOG, l.getNo())
										.stream()
										.filter(r->r.getIsDeleted().equals(IsDeleted.N))
										.collect(Collectors.toList());
			logboardList.setCommentCnt(findReplyList.size());
			
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

	@PostMapping("/logboard/{no}/like")
	public ResponseEntity logboardLike(@PathVariable Long no) {
		logboardService.likeLogboard(SecurityUtil.getLoginUserNo(), no);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/logboard/{logNo}/comment")
	public ResponseEntity logboardCommentAdd(@RequestBody @Valid CommentContentParam dto,
											 @PathVariable Long logNo) {
		//TODO: 퍼사드 패턴 도입전 임시 방편.
		User user = userService.findUser(SecurityUtil.getLoginUserNo());
		replyService.addComment(user, RealWorkCode.LOG, logNo, dto.getContent());

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}


	@PostMapping("/logboard/{logNo}/comment/{parentNo}/reply")
	public ResponseEntity logboardCommenReplytAdd(@RequestBody @Valid CommentContentParam dto,
												  @PathVariable Long logNo,
												  @PathVariable Long parentNo) {
		//TODO: 퍼사드 패턴 도입전 임시 방편.
		User user = userService.findUser(SecurityUtil.getLoginUserNo());
		replyService.addCommentReply(user, RealWorkCode.LOG, logNo, parentNo, dto.getContent());

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}


	@OrganizationAuth(auth = OrganizationAuth.Auth.REPLY_WRITER)
	@PutMapping("/logboard/{logNo}/comment/{replyNo}")
	public ResponseEntity logboardReplytEdit(@RequestBody @Valid CommentContentParam dto,
											 @PathVariable Long logNo,
											 @PathVariable Long replyNo) {
		replyService.editReply(replyNo, dto.getContent());

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
