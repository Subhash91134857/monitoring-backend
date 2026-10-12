package com.subhash.apimonitoring.notification.factory;


import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.strategy.AlertStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Alert Strategy Factory
 *
 * WHY - Factory Pattern:
 * Centralized creation and selection of alert strategies
 *
 * HOW:
 * - All strategies are auto-wired via constructor
 * - Builds channel-to-strategy mapping at startup
 * - Provides quick lookup by channel type
 *
 * RESULT: Clean strategy selection without if-else chains
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertStrategyFactory {

    private final List<AlertStrategy> strategies;
    private final Map<AlertChannel, AlertStrategy> strategyMap = new EnumMap<>(AlertChannel.class);

    @PostConstruct
    public void init() {
        for (AlertStrategy strategy : strategies) {
            strategyMap.put(strategy.getChannel(), strategy);
            log.info("Registered alert strategy for channel: {}", strategy.getChannel());
        }
    }

    public AlertStrategy getStrategy(AlertChannel channel) {
        AlertStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for channel: " + channel);
        }
        return strategy;
    }

    public boolean hasStrategy(AlertChannel channel) {
        return strategyMap.containsKey(channel);
    }
}