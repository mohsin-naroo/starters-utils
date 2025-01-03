package io.github.meritepk.starter.order;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class RsPrice {

    private Double price;
}
