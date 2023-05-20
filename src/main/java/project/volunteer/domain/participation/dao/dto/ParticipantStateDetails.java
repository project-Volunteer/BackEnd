package project.volunteer.domain.participation.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.component.State;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantStateDetails {

    private State state;
    private Long userNo;
    private String nickName;
    private String imageUrl;

    public ParticipantStateDetails(State state, Long userNo, String nickName, String imageUrl){
        this.state = state;
        this.userNo = userNo;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
    }
}
