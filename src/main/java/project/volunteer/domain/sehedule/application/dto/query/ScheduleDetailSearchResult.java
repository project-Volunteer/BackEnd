package project.volunteer.domain.sehedule.application.dto.query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;

@Getter
@NoArgsConstructor
public class ScheduleDetailSearchResult {
    private Long no;
    private AddressResult address;
    private LocalDate startDate;
    private LocalTime startTime;
    private HourFormat hourFormat;
    private int progressTime;
    private int volunteerNum;
    private String content;
    private int activeVolunteerNum;
    private String state;
    private Boolean hasData;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressResult {
        private String sido;
        private String sigungu;
        private String details;
        private String fullName;
    }

    public ScheduleDetailSearchResult(Long no, String sido, String sigungu, String detail, String fullName,
                                      LocalDate startDate, LocalTime startTime,
                                      HourFormat hourFormat, int progressTime, int volunteerNum, String content,
                                      int activeVolunteerNum) {
        this.no = no;
        this.address = new AddressResult(sido, sigungu, detail, fullName);
        this.startDate = startDate;
        this.startTime = startTime;
        this.hourFormat = hourFormat;
        this.progressTime = progressTime;
        this.volunteerNum = volunteerNum;
        this.content = content;
        this.activeVolunteerNum = activeVolunteerNum;
        this.hasData = true;
    }

    public static ScheduleDetailSearchResult createEmpty() {
        ScheduleDetailSearchResult result = new ScheduleDetailSearchResult();
        result.hasData = false;
        return result;
    }

    public Boolean hasData() {
        return this.hasData;
    }

    public void setResponseState(Optional<ParticipantState> state, LocalDate now) {
        this.state = getResponseState(state, now);
    }

    private String getResponseState(Optional<ParticipantState> state, LocalDate now) {
        //일정 참가 완료 미승인
        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)) {
            return StateResult.COMPLETE_UNAPPROVED.getId();
        }

        //일정 참가 완료 승인
        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)) {
            return StateResult.COMPLETE_APPROVED.getId();
        }

        //일정 참여 기간 만료
        if (isDone(now)) {
            return StateResult.DONE.getId();
        }

        //참여 중
        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATING)) {
            return StateResult.PARTICIPATING.getId();
        }

        //취소 요청
        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_CANCEL)) {
            return StateResult.CANCELLING.getId();
        }

        //인원 초과
        if (isFull()) {
            return StateResult.FULL.getId();
        }

        //신청 가능
        return StateResult.AVAILABLE.getId();
    }

    private boolean isFull() {
        return volunteerNum == activeVolunteerNum;
    }

    private boolean isDone(LocalDate now) {
        return startDate.isBefore(now);
    }

}
