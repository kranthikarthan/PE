package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.ResiliencyConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResiliencyConfigurationRepository extends JpaRepository<ResiliencyConfiguration, Long> {
}
