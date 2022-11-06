package TemplateProject.Template.global.auditing;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter @Setter @MappedSuperclass
@EntityListeners(AuditingEntityListener.class)  // JPA Entity에 이벤트가 발생할 때 콜백을 처리하고 코드를 실행하는 방법
public class BaseTime {

    @CreatedDate @Column(updatable = false) // 애너테이션으로 자동 적용 + 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate  // 애너테이션으로 자동 적용
    private LocalDateTime modifiedAt;
}
