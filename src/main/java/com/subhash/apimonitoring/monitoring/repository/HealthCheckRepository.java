package com.subhash.apimonitoring.monitoring.repository;


import com.subhash.apimonitoring.monitoring.entity.HealthCheck;
import com.subhash.apimonitoring.monitoring.enums.HealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long>, JpaSpecificationExecutor<HealthCheck> {

    // Recent checks
    List<HealthCheck> findByApiIdOrderByCheckedAtDesc(Long apiId, Pageable pageable);
    Optional<HealthCheck> findFirstByApiIdOrderByCheckedAtDesc(Long apiId);

    // Status queries
    List<HealthCheck> findByApiIdAndStatusAndCheckedAtAfter(Long apiId, HealthStatus status, LocalDateTime since);

    // Count queries
    long countByApiIdAndCheckedAtBetween(Long apiId, LocalDateTime start, LocalDateTime end);
    long countByApiIdAndStatusAndCheckedAtBetween(Long apiId, HealthStatus status, LocalDateTime start, LocalDateTime end);

    // Analytics queries
    @Query("SELECT AVG(h.responseTimeMs) FROM HealthCheck h WHERE h.api.id = :apiId AND h.status = 'UP' AND h.checkedAt >= :since")
    Optional<Double> getAverageResponseTime(@Param("apiId") Long apiId, @Param("since") LocalDateTime since);

    @Query("SELECT h FROM HealthCheck h WHERE h.api.id = :apiId AND h.checkedAt >= :since ORDER BY h.checkedAt ASC")
    List<HealthCheck> getHealthHistory(@Param("apiId") Long apiId, @Param("since") LocalDateTime since);

    // Downtime detection
    @Query("SELECT h FROM HealthCheck h WHERE h.api.id = :apiId AND h.status != 'UP' AND h.checkedAt > :since ORDER BY h.checkedAt DESC")
    List<HealthCheck> findFailuresSince(@Param("apiId") Long apiId, @Param("since") LocalDateTime since);

    // Performance metrics
    @Query(value = """
        SELECT 
            DATE(checked_at) as date,
            COUNT(*) as total_checks,
            SUM(CASE WHEN status = 'UP' THEN 1 ELSE 0 END) as successful_checks,
            AVG(response_time_ms) as avg_response_time,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY response_time_ms) as p95_response_time
        FROM health_checks
        WHERE api_id = :apiId AND checked_at >= :since
        GROUP BY DATE(checked_at)
        ORDER BY date DESC
        """, nativeQuery = true)
    List<Object[]> getDailyStats(@Param("apiId") Long apiId, @Param("since") LocalDateTime since);

    /**
     * Get health checks with filters and pagination
     */
    @Query("SELECT h FROM HealthCheck h " +
            "LEFT JOIN FETCH h.api " +
            "WHERE (:apiId IS NULL OR h.api.id = :apiId) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND (:startDate IS NULL OR h.checkedAt >= :startDate) " +
            "AND (:endDate IS NULL OR h.checkedAt <= :endDate) " +
            "ORDER BY h.checkedAt DESC")
    Page<HealthCheck> findWithFilters(
            @Param("apiId") Long apiId,
            @Param("status") HealthStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
