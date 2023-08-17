package project.volunteer.domain.logboard.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;
import project.volunteer.domain.reply.application.dto.CommentDetails;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LogboardDetailResponse {
    LogboardDetail log;
    List<CommentDetails> commentsList;
}
