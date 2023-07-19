package neoguri.springTemplate.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * List<T> data : list 타입의 데이터로서, JSON으로 응답할 값을 받음. 객체 타입는 제네릭(T)으로 받음으로서, 모든 객체를 받을 수 있다.
 * PageInfo : 실제 Pagenation으로 받는 객체 정보 중, PageInfo 클래스에서 정의한 값만 응답으로 내보내기 위함
 */
@Getter
public class MultiResDto<T> {
    private List<T> data;
    private PageInfo pageInfo;

    public MultiResDto(List<T> data, Page page) {
        this.data = data;
        this.pageInfo = new PageInfo(
                page.getNumber() + 1,  // 0페이지부터 시작하기때문에 +1 페이지 해서 응답으로 내보내기
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}