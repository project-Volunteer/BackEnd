package project.volunteer.domain.reply.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.volunteer.domain.reply.application.dto.CommentDetails;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.dao.queryDto.ReplyQueryDtoRepository;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 * 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ReplyServiceImplTest {
    @Mock ReplyQueryDtoRepository replyQueryDtoRepository;
    @Mock ReplyRepository replyRepository;
    @InjectMocks ReplyServiceImpl replyService;

    @Test
    @DisplayName("댓글/대댓글 리스트 DTO 조회에 성공하다.")
    void findCommentAndCommentReplyDtos(){
        //given
        final CommentMapperDto dto1 = new CommentMapperDto(1L, null, "profile1", "nickname1", "content1", LocalDateTime.now());
        final CommentMapperDto dto2 = new CommentMapperDto(4L, null, "profile4", "nickname4", "content4", LocalDateTime.now());
        final CommentMapperDto dto3 = new CommentMapperDto(2L, 1L, "profile2", "nickname2", "content2", LocalDateTime.now());
        final CommentMapperDto dto4 = new CommentMapperDto(3L, 1L, "profile3", "nickname3", "content3", LocalDateTime.now());
        final List<CommentMapperDto> mapperDtos = List.of(dto1, dto2, dto3, dto4);

        given(replyQueryDtoRepository.getCommentMapperDtos(RealWorkCode.NOTICE, 1L))
                .willReturn(mapperDtos);

        //when
        List<CommentDetails> commentReplyList = replyService.getCommentReplyList(RealWorkCode.NOTICE, 1L);

        //then
        assertAll(
                () -> assertThat(commentReplyList.get(0).getNo()).isEqualTo(dto1.getNo()),
                () -> assertThat(commentReplyList.get(0).getProfile()).isEqualTo(dto1.getProfile()),
                () -> assertThat(commentReplyList.get(0).getNickName()).isEqualTo(dto1.getNickname()),
                () -> assertThat(commentReplyList.get(0).getContent()).isEqualTo(dto1.getContent()),
                () -> assertThat(commentReplyList.get(0).getCommentsCnt()).isEqualTo(2),
                () -> assertThat(commentReplyList.get(0).getReplies().get(0).getNo()).isEqualTo(dto3.getNo()),
                () -> assertThat(commentReplyList.get(0).getReplies().get(1).getNo()).isEqualTo(dto4.getNo()),

                () -> assertThat(commentReplyList.get(1).getNo()).isEqualTo(dto2.getNo()),
                () -> assertThat(commentReplyList.get(1).getProfile()).isEqualTo(dto2.getProfile()),
                () -> assertThat(commentReplyList.get(1).getNickName()).isEqualTo(dto2.getNickname()),
                () -> assertThat(commentReplyList.get(1).getContent()).isEqualTo(dto2.getContent()),
                () -> assertThat(commentReplyList.get(1).getCommentsCnt()).isEqualTo(0)
        );

        //verify
        verify(replyQueryDtoRepository,times(1)).getCommentMapperDtos(RealWorkCode.NOTICE, 1L);
    }

    @Test
    @DisplayName("댓글 등록에 성공하다.")
    void addNoticeComment(){
        //given
        final String content = "test";
        User user = mock(User.class);

        //when
        replyService.addComment(user, RealWorkCode.NOTICE, 1L, content);

        //then
        verify(replyRepository,times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("공지사항 대댓글 등록에 성공하다.")
    void addNoticeCommentReply(){
        //given
        final String content = "test";
        User user = mock(User.class);
        Reply parentReply = mock(Reply.class);

        given(replyRepository.findVaildParentReply(1L)).willReturn(Optional.of(parentReply));

        //when
        replyService.addCommentReply(user, RealWorkCode.NOTICE, 1L, 1L, content);

        //then
        verify(replyRepository,times(1)).findVaildParentReply(1L);
        verify(parentReply,times(1)).getParent();
        verify(replyRepository,times(1)).save(any(Reply.class));
    }

    @Test
    @DisplayName("공지사항 댓글 수정에 성공하다.")
    void editNoticeReply(){
        //given
        final String content = "test";
        Reply reply = mock(Reply.class);

        given(replyRepository.findById(1L)).willReturn(Optional.of(reply));

        //when
        replyService.editReply(1L, content);

        //then
        verify(replyRepository, times(1)).findById(1L);
        verify(reply, times(1)).editReply(content);
    }

    @Test
    @DisplayName("공지사항 댓글 삭제 성공하다.")
    void deleteNoticeReply(){
        //given
        final String content = "test";
        Reply reply = mock(Reply.class);

        given(replyRepository.findById(1L)).willReturn(Optional.of(reply));

        //when
        replyService.deleteReply(1L);

        //then
        verify(replyRepository, times(1)).findById(1L);
        verify(replyRepository, times(1)).delete(reply);
    }
}