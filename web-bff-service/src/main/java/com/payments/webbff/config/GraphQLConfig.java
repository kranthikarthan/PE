package com.payments.webbff.config;

import com.graphql-java.extended.scalars.GraphQLExtendedScalars;
import com.graphql-java.extended.validation.GraphQLExtendedValidation;
import graphql.GraphQL;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * GraphQL Configuration for Web BFF Service.
 *
 * <p>This configuration sets up the GraphQL schema, resolvers, and instrumentation
 * for the Payment Engine Web BFF service.
 *
 * @since PE-414
 */
@Configuration
public class GraphQLConfig {

    @Autowired
    private List<Instrumentation> instrumentations;

    /**
     * Creates the GraphQL instance with schema and configuration.
     *
     * @param schema the GraphQL schema
     * @return configured GraphQL instance
     */
    @Bean
    public GraphQL graphQL(GraphQLSchema schema) {
        return GraphQL.newGraphQL(schema)
                .instrumentation(new DataLoaderDispatcherInstrumentation())
                .build();
    }

    /**
     * Creates the GraphQL schema from the schema definition file.
     *
     * @param runtimeWiring the runtime wiring configuration
     * @return configured GraphQL schema
     * @throws IOException if schema file cannot be read
     */
    @Bean
    public GraphQLSchema graphQLSchema(RuntimeWiring runtimeWiring) throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        ClassPathResource schemaResource = new ClassPathResource("graphql/schema.graphqls");
        
