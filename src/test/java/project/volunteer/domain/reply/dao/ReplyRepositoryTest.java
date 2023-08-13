package project.volunteer.domain.reply.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.reply.dao.queryDto.ReplyQueryDtoRepository;
import project.volunteer.domain.reply.dao.queryDto.dto.CommentMapperDto;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
class ReplyRepositoryTest {
    @Autowired UserRepository userRepository;
    @Autowired ReplyRepository replyRepository;
    @Autowired ReplyQueryDtoRepository replyQueryDtoRepository;
    @Autowired StorageRepository storageRepository;
    @Autowired ImageRepository imageRepository;

    @Test
    @DisplayName("댓글과 대댓글 리스트 조회 시 댓글(부모) 리스트를 가장 앞에 정렬시킨다.")
    void findCommentReplyList_orderBy_ParentNullFirsts(){
        //given
        User user1 = saveUser("user1");
        User user2 = saveUser("user2");
        User user3 = saveUser("user3");
        User user4 = saveUser("user4");
        User user5 = saveUser("user5");

        Reply parent1 = saveComment(RealWorkCode.NOTICE, 1L, "parent1", user1);
        Reply children1_1 = saveCommentReply(parent1, RealWorkCode.NOTICE, 1L, "children1-1", user2);
        Reply children1_2 = saveCommentReply(parent1, RealWorkCode.NOTICE, 1L, "children1-2", user3);
        Reply parent2 = saveComment(RealWorkCode.NOTICE, 1L, "parent2", user4);
        Reply etcParent = saveComment(RealWorkCode.LOG, 1L, "parent1", user5);

        //when
        List<Reply> commentReplyList = replyRepository.findCommentReplyList(RealWorkCode.NOTICE, 1L);

        //then
        for(Reply reply : commentReplyList){
            System.out.println(reply);
        }
    }

    @Test
    @DisplayName("View 전용 댓글과 대댓글 DTO 리스트 조회에 성공하다.")
    void findCommentReplyDtos(){
        //given
        User user1 = saveUser("user1");
        User user2 = saveUser("user2");
        User user3 = saveUser("user3");
        User user4 = saveUser("user4");
        User user5 = saveUser("user5");
        saveUploadImage(user1.getUserNo(), RealWorkCode.USER);
        saveUploadImage(user2.getUserNo(), RealWorkCode.USER);
        saveUploadImage(user3.getUserNo(), RealWorkCode.USER);

        Reply parent1 = saveComment(RealWorkCode.NOTICE, 1L, "parent1", user1);
        Reply children1_1 = saveCommentReply(parent1, RealWorkCode.NOTICE, 1L, "children1-1", user2);
        Reply children1_2 = saveCommentReply(parent1, RealWorkCode.NOTICE, 1L, "children1-2", user3);
        Reply parent2 = saveComment(RealWorkCode.NOTICE, 1L, "parent2", user4);
        Reply etcParent = saveComment(RealWorkCode.LOG, 1L, "parent1", user5);

        //then
        List<CommentMapperDto> commentReplyViewDtos = replyQueryDtoRepository.getCommentMapperDtos(RealWorkCode.NOTICE, 1L);

        //then
        for(CommentMapperDto dto : commentReplyViewDtos){
            System.out.println(dto);
        }
    }

    private User saveUser(String value){
        User user = User.createUser(value, "password", value, "email", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", value, null);
        return userRepository.save(user);
    }
    private void saveUploadImage(Long no, RealWorkCode code){
        Storage storage = storageRepository.save(Storage.builder()
                .imagePath("imagePath" + no)
                .fakeImageName("fakeImageName" + no)
                .realImageName("realImageName" + no)
                .extName(".jpg")
                .build());

        Image image = Image.builder()
                .realWorkCode(code)
                .no(no)
                .build();
        image.setStorage(storage);
        imageRepository.save(image);
    }

    private Reply saveComment(RealWorkCode code, Long no, String content, User writer){
        Reply comment = Reply.createComment(code, no, content);
        comment.setWriter(writer);
        return replyRepository.save(comment);
    }
    private Reply saveCommentReply(Reply parent, RealWorkCode code, Long no, String content, User writer){
        Reply reply = Reply.createCommentReply(parent, code, no, content);
        reply.setWriter(writer);
        return replyRepository.save(reply);
    }
}