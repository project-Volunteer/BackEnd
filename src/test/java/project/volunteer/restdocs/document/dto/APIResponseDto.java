package project.volunteer.restdocs.document.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class APIResponseDto<T> {
    private T data;

    private APIResponseDto(T data){
        this.data = data;
    }

    public static <T> APIResponseDto<T> of(T data){
        return new APIResponseDto<>(data);
    }
}
