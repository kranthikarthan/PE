package com.paymentengine.config.service;

import com.paymentengine.config.entity.Tenant;
import com.paymentengine.config.entity.ConfigurationHistory;
import com.paymentengine.config.repository.TenantRepository;
import com.paymentengine.config.repository.ConfigurationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ConfigurationHistoryRepository configurationHistoryRepository;

    @InjectMocks
    private TenantService tenantService;

    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setId(UUID.randomUUID());
        testTenant.setName("Test Tenant");
        testTenant.setCode("TEST");
        testTenant.setContactEmail("test@example.com");
        testTenant.setContactPhone("+1234567890");
        testTenant.setAddress("123 Test Street");
        testTenant.setStatus(Tenant.TenantStatus.ACTIVE);
        testTenant.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findById_ExistingId_ReturnsTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));

        // When
        Optional<Tenant> result = tenantService.findById(tenantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTenant, result.get());
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // Given
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // When
        Optional<Tenant> result = tenantService.findById(tenantId);

        // Then
        assertFalse(result.isPresent());
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    void findByCode_ExistingCode_ReturnsTenant() {
        // Given
        String code = "TEST";
        when(tenantRepository.findByCode(code)).thenReturn(Optional.of(testTenant));

        // When
        Optional<Tenant> result = tenantService.findByCode(code);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTenant, result.get());
        verify(tenantRepository).findByCode(code);
    }

    @Test
    void findByCode_NonExistingCode_ReturnsEmpty() {
        // Given
        String code = "NONEXISTENT";
        when(tenantRepository.findByCode(code)).thenReturn(Optional.empty());

        // When
        Optional<Tenant> result = tenantService.findByCode(code);

        // Then
        assertFalse(result.isPresent());
        verify(tenantRepository).findByCode(code);
    }

    @Test
    void findAll_ReturnsAllTenants() {
        // Given
        List<Tenant> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findAll()).thenReturn(tenants);

        // When
        List<Tenant> result = tenantService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testTenant, result.get(0));
        verify(tenantRepository).findAll();
    }

    @Test
    void findByStatus_ReturnsTenantsWithStatus() {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        List<Tenant> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByStatus(status)).thenReturn(tenants);

        // When
        List<Tenant> result = tenantService.findByStatus(status);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTenant, result.get(0));
        verify(tenantRepository).findByStatus(status);
    }

    @Test
    void createTenant_ValidData_ReturnsCreatedTenant() {
        // Given
        when(tenantRepository.existsByCode("TEST")).thenReturn(false);
        when(tenantRepository.existsByContactEmail("test@example.com")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        Tenant result = tenantService.createTenant(
                "Test Tenant", "TEST", "test@example.com", "+1234567890", "123 Test Street");

        // Then
        assertNotNull(result);
        assertEquals(testTenant, result);
        verify(tenantRepository).save(any(Tenant.class));
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void createTenant_CodeExists_ThrowsException() {
        // Given
        when(tenantRepository.existsByCode("TEST")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tenantService.createTenant(
                    "Test Tenant", "TEST", "test@example.com", "+1234567890", "123 Test Street");
        });

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void createTenant_EmailExists_ThrowsException() {
        // Given
        when(tenantRepository.existsByCode("TEST")).thenReturn(false);
        when(tenantRepository.existsByContactEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tenantService.createTenant(
                    "Test Tenant", "TEST", "test@example.com", "+1234567890", "123 Test Street");
        });

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void updateTenant_ValidData_ReturnsUpdatedTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        Tenant result = tenantService.updateTenant(
                tenantId, "Updated Tenant", "updated@example.com", "+0987654321", "456 Updated Street");

        // Then
        assertNotNull(result);
        assertEquals(testTenant, result);
        verify(tenantRepository).save(any(Tenant.class));
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void updateTenant_NonExistingTenant_ThrowsException() {
        // Given
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tenantService.updateTenant(
                    tenantId, "Updated Tenant", "updated@example.com", "+0987654321", "456 Updated Street");
        });

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void activateTenant_ValidTenant_ActivatesTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        testTenant.setStatus(Tenant.TenantStatus.INACTIVE);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        tenantService.activateTenant(tenantId);

        // Then
        assertEquals(Tenant.TenantStatus.ACTIVE, testTenant.getStatus());
        verify(tenantRepository).save(any(Tenant.class));
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void deactivateTenant_ValidTenant_DeactivatesTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        tenantService.deactivateTenant(tenantId);

        // Then
        assertEquals(Tenant.TenantStatus.INACTIVE, testTenant.getStatus());
        verify(tenantRepository).save(any(Tenant.class));
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void suspendTenant_ValidTenant_SuspendsTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        tenantService.suspendTenant(tenantId);

        // Then
        assertEquals(Tenant.TenantStatus.SUSPENDED, testTenant.getStatus());
        verify(tenantRepository).save(any(Tenant.class));
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void deleteTenant_ValidTenant_DeletesTenant() {
        // Given
        UUID tenantId = testTenant.getId();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(testTenant));
        when(configurationHistoryRepository.save(any(ConfigurationHistory.class)))
                .thenReturn(new ConfigurationHistory());

        // When
        tenantService.deleteTenant(tenantId);

        // Then
        verify(tenantRepository).delete(testTenant);
        verify(configurationHistoryRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void countByStatus_ReturnsCount() {
        // Given
        Tenant.TenantStatus status = Tenant.TenantStatus.ACTIVE;
        Long expectedCount = 5L;
        when(tenantRepository.countByStatus(status)).thenReturn(expectedCount);

        // When
        Long result = tenantService.countByStatus(status);

        // Then
        assertEquals(expectedCount, result);
        verify(tenantRepository).countByStatus(status);
    }

    @Test
    void findByNameContaining_ReturnsMatchingTenants() {
        // Given
        String name = "Test";
        List<Tenant> tenants = Arrays.asList(testTenant);
        when(tenantRepository.findByNameContaining(name)).thenReturn(tenants);

        // When
        List<Tenant> result = tenantService.findByNameContaining(name);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTenant, result.get(0));
        verify(tenantRepository).findByNameContaining(name);
    }

    @Test
    void existsByCode_ExistingCode_ReturnsTrue() {
        // Given
        String code = "TEST";
        when(tenantRepository.existsByCode(code)).thenReturn(true);

        // When
        boolean result = tenantService.existsByCode(code);

        // Then
        assertTrue(result);
        verify(tenantRepository).existsByCode(code);
    }

    @Test
    void existsByCode_NonExistingCode_ReturnsFalse() {
        // Given
        String code = "NONEXISTENT";
        when(tenantRepository.existsByCode(code)).thenReturn(false);

        // When
        boolean result = tenantService.existsByCode(code);

        // Then
        assertFalse(result);
        verify(tenantRepository).existsByCode(code);
    }
}