package TemplateProject.Template.global.common.dto;

import lombok.*;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class PageInfoDto<T> {

    private List<T> content;  // 감싸는 리스트 이름

    private int totalPages;

    private long totalElements;

    private boolean first;  // 첫 페이지인지

    private boolean last;   // 마지막 페이지인지

    private boolean sorted; // 정렬 조건이 있는지

    private int size;

    private int pageNumber;

    private int numberOfElements;

}
