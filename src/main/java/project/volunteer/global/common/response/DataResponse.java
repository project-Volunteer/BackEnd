package project.volunteer.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataResponse<T> extends BaseResponse {
    private T data;
    public DataResponse(T data, String message){
        super(message);
        this.data = data;
    }

}
