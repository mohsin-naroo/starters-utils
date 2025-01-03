package io.github.meritepk.starter.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.github.meritepk.starter.jpa.audit.AuditRevisionListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EntityListeners(AuditRevisionListener.class)
@Entity
public class RsOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<RsOrderItem> items = new ArrayList<>();
}
