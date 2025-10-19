package com.payments.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** AggregateRoot - base class providing domain event recording. */
public abstract class AggregateRoot<T> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected void registerEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  protected void addDomainEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
