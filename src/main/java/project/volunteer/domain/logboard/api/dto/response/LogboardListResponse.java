package project.volunteer.domain.logboard.api.dto.response;

import java.util.List;

import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class LogboardListResponse {

    private List<LogboardListQuery> logboardList;
    private Boolean isLast;
    private Long lastId;
	public LogboardListResponse(List<LogboardListQuery> logboardList, Boolean isLast, Long lastId) {
		this.logboardList = logboardList;
		this.isLast = isLast;
		this.lastId = lastId;
	}
    
    
    
    
}
