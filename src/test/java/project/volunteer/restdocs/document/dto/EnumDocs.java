package project.volunteer.restdocs.document.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class EnumDocs {
    // 문서화하고 싶은 모든 enum 값을 명시
    Map<String,String> hourFormat;
    Map<String,String> clientState;
}
