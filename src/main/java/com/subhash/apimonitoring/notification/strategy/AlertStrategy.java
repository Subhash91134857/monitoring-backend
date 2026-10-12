package com.subhash.apimonitoring.notification.strategy;



import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.enums.AlertChannel;

/**
 * Alert Strategy Interface
 *
 * WHY - Strategy Pattern & Open-Closed Principle:
 * Each channel has its own implementation
 * New channels can be added without modifying existing code
 *
 * RESULT: Extensible, maintainable notification system
 */
public interface AlertStrategy {

    /**
     * Send notification using specific channel
     */
    Alert send(NotificationRequest request);

    /**
     * Get the channel this strategy handles
     */
    AlertChannel getChannel();

    /**
     * Validate if the destination is valid for this channel
     */
    boolean isValidDestination(String destination);
}
