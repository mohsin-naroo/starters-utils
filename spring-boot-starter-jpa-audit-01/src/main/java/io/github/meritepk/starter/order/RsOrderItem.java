package io.github.meritepk.starter.order;

import io.github.meritepk.starter.jpa.audit.AuditRevisionListener;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EntityListeners(AuditRevisionListener.class)
@Entity
public class RsOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quantity;

    @Embedded
    private RsPrice price;

    @ManyToOne
    private RsOrder order;

    @OneToOne
    private RsItem item;
}
