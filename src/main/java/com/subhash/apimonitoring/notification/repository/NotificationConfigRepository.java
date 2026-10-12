package com.subhash.apimonitoring.notification.repository;



import com.subhash.apimonitoring.notification.entity.NotificationConfig;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {

    List<NotificationConfig> findByUserIdAndEnabledTrue(Long userId);
    List<NotificationConfig> findByTeamIdAndEnabledTrue(Long teamId);
    List<NotificationConfig> findByChannelAndEnabledTrue(AlertChannel channel);

    List<NotificationConfig> findByUserIdAndAlertTypeAndEnabledTrue(Long userId, AlertType alertType);
}
