package project.volunteer.domain.reply.dao.queryDto;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;
import project.volunteer.domain.reply.dao.queryDto.dto.QCommentMapperDto;
import project.volunteer.domain.reply.domain.QReply;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;

import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.image.domain.QStorage.storage;
import static project.volunteer.domain.user.domain.QUser.user;
@Repository
@RequiredArgsConstructor
public class ReplyQueryDtoRepositoryImpl implements ReplyQueryDtoRepository{
    private final JPAQueryFactory jpaQueryFactory;
    private final QReply children = new QReply("children");
    private final QReply parent = new QReply("parent");
    @Override
    public List<CommentMapperDto> getCommentMapperDtos(RealWorkCode code, Long no) {
        return jpaQueryFactory
                .select(
                        new QCommentMapperDto(children.replyNo, parent.replyNo,storage.imagePath.coalesce(user.picture).as("profile"),
                                user.nickName, children.content, children.createdDate))
                .from(children)
                .join(children.writer, user)
                .leftJoin(image).on(
                        image.realWorkCode.eq(RealWorkCode.USER),
                        image.no.eq(user.userNo),
                        image.isDeleted.eq(IsDeleted.N))
                .leftJoin(image.storage, storage)
                .leftJoin(children.parent, parent)
                .where(
                        eqRealWorkCode(code),
                        eqNo(no)
                )
                .orderBy(parent.replyNo.asc().nullsFirst(), children.replyNo.asc())
                .fetch();
    }

    private BooleanExpression eqRealWorkCode(RealWorkCode code){
        return (code!=null)?children.realWorkCode.eq(code):null;
    }
    private BooleanExpression eqNo(Long no){
        return (no!=null)?children.no.eq(no):null;
    }
}

