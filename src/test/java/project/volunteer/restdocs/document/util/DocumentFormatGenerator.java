package project.volunteer.restdocs.document.util;

import org.springframework.restdocs.snippet.Attributes;

import static org.springframework.restdocs.snippet.Attributes.key;

public interface DocumentFormatGenerator {

    //java 8 정적 메서드 활용(default 메서드도 가능)
    static Attributes.Attribute getDateFormat(){
        return key("format").value("MM-dd-yyyy");
    }
    static Attributes.Attribute getTimeFormat(){
        return key("format").value("HH-mm");
    }
}
