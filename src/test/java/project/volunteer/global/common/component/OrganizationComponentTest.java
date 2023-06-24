package project.volunteer.global.common.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerMapping;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class OrganizationComponentTest {

    @Autowired OrganizationComponent organizationComponent;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;

    User writer;
    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 등록
        writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);

        //정기 모집글 등록
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @DisplayName("봉사 모집글 방장 검증에 성공하다.")
    @Transactional
    public void isRecruitmentOwner(){
        //given
        final String recruitmentNo = String.valueOf(saveRecruitment.getRecruitmentNo());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("recruitmentNo", recruitmentNo));

        //when & then
        organizationComponent.validRecruitmentOwner(request, writer.getUserNo());
    }

    @Test
    @DisplayName("봉사 모집글 방장 검증에 실패하다.")
    @Transactional
    public void isNotRecruitmentOwner(){
        //given
        User testUser = 사용자_추가("test");
        final String recruitmentNo = String.valueOf(saveRecruitment.getRecruitmentNo());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("recruitmentNo", recruitmentNo));

        //when & then
        assertThatThrownBy(() -> organizationComponent.validRecruitmentOwner(request, testUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

    @Test
    @DisplayName("봉사 모집글 팀원 검증에 성공하다.")
    @Transactional
    public void isRecruitmentTeam(){
        //given
        User testUser = 사용자_추가("test");
        팀_추가(testUser, ParticipantState.JOIN_APPROVAL); //팀원 추가
        final String recruitmentNo = String.valueOf(saveRecruitment.getRecruitmentNo());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("recruitmentNo", recruitmentNo));

        //when & then
        organizationComponent.validRecruitmentTeam(request, testUser.getUserNo());
    }

    @Test
    @DisplayName("봉사 모집글 팀원 검증에 실패하다.")
    @Transactional
    public void isNotRecruitmentTeam(){
        //given
        User testUser = 사용자_추가("test");
        final String recruitmentNo = String.valueOf(saveRecruitment.getRecruitmentNo());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.singletonMap("recruitmentNo", recruitmentNo));

        //when & then
        assertThatThrownBy(() -> organizationComponent.validRecruitmentTeam(request, testUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT_TEAM");
    }

    private User 사용자_추가(String username){
        User newUser = User.createUser(username, username, username, username, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", username, null);
        return userRepository.save(newUser);
    }
    private Participant 팀_추가( User user, ParticipantState state){
        Participant participant = Participant.createParticipant(saveRecruitment, user, state);
        return participantRepository.save(participant);
    }
}