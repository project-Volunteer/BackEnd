package project.volunteer.global.common.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OrganizationComponent {

    private final String RECRUITMENT_NO = "recruitmentNo";

    private final RecruitmentRepository recruitmentRepository;
    private final ParticipantRepository participantRepository;

    //봉사 모집글 방장 검증 메서드
    public void validRecruitmentOwner(HttpServletRequest request, Long loginUserNo){
        Recruitment findRecruitment = getRecruitment(request);
        //DDD 설계 살려서
        if(!findRecruitment.isRecruitmentOwner(loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", findRecruitment.getRecruitmentNo(), loginUserNo));
        }
    }

    //봉사 모집글 팀원 검증 메서드(방장 or 팀원)
    public void validRecruitmentTeam(HttpServletRequest request, Long loginUserNo){
        Recruitment findRecruitment = getRecruitment(request);
        //단방향 연관관계이므로 별도의 메서드로 검증
        isRecruitmentTeam(findRecruitment, loginUserNo);
    }

    //Request 정보에서 봉사 모집글 검색 메서드
    private Recruitment getRecruitment(HttpServletRequest request){

        //PathVariable 정보 Map으로 추출
        final Map<String, String> path = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
         Long recruitmentNo = Long.valueOf(path.get(RECRUITMENT_NO));

        //기본 Spring OSIV 모드 트랜잭션 읽기 모드 사용(수정 변경 불가, 단순 읽기만, 영속 상태)
        //삭제만 되지 않은 봉사 모집글 검색(임시 봉사 모집글을 위해서)
        return recruitmentRepository.findValidRecruitment(recruitmentNo)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
    }

    private void isRecruitmentTeam(Recruitment recruitment, Long loginUserNo){
        //기본 Spring OSIV 모드 트랜잭션 읽기 모드 사용(수정 변경 불가, 단순 읽기만, 영속 상태)
        //팀원 or 방장
        if(!participantRepository.existRecruitmentTeamMember(recruitment.getRecruitmentNo(), loginUserNo) && !recruitment.isRecruitmentOwner(loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT_TEAM,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitment.getRecruitmentNo(), loginUserNo));
        }
    }
}
