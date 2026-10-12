package com.subhash.apimonitoring.incident.repository;



import com.subhash.apimonitoring.incident.entity.IncidentTimeline;
import com.subhash.apimonitoring.incident.enums.TimelineActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentTimelineRepository extends JpaRepository<IncidentTimeline, Long> {

    List<IncidentTimeline> findByIncidentIdOrderByPerformedAtDesc(Long incidentId);
    Page<IncidentTimeline> findByIncidentId(Long incidentId, Pageable pageable);
    List<IncidentTimeline> findByIncidentIdAndActionType(Long incidentId, TimelineActionType actionType);
}
