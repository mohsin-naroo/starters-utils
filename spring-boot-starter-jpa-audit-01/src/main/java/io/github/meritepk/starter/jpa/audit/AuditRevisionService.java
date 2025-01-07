package io.github.meritepk.starter.jpa.audit;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service
public class AuditRevisionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AuditRevisionRepository repository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    private final Map<Class<?>, AttributeMeta[]> attributeMetaCache;

    public AuditRevisionService(AuditRevisionRepository repository, ObjectMapper objectMapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
        this.attributeMetaCache = new HashMap<>();
    }

    public Page<AuditRevision> find(AuditRevision audit, LocalDateTime fromDateTime, LocalDateTime toDateTime,
            Pageable pageable) {
        return repository.findAll(findSpecs(audit, fromDateTime, toDateTime), pageable);
    }

    protected Specification<AuditRevision> findSpecs(AuditRevision audit, LocalDateTime fromDateTime,
            LocalDateTime toDateTime) {
        return (root, query, criteria) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (audit.getId() != null) {
                predicates.add(criteria.equal(root.get("id"), audit.getId()));
            }
            if (audit.getActionType() != null) {
                predicates.add(criteria.equal(root.get("actionType"), audit.getActionType()));
            }
            if (audit.getActionBy() != null) {
                predicates.add(criteria.equal(root.get("actionBy"), audit.getActionBy()));
            }
            if (fromDateTime != null) {
                predicates.add(criteria.greaterThan(root.get("actionAt"), fromDateTime));
            }
            if (toDateTime != null) {
                predicates.add(criteria.lessThan(root.get("actionAt"), toDateTime));
            }
            if (audit.getEntityType() != null) {
                predicates.add(criteria.equal(root.get("entityType"), audit.getEntityType()));
            }
            if (audit.getEntityId() != null) {
                predicates.add(criteria.equal(root.get("entityId"), audit.getEntityId()));
            }
            if (audit.getEntityIdText() != null) {
                predicates.add(criteria.equal(root.get("entityIdText"), audit.getEntityIdText()));
            }
            return criteria.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    protected String formatJson(Object target) {
        try {
            return objectMapper.writeValueAsString(target);
        } catch (Exception e) {
            logger.warn("formatJson error: {}", e.toString());
            return String.valueOf(target);
        }
    }

    protected AttributeMeta[] findAttributeMetas(Class<?> clas) {
        if (!attributeMetaCache.containsKey(clas)) {
            initAttributeMetas(clas);
        }
        return attributeMetaCache.get(clas);
    }

    protected synchronized void initAttributeMetas(Class<?> clas) {
        if (!attributeMetaCache.containsKey(clas)) {
            Map<String, AttributeMeta> attributes = new LinkedHashMap<>();
            EntityType<?> entityType = findEntityType(clas);
            Map<String, Method> methods = findReadMethods(clas);
            initAttributeMeta(entityType.getId(entityType.getIdType().getJavaType()), methods, attributes);
            entityType.getAttributes().forEach(attribute -> {
                if (!attributes.containsKey(attribute.getName()) && methods.containsKey(attribute.getName())) {
                    if (Attribute.PersistentAttributeType.BASIC.equals(attribute.getPersistentAttributeType())
                            || Attribute.PersistentAttributeType.EMBEDDED
                                    .equals(attribute.getPersistentAttributeType())) {
                        initAttributeMeta(attribute, methods, attributes);
                    } else if (Attribute.PersistentAttributeType.MANY_TO_ONE
                            .equals(attribute.getPersistentAttributeType())
                            || Attribute.PersistentAttributeType.ONE_TO_ONE
                                    .equals(attribute.getPersistentAttributeType())) {
                        AttributeMeta meta = initAttributeMeta(attribute, methods, attributes);
                        meta.setReference(findAttributeMetas(attribute.getJavaType())[0]);
                    }
                }
            });
            attributeMetaCache.put(clas, attributes.values().toArray(new AttributeMeta[attributes.size()]));
        }
    }

    protected AttributeMeta initAttributeMeta(Attribute<?, ?> attribute, Map<String, Method> methods,
            Map<String, AttributeMeta> attributes) {
        AttributeMeta meta = new AttributeMeta(attribute.getName(), methods.get(attribute.getName()), null);
        attributes.put(meta.getName(), meta);
        return meta;
    }

    protected EntityType<?> findEntityType(Class<?> clas) {
        return entityManager.getMetamodel().entity(clas);
    }

    protected Map<String, Method> findReadMethods(Class<?> clas) {
        Map<String, Method> methods = new HashMap<>();
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(clas).getPropertyDescriptors()) {
                Method method = descriptor.getReadMethod();
                if (method != null) {
                    methods.put(descriptor.getName(), method);
                }
            }
        } catch (Exception e) {
            logger.warn("Error init property descriptor for:{}, error: {}", clas, e.getMessage(), e);
        }
        return methods;
    }

    protected Object invokeMethod(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception e) {
            logger.warn("Error setting attribute: {}, to object: {}, error: {}", method, object, e.getMessage(), e);
        }
        return null;
    }

    public void audit(Object target, String actionType, String actionBy) {
        AttributeMeta[] attributes = findAttributeMetas(target.getClass());
        Map<String, Object> values = new HashMap<>();
        for (AttributeMeta attribute : attributes) {
            Object value = invokeMethod(target, attribute.getMethod());
            if (value != null) {
                if (attribute.getReference() != null) {
                    values.put(attribute.getName(), Map.of(attribute.getReference().getName(),
                            invokeMethod(value, attribute.getReference().getMethod())));
                } else {
                    values.put(attribute.getName(), value);
                }
            }
        }
        String entityIdText = values.get(attributes[0].getName()).toString();
        Long entityId;
        try {
            entityId = Long.parseLong(entityIdText);
        } catch (Exception e) {
            entityId = null;
        }
        audit(actionType, actionBy, LocalDateTime.now(), target.getClass().getName(), formatJson(values), entityId,
                entityIdText);
    }

    public AuditRevision audit(String actionType, String actionBy, LocalDateTime actionAt,
            String entityType, String entityState, Long entityId, String entityIdText) {
        AuditRevision audit = new AuditRevision();
        audit.setActionType(actionType);
        audit.setActionBy(actionBy);
        audit.setActionAt(actionAt);
        audit.setEntityType(entityType);
        audit.setEntityState(entityState);
        audit.setEntityId(entityId);
        audit.setEntityIdText(entityIdText);
        return audit(audit);
    }

    public AuditRevision audit(AuditRevision audit) {
        return repository.save(audit);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AttributeMeta {
        private String name;
        private Method method;
        private AttributeMeta reference;
    }
}
