package com.paymentengine.paymentprocessing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "features")
public class FeatureToggleProperties {

    private final Toggle kafka = new Toggle();
    private final Toggle fraud = new Toggle();
    private final Validation validation = new Validation();

    public Toggle getKafka() {
        return kafka;
    }

    public Toggle getFraud() {
        return fraud;
    }

    public Validation getValidation() {
        return validation;
    }

    public static class Validation {
        private final Toggle heavy = new Toggle();

        public Toggle getHeavy() {
            return heavy;
        }
    }

    public static class Toggle {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
