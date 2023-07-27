package project.volunteer.domain.notice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.reply.application.dto.CommentDetails;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoticeDetailsResponse {

    NoticeDetails notice;
    List<CommentDetails> commentsList;
}
