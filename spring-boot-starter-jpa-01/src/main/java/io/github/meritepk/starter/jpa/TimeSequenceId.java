package io.github.meritepk.starter.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.hibernate.annotations.IdGeneratorType;

@IdGeneratorType(TimeSequenceIdGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeSequenceId {
}
