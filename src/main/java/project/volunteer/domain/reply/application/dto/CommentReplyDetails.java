package project.volunteer.domain.reply.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class CommentReplyDetails {
    private Long no;
    private String profile;
    private String nickName;
    private String replyDate;
    private String replyTime;
    private String content;

    public static CommentReplyDetails from(CommentMapperDto dto){
        CommentReplyDetails commentReplyDetails = new CommentReplyDetails();
        commentReplyDetails.no = dto.getNo();
        commentReplyDetails.profile = dto.getProfile();
        commentReplyDetails.nickName = dto.getNickname();
        commentReplyDetails.replyDate =  dto.getCommentTime().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        commentReplyDetails.replyTime = dto.getCommentTime().format(DateTimeFormatter.ofPattern("HH-mm"));
        commentReplyDetails.content = dto.getContent();
        return commentReplyDetails;
    }
}
