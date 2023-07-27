package project.volunteer.domain.reply.dao.queryDto.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentMapperDto {
    private Long no;
    private Long parentNo;
    private String profile;
    private String nickname;
    private String content;
    private LocalDateTime commentTime;

    @QueryProjection
    public CommentMapperDto(Long no, Long parentNo, String profile, String nickname, String content, LocalDateTime commentTime) {
        this.no = no;
        this.parentNo = parentNo;
        this.profile = profile;
        this.nickname = nickname;
        this.content = content;
        this.commentTime = commentTime;
    }

    @Override
    public String toString() {
        return "CommentMapperDto{" +
                "no=" + no +
                ", parentNo=" + parentNo +
                ", profile='" + profile + '\'' +
                ", nickname='" + nickname + '\'' +
                ", content='" + content + '\'' +
                ", commentTime=" + commentTime +
                '}';
    }
}
