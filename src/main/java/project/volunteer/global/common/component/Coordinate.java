package project.volunteer.global.common.component;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coordinate {

    @Column(nullable = false)
    private Float latitude;

    @Column(nullable = false)
    private Float longitude;

    @Builder
    public Coordinate(Float latitude, Float longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinate createCoordinate(Float latitude, Float longitude){
        Coordinate coordinate = new Coordinate();
        coordinate.latitude = latitude;
        coordinate.longitude = longitude;
        return coordinate;
    }
}
