package project.volunteer.domain.reply.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class CommentDetails {
    private Long no;
    private String profile;
    private String nickName;
    private String commentDate;
    private String commentTime;
    private String content;
    private Integer commentsCnt;
    private List<CommentReplyDetails> replies;

    public static CommentDetails from(CommentMapperDto dto){
        CommentDetails commentDetails = new CommentDetails();
        commentDetails.no = dto.getNo();
        commentDetails.profile = dto.getProfile();
        commentDetails.nickName = dto.getNickname();
        commentDetails.commentDate = dto.getCommentTime().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        commentDetails.commentTime = dto.getCommentTime().format(DateTimeFormatter.ofPattern("HH-mm"));
        commentDetails.content = dto.getContent();

        commentDetails.commentsCnt = 0;
        commentDetails.replies = new ArrayList<>();
        return commentDetails;
    }

    public void addReplyDetails(CommentReplyDetails commentReplyDetails){
        replies.add(commentReplyDetails);
        commentsCnt++;
    }
}
