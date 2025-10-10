package com.paymentengine.config.repository;

import com.paymentengine.config.entity.ConfigurationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConfigurationHistoryRepository extends JpaRepository<ConfigurationHistory, UUID> {
    
    List<ConfigurationHistory> findByConfigType(ConfigurationHistory.ConfigType configType);
    
    List<ConfigurationHistory> findByConfigId(UUID configId);
    
    List<ConfigurationHistory> findByConfigKey(String configKey);
    
    List<ConfigurationHistory> findByChangedBy(String changedBy);
    
    List<ConfigurationHistory> findByChangedAtAfter(LocalDateTime date);
    
    List<ConfigurationHistory> findByChangedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.configType = :configType AND ch.configId = :configId ORDER BY ch.changedAt DESC")
    List<ConfigurationHistory> findByConfigTypeAndConfigIdOrderByChangedAtDesc(@Param("configType") ConfigurationHistory.ConfigType configType, 
                                                                              @Param("configId") UUID configId);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.configType = :configType AND ch.configKey = :configKey ORDER BY ch.changedAt DESC")
    List<ConfigurationHistory> findByConfigTypeAndConfigKeyOrderByChangedAtDesc(@Param("configType") ConfigurationHistory.ConfigType configType, 
                                                                               @Param("configKey") String configKey);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.changedBy = :changedBy AND ch.changedAt >= :startDate ORDER BY ch.changedAt DESC")
    List<ConfigurationHistory> findByChangedByAndChangedAtAfterOrderByChangedAtDesc(@Param("changedBy") String changedBy, 
                                                                                   @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(ch) FROM ConfigurationHistory ch WHERE ch.configType = :configType")
    Long countByConfigType(@Param("configType") ConfigurationHistory.ConfigType configType);
    
    @Query("SELECT COUNT(ch) FROM ConfigurationHistory ch WHERE ch.changedBy = :changedBy AND ch.changedAt >= :startDate")
    Long countByChangedByAndChangedAtAfter(@Param("changedBy") String changedBy, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ch FROM ConfigurationHistory ch WHERE ch.changedAt < :cutoffDate")
    List<ConfigurationHistory> findOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    void deleteByChangedAtBefore(LocalDateTime cutoffDate);
}