package com.subhash.apimonitoring.incident.repository;


import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.enums.IncidentSeverity;
import com.subhash.apimonitoring.incident.enums.IncidentStatus;
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
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Basic queries
    Optional<Incident> findByIncidentNumber(String incidentNumber);
    List<Incident> findByApiId(Long apiId);
    List<Incident> findByStatus(IncidentStatus status);
    List<Incident> findBySeverity(IncidentSeverity severity);

    // Active incidents
    @Query("SELECT i FROM Incident i WHERE i.status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING')")
    List<Incident> findAllActiveIncidents();

    @Query("SELECT i FROM Incident i WHERE i.status = 'OPEN' ORDER BY i.severity DESC, i.startedAt ASC")
    List<Incident> findOpenIncidentsByPriority();

    // Unacknowledged incidents (for escalation)
    @Query("SELECT i FROM Incident i WHERE i.status = 'OPEN' AND i.acknowledgedAt IS NULL")
    List<Incident> findUnacknowledgedIncidents();

    // Incidents by time range
    List<Incident> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Incident> findByResolvedAtBetween(LocalDateTime start, LocalDateTime end);

    // Assignment queries
    List<Incident> findByAssignedToId(Long userId);
    List<Incident> findByAssignedToIdAndStatusIn(Long userId, List<IncidentStatus> statuses);

    // Escalation and reminders
    @Query("SELECT i FROM Incident i WHERE i.status = 'OPEN' AND i.escalatedAt IS NULL AND i.startedAt < :time")
    List<Incident> findIncidentsNeedingEscalation(@Param("time") LocalDateTime time);

    @Query("SELECT i FROM Incident i WHERE i.status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING') " +
            "AND (i.reminderSentAt IS NULL OR i.reminderSentAt < :cutoff)")
    List<Incident> findIncidentsNeedingReminder(@Param("cutoff") LocalDateTime cutoff);

    // Statistics queries
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = 'CLOSED' AND i.resolvedAt BETWEEN :start AND :end")
    long countResolvedIncidents(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(i.timeToResolveSeconds) FROM Incident i WHERE i.status = 'CLOSED' AND i.severity = :severity")
    Optional<Double> getAverageResolutionTimeBySeverity(@Param("severity") IncidentSeverity severity);

    @Query("SELECT i.severity, COUNT(i) FROM Incident i WHERE i.startedAt >= :since GROUP BY i.severity")
    List<Object[]> countIncidentsBySeverity(@Param("since") LocalDateTime since);

    // Update queries
    @Modifying
    @Transactional
    @Query("UPDATE Incident i SET i.acknowledgedAt = :time, i.status = 'ACKNOWLEDGED' WHERE i.id = :id")
    int acknowledgeIncident(@Param("id") Long id, @Param("time") LocalDateTime time);

    @Modifying
    @Transactional
    @Query("UPDATE Incident i SET i.resolvedAt = :time, i.resolvedBy = :userId, " +
            "i.status = 'RESOLVED', i.durationSeconds = :duration WHERE i.id = :id")
    int resolveIncident(@Param("id") Long id, @Param("time") LocalDateTime time,
                        @Param("userId") Long userId, @Param("duration") Integer duration);

    @Modifying
    @Transactional
    @Query("UPDATE Incident i SET i.escalatedAt = :time, i.escalationCount = i.escalationCount + 1 WHERE i.id = :id")
    int escalateIncident(@Param("id") Long id, @Param("time") LocalDateTime time);
}