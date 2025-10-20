package com.payments.e2e;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * E2E Test Runner
 * 
 * Executes all E2E tests using Cucumber framework
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/java/com/payments/e2e/features",
    glue = "com.payments.e2e.stepdefinitions",
    plugin = {
        "pretty",
        "html:target/cucumber-reports",
        "json:target/cucumber-reports/Cucumber.json",
        "junit:target/cucumber-reports/Cucumber.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@payment-initiation or @payment-validation or @payment-processing or @payment-clearing or @payment-settlement",
    monochrome = true,
    dryRun = false
)
public class E2ETestRunner {
    // Test runner configuration
}
