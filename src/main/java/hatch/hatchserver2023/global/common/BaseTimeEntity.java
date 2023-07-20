package hatch.hatchserver2023.global.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Getter //mapper 사용을 위해 필요
@MappedSuperclass // 이 클래스를 상속한 엔티티들이 이 클래스의 필드들도 자신의 컬럼으로 인식하도록 함
@EntityListeners(AuditingEntityListener.class) //Auditing 기능
public abstract class BaseTimeEntity {

    //TODO : NOT NULL 설정?
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


    /**
     * 테스트 시 필요한 메서드, 다른 기능에는 사용하지 말 것
     * @param createdAt
     */
    public void updateForTestCode(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
