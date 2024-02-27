package project.volunteer.domain.recruitment.application.dto.query.list;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentListSearchResult {
    private List<RecruitmentList> recruitmentList;
    private Boolean isLast;
    private Long lastId;

}
