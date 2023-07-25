package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HistoryListResponse {
    private List<HistoriesList> histories;
    private Boolean isLast;
    private Long lastId;

    public HistoryListResponse(List<HistoriesList> data, Boolean isLast, Long lastId){
        this.histories = data;
        this.isLast = isLast;
        this.lastId = lastId;
    }
}
