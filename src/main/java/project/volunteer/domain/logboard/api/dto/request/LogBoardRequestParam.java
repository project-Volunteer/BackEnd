package project.volunteer.domain.logboard.api.dto.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogBoardRequestParam {
    private List<MultipartFile> uploadImage = new ArrayList<>();
    
    @NotNull
    private String content;

    @NotNull
    private Long scheduleNo;

    @NotNull
    private Boolean isPublished; 
    
    
}
