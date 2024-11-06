package io.github.meritepk.starter.jpa;

import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import io.github.meritepk.starter.util.IdGenerator;

@SuppressWarnings("serial")
public class TimeSequenceIdGenerator implements BeforeExecutionGenerator {

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object value, EventType type) {
        return IdGenerator.nextId();
    }
}
