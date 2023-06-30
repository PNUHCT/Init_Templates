package neoguri.springTemplate.auditing;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;


/**
 * 상속(extend)를 통해 각 객체에 공통적으로 적용해줄 필드를 정의해서 사용하는 추상 클래스
 * 예시)
 * public class User extends Auditable { ... } 방식을 상속
 *
 * 생성자를 이용해 객체 생성 시 필드에 대한 값을 할당해주어 넣어주며, 할당 안할시 기본값으로 들어감(null, 0 등)
 * 예시 )
 * public User() {
 *     ...
 *     user.createdAt(LocalDateTime.now());
 *     ...
 * }
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_AT")
    private LocalDateTime modifiedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
}