        try (InputStream schemaStream = schemaResource.getInputStream()) {
            TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaStream);
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        }
    }

    /**
     * Creates the runtime wiring for GraphQL resolvers and scalars.
     *
     * @param paymentQueryResolver payment query resolver
     * @param paymentMutationResolver payment mutation resolver
     * @param paymentSubscriptionResolver payment subscription resolver
     * @param settlementQueryResolver settlement query resolver
     * @param settlementMutationResolver settlement mutation resolver
     * @param settlementSubscriptionResolver settlement subscription resolver
     * @param reconciliationQueryResolver reconciliation query resolver
     * @param reconciliationMutationResolver reconciliation mutation resolver
     * @param reconciliationSubscriptionResolver reconciliation subscription resolver
     * @param monitoringQueryResolver monitoring query resolver
     * @param monitoringMutationResolver monitoring mutation resolver
     * @param monitoringSubscriptionResolver monitoring subscription resolver
     * @param batchQueryResolver batch query resolver
     * @param batchMutationResolver batch mutation resolver
     * @param batchSubscriptionResolver batch subscription resolver
     * @param statisticsQueryResolver statistics query resolver
     * @return configured runtime wiring
     */
    @Bean
    public RuntimeWiring runtimeWiring(
            com.payments.webbff.resolver.PaymentQueryResolver paymentQueryResolver,
            com.payments.webbff.resolver.PaymentMutationResolver paymentMutationResolver,
            com.payments.webbff.resolver.PaymentSubscriptionResolver paymentSubscriptionResolver,
            com.payments.webbff.resolver.SettlementQueryResolver settlementQueryResolver,
            com.payments.webbff.resolver.SettlementMutationResolver settlementMutationResolver,
            com.payments.webbff.resolver.SettlementSubscriptionResolver settlementSubscriptionResolver,
            com.payments.webbff.resolver.ReconciliationQueryResolver reconciliationQueryResolver,
            com.payments.webbff.resolver.ReconciliationMutationResolver reconciliationMutationResolver,
            com.payments.webbff.resolver.ReconciliationSubscriptionResolver reconciliationSubscriptionResolver,
            com.payments.webbff.resolver.MonitoringQueryResolver monitoringQueryResolver,
            com.payments.webbff.resolver.MonitoringMutationResolver monitoringMutationResolver,
            com.payments.webbff.resolver.MonitoringSubscriptionResolver monitoringSubscriptionResolver,
            com.payments.webbff.resolver.BatchQueryResolver batchQueryResolver,
            com.payments.webbff.resolver.BatchMutationResolver batchMutationResolver,
            com.payments.webbff.resolver.BatchSubscriptionResolver batchSubscriptionResolver,
            com.payments.webbff.resolver.StatisticsQueryResolver statisticsQueryResolver) {
        
        return RuntimeWiring.newRuntimeWiring()
                // Scalars
                .scalar(GraphQLExtendedScalars.DateTime)
                .scalar(GraphQLExtendedScalars.BigDecimal)
                .scalar(GraphQLExtendedScalars.UUID)
                
                // Query resolvers
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("payments", paymentQueryResolver::payments)
                        .dataFetcher("payment", paymentQueryResolver::payment)
                        .dataFetcher("settlementWorkflows", settlementQueryResolver::settlementWorkflows)
                        .dataFetcher("settlementWorkflow", settlementQueryResolver::settlementWorkflow)
                        .dataFetcher("nettingPositions", settlementQueryResolver::nettingPositions)
                        .dataFetcher("settlementOrchestrations", settlementQueryResolver::settlementOrchestrations)
                        .dataFetcher("reconciliationRuns", reconciliationQueryResolver::reconciliationRuns)
                        .dataFetcher("reconciliationRun", reconciliationQueryResolver::reconciliationRun)
                        .dataFetcher("reconciliationExceptions", reconciliationQueryResolver::reconciliationExceptions)
                        .dataFetcher("reconciliationException", reconciliationQueryResolver::reconciliationException)
                        .dataFetcher("settlementMonitoring", monitoringQueryResolver::settlementMonitoring)
                        .dataFetcher("settlementMetrics", monitoringQueryResolver::settlementMetrics)
                        .dataFetcher("settlementAlerts", monitoringQueryResolver::settlementAlerts)
                        .dataFetcher("batchJobs", batchQueryResolver::batchJobs)
                        .dataFetcher("batchJob", batchQueryResolver::batchJob)
                        .dataFetcher("paymentStatistics", statisticsQueryResolver::paymentStatistics)
                        .dataFetcher("settlementStatistics", statisticsQueryResolver::settlementStatistics)
                        .dataFetcher("reconciliationStatistics", statisticsQueryResolver::reconciliationStatistics)
                )
                
                // Mutation resolvers
                .type("Mutation", typeWiring -> typeWiring
                        .dataFetcher("createPayment", paymentMutationResolver::createPayment)
                        .dataFetcher("updatePayment", paymentMutationResolver::updatePayment)
                        .dataFetcher("cancelPayment", paymentMutationResolver::cancelPayment)
                        .dataFetcher("createSettlementWorkflow", settlementMutationResolver::createSettlementWorkflow)
                        .dataFetcher("updateSettlementWorkflow", settlementMutationResolver::updateSettlementWorkflow)
                        .dataFetcher("startSettlementWorkflow", settlementMutationResolver::startSettlementWorkflow)
                        .dataFetcher("completeSettlementWorkflow", settlementMutationResolver::completeSettlementWorkflow)
                        .dataFetcher("createReconciliationRun", reconciliationMutationResolver::createReconciliationRun)
                        .dataFetcher("startReconciliationRun", reconciliationMutationResolver::startReconciliationRun)
                        .dataFetcher("completeReconciliationRun", reconciliationMutationResolver::completeReconciliationRun)
                        .dataFetcher("createReconciliationException", reconciliationMutationResolver::createReconciliationException)
                        .dataFetcher("assignReconciliationException", reconciliationMutationResolver::assignReconciliationException)
                        .dataFetcher("resolveReconciliationException", reconciliationMutationResolver::resolveReconciliationException)
                        .dataFetcher("acknowledgeAlert", monitoringMutationResolver::acknowledgeAlert)
                        .dataFetcher("resolveAlert", monitoringMutationResolver::resolveAlert)
                        .dataFetcher("startBatchJob", batchMutationResolver::startBatchJob)
                        .dataFetcher("stopBatchJob", batchMutationResolver::stopBatchJob)
                        .dataFetcher("restartBatchJob", batchMutationResolver::restartBatchJob)
                )
                
                // Subscription resolvers
                .type("Subscription", typeWiring -> typeWiring
                        .dataFetcher("paymentStatusChanged", paymentSubscriptionResolver::paymentStatusChanged)
                        .dataFetcher("paymentCreated", paymentSubscriptionResolver::paymentCreated)
                        .dataFetcher("settlementWorkflowStatusChanged", settlementSubscriptionResolver::settlementWorkflowStatusChanged)
                        .dataFetcher("settlementWorkflowCompleted", settlementSubscriptionResolver::settlementWorkflowCompleted)
                        .dataFetcher("reconciliationRunStatusChanged", reconciliationSubscriptionResolver::reconciliationRunStatusChanged)
                        .dataFetcher("reconciliationExceptionCreated", reconciliationSubscriptionResolver::reconciliationExceptionCreated)
                        .dataFetcher("reconciliationExceptionStatusChanged", reconciliationSubscriptionResolver::reconciliationExceptionStatusChanged)
                        .dataFetcher("settlementAlertCreated", monitoringSubscriptionResolver::settlementAlertCreated)
                        .dataFetcher("settlementAlertStatusChanged", monitoringSubscriptionResolver::settlementAlertStatusChanged)
                        .dataFetcher("batchJobStatusChanged", batchSubscriptionResolver::batchJobStatusChanged)
                        .dataFetcher("batchJobCompleted", batchSubscriptionResolver::batchJobCompleted)
                )
                
                // Extended validation
                .directive(GraphQLExtendedValidation.newValidationDirective())
                .build();
    }
}
