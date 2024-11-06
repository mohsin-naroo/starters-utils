package io.github.meritepk.starter.news;

import java.time.LocalDateTime;

import io.github.meritepk.starter.jpa.TimeSequenceId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class News {

    @Id
    @TimeSequenceId
    private Long id;
    private String title;
    private String details;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime modifiedAt;
    private String modifiedBy;
}
