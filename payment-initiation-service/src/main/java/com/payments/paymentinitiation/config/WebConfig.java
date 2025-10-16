package com.payments.paymentinitiation.config;

import com.payments.paymentinitiation.api.validation.PaymentValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 *
 * <p>Configures web-related components: - Validation interceptors - CORS settings -
 * Request/response handling
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final PaymentValidationInterceptor paymentValidationInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(paymentValidationInterceptor)
        .addPathPatterns("/api/v1/payments/initiate");
  }
}
