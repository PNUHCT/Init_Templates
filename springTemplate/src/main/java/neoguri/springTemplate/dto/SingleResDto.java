package neoguri.springTemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 기본 Optional Response용 구현 클래스
 * 응답으로 출력시, 아래 형식과 같이 data에 대한  밸류값으로 래핑되어 응답
 * data : {
 *     ...
 * }
 */
@Data
@AllArgsConstructor
public class SingleResDto<T> {
    T data;
}
