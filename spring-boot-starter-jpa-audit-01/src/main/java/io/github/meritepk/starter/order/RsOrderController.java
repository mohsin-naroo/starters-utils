package io.github.meritepk.starter.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class RsOrderController {

    private final RsItemRepository itemRepository;
    private final RsOrderRepository orderRepository;

    public RsOrderController(RsItemRepository itemRepository, RsOrderRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @GetMapping
    public ResponseEntity<Map<String, Object>> orders() {
        List<Map<String, Object>> orders = new ArrayList<>();
        orderRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")))).getContent().forEach(order -> {
            List<Map<String, Object>> items = new ArrayList<>();
            order.getItems().forEach(item -> {
                items.add(Map.of("id", item.getId(), "itemId", item.getItem().getId(), "name", item.getItem().getName(),
                        "price", item.getItem().getPrice().getPrice(), "quantity", item.getQuantity()));
            });
            orders.add(Map.of("id", order.getId(), "createdAt", order.getCreatedAt(), "items", items));
        });
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    @Transactional
    @GetMapping("/create")
    public ResponseEntity<Map<String, Object>> test1() {
        return create().orders();
    }

    @Transactional
    @GetMapping("/update")
    public ResponseEntity<Map<String, Object>> test2() {
        return update().orders();
    }

    @Transactional
    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> test3() {
        return delete().orders();
    }

    private RsOrderController create() {
        // create item
        RsItem item = new RsItem();
        item.setName("test# " + System.currentTimeMillis());
        item.setCreatedAt(LocalDateTime.now());
        item.setPrice(new RsPrice());
        item.getPrice().setPrice(2.0);
        itemRepository.save(item);
        // create order
        RsOrder order = new RsOrder();
        order.setCreatedAt(LocalDateTime.now());
        // create order item
        itemRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Order.desc("id")))).getContent().forEach(itm -> {
            int index = order.getItems().size();
            order.getItems().add(new RsOrderItem());
            order.getItems().get(index).setOrder(order);
            order.getItems().get(index).setQuantity((long) order.getItems().size());
            order.getItems().get(index).setItem(itm);
        });
        orderRepository.save(order);

        return this;
    }

    private RsOrderController update() {
        orderRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id")))).getContent().forEach(order -> {
            order.getItems().forEach(item -> {
                item.setQuantity(item.getQuantity() + 1);
                item.getItem().getPrice().setPrice(item.getItem().getPrice().getPrice() + 1);
            });
            orderRepository.save(order);
        });
        return this;
    }

    private RsOrderController delete() {
        orderRepository.findAll(PageRequest.of(0, 1, Sort.by(Sort.Order.asc("id")))).getContent().forEach(order -> {
            orderRepository.delete(order);
        });
        return this;
    }
}
