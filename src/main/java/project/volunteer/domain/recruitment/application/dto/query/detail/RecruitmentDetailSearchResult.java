package project.volunteer.domain.recruitment.application.dto.query.detail;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import project.volunteer.domain.participation.dao.dto.RecruitmentParticipantDetail;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.global.common.component.ParticipantState;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentDetailSearchResult {
    private Long no;
    private String volunteeringCategory;
    private String organizationName;
    private Boolean isIssued;
    private String volunteeringType;
    private String volunteerType;
    private Integer maxVolunteerNum;
    private String startDate;
    private String endDate;
    private String hourFormat;
    private String startTime;
    private Integer progressTime;
    private String title;
    private String content;

    private AddressDetail address;
    private PictureDetail picture;
    private WriterDetail author;

    private RepeatPeriodDetail repeatPeriod;

    private List<ParticipantDetail> approvalParticipant;
    private List<ParticipantDetail> requiredParticipant;

    public static RecruitmentDetailSearchResult of(RecruitmentAndUserDetail recruitmentAndUserDetail,
                                                   RepeatPeriodDetail repeatPeriodDetail,
                                                   List<RecruitmentParticipantDetail> participantDetails) {

        AddressDetail addressDetail = new AddressDetail(recruitmentAndUserDetail.getAddress(),
                recruitmentAndUserDetail.getCoordinate());
        PictureDetail pictureDetail = PictureDetail.of(recruitmentAndUserDetail.getRecruitmentImagePath());
        WriterDetail writerDetail = new WriterDetail(recruitmentAndUserDetail.getUserNickName(),
                recruitmentAndUserDetail.getUserImagePath());

        List<ParticipantDetail> approvalParticipant = participantDetails.stream()
                .filter(participantDetail -> participantDetail.getState().equals(ParticipantState.JOIN_APPROVAL))
                .map(participantDetail -> new ParticipantDetail(participantDetail.getUserNo(), participantDetail.getNickName(), participantDetail.getImageUrl()))
                .collect(Collectors.toList());
        List<ParticipantDetail> requiredParticipant = participantDetails.stream()
                .filter(participantDetail -> participantDetail.getState().equals(ParticipantState.JOIN_REQUEST))
                .map(participantDetail -> new ParticipantDetail(participantDetail.getUserNo(), participantDetail.getNickName(), participantDetail.getImageUrl()))
                .collect(Collectors.toList());

        return new RecruitmentDetailSearchResult(
                recruitmentAndUserDetail.getNo(),
                recruitmentAndUserDetail.getVolunteeringCategory().getId(),
                recruitmentAndUserDetail.getOrganizationName(),
                recruitmentAndUserDetail.getIsIssued(),
                recruitmentAndUserDetail.getVolunteeringType().getId(),
                recruitmentAndUserDetail.getVolunteerType().getId(),
                recruitmentAndUserDetail.getMaxParticipationNum(),
                recruitmentAndUserDetail.getTimetable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                recruitmentAndUserDetail.getTimetable().getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                recruitmentAndUserDetail.getTimetable().getHourFormat().getId(),
                recruitmentAndUserDetail.getTimetable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                recruitmentAndUserDetail.getTimetable().getProgressTime(),
                recruitmentAndUserDetail.getTitle(),
                recruitmentAndUserDetail.getContent(),
                addressDetail,
                pictureDetail,
                writerDetail,
                repeatPeriodDetail,
                approvalParticipant,
                requiredParticipant
        );
    }

}
