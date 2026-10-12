package com.subhash.apimonitoring.notification.repository;


import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Basic queries
    List<Alert> findByIncidentId(Long incidentId);
    List<Alert> findByApiId(Long apiId);
    List<Alert> findByStatus(AlertStatus status);

    // Pending alerts for retry
    List<Alert> findByStatusAndNextRetryAtBefore(AlertStatus status, LocalDateTime time);

    // Rate limiting
    long countByRecipientAndCreatedAtAfter(String recipient, LocalDateTime time);

    // Recent alerts
    List<Alert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime time);

    // Statistics
    @Query("SELECT a.channel, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.channel")
    List<Object[]> countAlertsByChannel(@Param("since") LocalDateTime since);

    @Query("SELECT a.status, COUNT(a) FROM Alert a WHERE a.createdAt >= :since GROUP BY a.status")
    List<Object[]> countAlertsByStatus(@Param("since") LocalDateTime since);

    // Update queries for retry
    @Modifying
    @Transactional
    @Query("UPDATE Alert a SET a.retryCount = a.retryCount + 1, " +
            "a.nextRetryAt = :nextRetry, a.status = 'RETRYING' WHERE a.id = :id")
    int markForRetry(@Param("id") Long id, @Param("nextRetry") LocalDateTime nextRetry);

    @Modifying
    @Transactional
    @Query("UPDATE Alert a SET a.status = 'SENT', a.sentAt = :sentAt WHERE a.id = :id")
    int markAsSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);

    @Modifying
    @Transactional
    @Query("UPDATE Alert a SET a.status = 'FAILED', a.failedAt = :failedAt, " +
            "a.errorMessage = :error WHERE a.id = :id")
    int markAsFailed(@Param("id") Long id, @Param("failedAt") LocalDateTime failedAt,
                     @Param("error") String error);
}