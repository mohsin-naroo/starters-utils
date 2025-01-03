package io.github.meritepk.starter.jpa.audit;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audits")
public class AuditRevisionController {

    private final AuditRevisionService service;

    public AuditRevisionController(AuditRevisionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PagedModel<AuditRevision>> find(
            AuditRevision audit, LocalDateTime fromDateTime, LocalDateTime toDateTime,
            @PageableDefault(size = 100) @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(service.find(audit, fromDateTime, toDateTime, pageable)));
    }

    @PostMapping
    public ResponseEntity<AuditRevision> audit(AuditRevision audit) {
        return ResponseEntity.ok(service.audit(audit));
    }
}