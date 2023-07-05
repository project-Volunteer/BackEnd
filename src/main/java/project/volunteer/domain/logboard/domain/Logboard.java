package project.volunteer.domain.logboard.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.IsDeleted;

@Getter
@Entity
@Table(name = "vlt_logboard")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Logboard  extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logboardno")
    private Long logboardNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleno")
    private Schedule schedule;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;
    
    @Column(name = "create_by", nullable = false)
    private Long createUserNo;

    @Column(name = "modified_by")
    private Long modifiedUserNo;

    
    public static Logboard createLogBoard(String content, Boolean isPublished, Long userno){
    	Logboard logboard = new Logboard();
    	logboard.content = content;
    	logboard.isPublished = isPublished;
    	logboard.likeCount = 0;
    	logboard.viewCount = 0;
    	logboard.isDeleted = IsDeleted.N;
    	logboard.createUserNo = userno;
        return logboard;
    }

	public void editLogBoard( String content, Boolean isPublished, Long userno) {
    	this.content = content;
    	this.isPublished = isPublished;
    	this.isDeleted = IsDeleted.N;
    	this.modifiedUserNo = userno;
	}

	public void delete() {
    	this.isDeleted = IsDeleted.Y;
	}
	
    public void setWriter(User user){
        this.writer = user;
    }

    public void setSchedule(Schedule schedule){
        this.schedule = schedule;
    }


}
