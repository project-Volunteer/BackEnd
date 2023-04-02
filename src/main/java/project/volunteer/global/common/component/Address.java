package project.volunteer.global.common.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    @Column(length = 5, nullable = false)
    private String sido;

    @Column(length = 10, nullable = false)
    private String sigungu;

    @Column(length = 50, nullable = false)
    private String details;

    @Column(nullable = false)
    private Float latitude;

    @Column(nullable = false)
    private Float longitude;

}
