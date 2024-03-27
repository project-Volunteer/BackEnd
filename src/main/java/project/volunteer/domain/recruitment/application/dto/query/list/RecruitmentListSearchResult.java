package project.volunteer.domain.recruitment.application.dto.query.list;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentListSearchResult {
    private List<RecruitmentList> recruitmentList;
    private Boolean isLast;
    private Long lastId;

    public static RecruitmentListSearchResult from(Slice<RecruitmentList> recruitmentSlice) {
        List<RecruitmentList> content = recruitmentSlice.getContent();
        Long lastId = content.stream()
                .map(RecruitmentList::getNo)
                .reduce((first, second) -> second)
                .orElse(0L);

        return new RecruitmentListSearchResult(content, recruitmentSlice.isLast(), lastId);
    }

}
