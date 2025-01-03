package io.github.meritepk.starter.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RsOrderItemRepository extends JpaRepository<RsOrderItem, Long> {
}
