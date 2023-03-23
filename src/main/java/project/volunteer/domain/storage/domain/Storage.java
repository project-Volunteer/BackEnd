package project.volunteer.domain.storage.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_storage")
public class Storage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storageno")
    private Long storageNo;

    @Column(name = "image_path", length = 500, nullable = false)
    private String imagePath; //이미지 URL

    @Column(name = "fake_image_name", length = 400, nullable = false)
    private String fakeImageName; //저장 이미지명

    @Column(name = "real_image_name", length = 400, nullable = false)
    private String realImageName; //실제 이미지명

    @Column(name = "ext_name", length = 10, nullable = false)
    private String extName; //확장자명

}
