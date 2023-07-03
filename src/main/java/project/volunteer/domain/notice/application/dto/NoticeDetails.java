package project.volunteer.domain.notice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.domain.Notice;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NoticeDetails {

    private NoticeList notice;

    //댓글 DTO 추가 필요

    public static NoticeDetails toDto(Notice notice, Boolean isChecked){
        NoticeDetails dto = new NoticeDetails();
        dto.notice = NoticeList.toDto(notice, isChecked);
        //댓글 DTO 추가 필요

        return dto;
    }
}
