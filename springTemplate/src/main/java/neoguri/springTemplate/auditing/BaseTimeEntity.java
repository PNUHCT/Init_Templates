package neoguri.springTemplate.auditing;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


/**
 * 주로 사용하는 생성일자, 수정일자만 받는 상속클래스
 *
 * 사용방법 : 적용하실 ResDto 클래스에 extends로 상속 후,
 *             private LocalDatetime createdAt
 *             private LocalDatetime modifiedAt
 *             으로 필드를 구현해 주시면 됩니다.
 *
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)  // JPA Entity에 이벤트가 발생할 때 콜백을 처리하고 코드를 실행하는 방법
public class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false) // 애너테이션으로 자동 적용 + 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate  // 애너테이션으로 자동 적용
    @Column(name="last_modified_at")
    private LocalDateTime modifiedAt;
}
