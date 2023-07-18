package hatch.hatchserver2023.global.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Getter //mapper 사용을 위해 필요
@MappedSuperclass // 이 클래스를 상속한 엔티티들이 이 클래스의 필드들도 자신의 컬럼으로 인식하도록 함
@EntityListeners(AuditingEntityListener.class) //Auditing 기능
public abstract class BaseTimeEntity {

    private ZonedDateTime createdAt;

    private ZonedDateTime modifiedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        this.modifiedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
