package project.volunteer.domain.recruitment.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantDto {

    private String nickName;
    private String imageUrl;
    private Boolean isApproved;

}
