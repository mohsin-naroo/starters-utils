package io.github.meritepk.starter.jpa.audit;

import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
public class AuditRevisionListener {

    private final ApplicationContext applicationContext;
    private AuditRevisionService auditRevisionService;
    private AuditorAware<?> auditorAware;

    public AuditRevisionListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected String getCurrentAuditor() {
        Object auditor = auditorAware.getCurrentAuditor().orElse(null);
        return auditor == null ? "n/a" : auditor.toString();
    }

    protected void audit(Object target, String actionType) {
        if (auditRevisionService == null && auditorAware == null) {
            auditRevisionService = applicationContext.getBean(AuditRevisionService.class);
            try {
                auditorAware = applicationContext.getBean(AuditorAware.class);
            } catch (Exception e) {
                auditorAware = Optional::empty;
            }
        }
        auditRevisionService.audit(target, actionType, getCurrentAuditor());
    }

    @PostPersist
    public void onPostPersist(Object target) {
        audit(target, "I");
    }

    @PostUpdate
    public void onPostUpdate(Object target) {
        audit(target, "U");
    }

    @PostRemove
    public void onPostRemove(Object target) {
        audit(target, "D");
    }
}
