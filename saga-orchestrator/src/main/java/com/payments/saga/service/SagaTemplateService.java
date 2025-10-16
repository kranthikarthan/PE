package com.payments.saga.service;

import com.payments.saga.domain.SagaTemplate;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for managing saga templates */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaTemplateService {

  private final Map<String, SagaTemplate> templates = new HashMap<>();

  @PostConstruct
  public void initializeDefaultTemplates() {
    log.info("Initializing default saga templates");

    // Register default templates
    registerTemplate(SagaTemplate.createPaymentProcessingTemplate());
    registerTemplate(SagaTemplate.createFastPaymentTemplate());
    registerTemplate(SagaTemplate.createHighValuePaymentTemplate());

    log.info("Initialized {} default templates", templates.size());
  }

  /** Get a saga template by name */
  public SagaTemplate getTemplate(String templateName) {
    log.debug("Getting saga template: {}", templateName);

    return templates.get(templateName);
  }

  /** Register a new saga template */
  public void registerTemplate(SagaTemplate template) {
    log.info("Registering saga template: {}", template.getTemplateName());

    templates.put(template.getTemplateName(), template);
  }

  /** Get all available templates */
  public Map<String, SagaTemplate> getAllTemplates() {
    log.debug("Getting all saga templates");

    return new HashMap<>(templates);
  }

  /** Check if a template exists */
  public boolean hasTemplate(String templateName) {
    return templates.containsKey(templateName);
  }
}
