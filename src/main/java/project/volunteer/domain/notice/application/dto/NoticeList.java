package project.volunteer.domain.notice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.domain.Notice;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NoticeList {

    private Long no;
    private String createdAt;
    private String createdTime;
    private String content;
    private Integer checkCnt;
    private Integer commentsCnt;
    private Boolean isChecked;

    public static NoticeList toDto(Notice notice, Boolean isChecked){
        NoticeList dto = new NoticeList();
        dto.no = notice.getNoticeNo();
        dto.createdAt = notice.getCreatedDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        dto.createdTime = notice.getCreatedDate().format(DateTimeFormatter.ofPattern("HH-mm"));
        dto.content = notice.getContent();
        dto.checkCnt = notice.getCheckedNum();
        dto.commentsCnt = notice.getCommentNum();
        dto.isChecked = isChecked;
        return dto;
    }
}
