package project.volunteer.domain.recruitment.repository;

import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;
import static project.volunteer.domain.user.domain.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.image.domain.QImage;
import project.volunteer.domain.image.domain.QStorage;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Repository
@RequiredArgsConstructor
public class RecruitmentQueryDSLRepositoryImpl implements RecruitmentQueryDSLRepository {
    private final static QImage recruitmentImage = new QImage("recruitmentImage");
    private final static QImage userImage = new QImage("userImage");
    private final static QStorage recruitmentImageStorage = new QStorage("recruitmentImageStorage");
    private final static QStorage userImageStorage = new QStorage("userImageStorage");

    private final JPAQueryFactory queryFactory;

    @Override
    public RecruitmentAndUserDetail findRecruitmentAndUserDetailBy(Long recruitmentNo) {
        RecruitmentAndUserDetail result = queryFactory.select(
                        Projections.constructor(RecruitmentAndUserDetail.class, recruitment.recruitmentNo,
                                recruitment.volunteeringCategory, recruitment.organizationName, recruitment.isIssued,
                                recruitment.volunteeringType, recruitment.volunteerType, recruitment.maxParticipationNum,
                                recruitment.timetable, recruitment.title, recruitment.content, recruitment.address,
                                recruitment.coordinate,
                                recruitmentImageStorage.imagePath, user.nickName, user.picture, userImageStorage.imagePath))
                .from(recruitment)
                .innerJoin(recruitment.writer, user)
                .leftJoin(recruitmentImage)
                .on(recruitment.recruitmentNo.eq(recruitmentImage.no),
                        recruitmentImage.realWorkCode.eq(RealWorkCode.RECRUITMENT),
                        recruitmentImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(recruitmentImage.storage, recruitmentImageStorage)
                .leftJoin(userImage)
                .on(user.userNo.eq(userImage.no),
                        userImage.realWorkCode.eq(RealWorkCode.USER),
                        userImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(userImage.storage, userImageStorage)
                .where(recruitment.recruitmentNo.eq(recruitmentNo),
                        recruitment.isDeleted.eq(IsDeleted.N))
                .fetchFirst();

        if (Objects.isNull(result)) {
            throw new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                    String.format("RecruitmentNo = [%d]", recruitmentNo));
        }

        return result;
    }


}
