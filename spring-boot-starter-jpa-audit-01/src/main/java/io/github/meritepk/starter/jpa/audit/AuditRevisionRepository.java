package io.github.meritepk.starter.jpa.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRevisionRepository
        extends JpaRepository<AuditRevision, Long>, JpaSpecificationExecutor<AuditRevision> {
}
