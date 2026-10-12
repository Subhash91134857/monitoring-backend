package com.subhash.apimonitoring.monitoring.repository;


import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.enums.ApiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoredApiRepository extends JpaRepository<MonitoredApi, Long> {

    // Basic queries
    Optional<MonitoredApi> findByName(String name);
    List<MonitoredApi> findByIsActiveTrue();
    List<MonitoredApi> findByCurrentStatus(ApiStatus status);

    // Paginated queries
    Page<MonitoredApi> findByCategory(String category, Pageable pageable);
    Page<MonitoredApi> findByIsActiveTrue(Pageable pageable);

    // Count queries
    long countByCurrentStatus(ApiStatus status);
    long countByIsActiveTrue();

    // Search queries
    @Query("SELECT a FROM MonitoredApi a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.url) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MonitoredApi> searchApis(@Param("search") String search, Pageable pageable);

    // Update queries
    @Modifying
    @Transactional
    @Query("UPDATE MonitoredApi a SET a.currentStatus = :status, a.lastCheckTime = CURRENT_TIMESTAMP WHERE a.id = :id")
    int updateApiStatus(@Param("id") Long id, @Param("status") ApiStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE MonitoredApi a SET a.uptimePercentage = :uptime WHERE a.id = :id")
    int updateUptimePercentage(@Param("id") Long id, @Param("uptime") Double uptime);

    // Bulk operations
    @Modifying
    @Transactional
    @Query("UPDATE MonitoredApi a SET a.isActive = false WHERE a.id = :id")
    int deactivateApi(@Param("id") Long id);

    // Monitoring queries
    @Query("SELECT a FROM MonitoredApi a WHERE a.isActive = true AND a.currentStatus != 'DOWN'")
    List<MonitoredApi> findActiveNonDownApis();

    @Query(value = "SELECT * FROM monitored_apis WHERE is_active = true ORDER BY priority DESC", nativeQuery = true)
    List<MonitoredApi> findActiveApisOrderByPriority();
}
