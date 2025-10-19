package com.payments.webbff.resolver;

import com.payments.webbff.dto.SettlementWorkflowDto;
import com.payments.webbff.dto.NettingPositionDto;
import com.payments.webbff.dto.SettlementOrchestrationDto;
import com.payments.webbff.service.SettlementService;
import com.payments.webbff.type.SettlementStatus;
import com.payments.webbff.type.OrchestrationStatus;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Query Resolver for Settlement operations.
 *
 * <p>This resolver handles all settlement-related queries in the GraphQL API,
 * providing access to settlement workflows, netting positions, and orchestration data.
 *
 * @since PE-414
 */
@Component
public class SettlementQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private SettlementService settlementService;

    /**
     * Retrieves a list of settlement workflows with optional filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param status optional settlement status filter
     * @param limit maximum number of results (default: 50)
     * @param offset offset for pagination (default: 0)
     * @return list of settlement workflows
     */
    public List<SettlementWorkflowDto> settlementWorkflows(
            UUID tenantId,
            UUID businessUnitId,
            SettlementStatus status,
            Integer limit,
            Integer offset) {
        
        return settlementService.getSettlementWorkflows(
                tenantId,
                businessUnitId,
                status,
                limit != null ? limit : 50,
                offset != null ? offset : 0
        );
    }

    /**
     * Retrieves a specific settlement workflow by ID.
     *
     * @param id the settlement workflow ID
     * @return the settlement workflow or null if not found
     */
    public SettlementWorkflowDto settlementWorkflow(UUID id) {
        return settlementService.getSettlementWorkflow(id);
    }

    /**
     * Retrieves netting positions for a specific netting cycle.
     *
     * @param nettingCycleId the netting cycle ID
     * @param tenantId the tenant ID
     * @return list of netting positions
     */
    public List<NettingPositionDto> nettingPositions(UUID nettingCycleId, UUID tenantId) {
        return settlementService.getNettingPositions(nettingCycleId, tenantId);
    }

    /**
     * Retrieves a list of settlement orchestrations with optional filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param status optional orchestration status filter
     * @param limit maximum number of results (default: 50)
     * @param offset offset for pagination (default: 0)
     * @return list of settlement orchestrations
     */
    public List<SettlementOrchestrationDto> settlementOrchestrations(
            UUID tenantId,
            UUID businessUnitId,
            OrchestrationStatus status,
            Integer limit,
            Integer offset) {
        
        return settlementService.getSettlementOrchestrations(
                tenantId,
                businessUnitId,
                status,
                limit != null ? limit : 50,
                offset != null ? offset : 0
        );
    }
}
