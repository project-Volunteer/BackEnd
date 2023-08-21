package project.volunteer.domain.reply.domain;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.image.converter.RealWorkCodeConverter;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.RealWorkCode;

@Getter
@Entity
@Table(name = "vlt_reply")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseTimeEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "replyno")
	private Long replyNo;

	//TODO: cascade 옵션이 없어도 될까요? 부모 댓글이 삭제될때 자식 댓글 리스트들이 자동으로 삭제되어야 할거 같아요.
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentno")
    private Reply parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userno")
	private User writer;
	
    @Convert(converter = RealWorkCodeConverter.class)
    @Column(name = "realwork_code", length = 2, nullable = false)
    private RealWorkCode realWorkCode;

    @Column(nullable = false)
    private Long no;

	@Column(columnDefinition = "LONGTEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "is_deleted", length = 1, nullable = false)
	private IsDeleted isDeleted;
    
    public static Reply createComment(RealWorkCode code, Long no, String content){
    	Reply createReply = new Reply();
    	createReply.realWorkCode = code;
    	createReply.no = no;
    	createReply.content = content;
		createReply.isDeleted = IsDeleted.N;
        return createReply;
    }

    public static Reply createCommentReply(Reply parent, RealWorkCode code, Long no, String content){
    	Reply createReply = new Reply();
    	createReply.parent = parent;
    	createReply.realWorkCode = code;
    	createReply.no = no;
    	createReply.content = content;
		createReply.isDeleted = IsDeleted.N;
        return createReply;
    }

	@Builder
	public Reply(Reply parent, User user, RealWorkCode code, Long no, String content){
		this.parent = parent;
		this.writer = user;
		this.realWorkCode = code;
		this.no = no;
		this.content = content;
	}

	public void editReply(String content) {
    	this.content = content;
	}

	public void delete(){
		this.isDeleted = IsDeleted.Y;
	}
    
    public void setWriter(User user){
        this.writer = user;
    }

	@Override
	public String toString() {
		return "replyNo="+ replyNo + " code=" + realWorkCode + " no=" + no + " content=" + content;
	}
}
