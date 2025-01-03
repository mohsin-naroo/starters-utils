package io.github.meritepk.starter.order;

import java.time.LocalDateTime;

import io.github.meritepk.starter.jpa.audit.AuditRevisionListener;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EntityListeners(AuditRevisionListener.class)
@Entity
public class RsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime createdAt;

    @Embedded
    private RsPrice price;
}
