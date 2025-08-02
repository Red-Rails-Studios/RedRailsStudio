package de.gts.redrail.game.models.entities;

import java.util.List;
import java.util.Random;

public enum EventsEnum {
    WINTER_STORM("Winter Storm", "Trains move slower due to snow", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Reduce train efficiency by 20%
            player.getTrains().forEach(train -> 
                train.setCapacity((int)(train.getCapacity() * 0.8))
            );
        }
    },
    
    HOLIDAY_RUSH("Holiday Rush", "Increased passenger demand!", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            // Increase DB coins by 25%
            int bonus = (int)(player.getResourceRack().getDbCoin() * 0.25);
            player.getResourceRack().setDbCoin(player.getResourceRack().getDbCoin() + bonus);
        }
    },
    
    EMPLOYEE_STRIKE("Employee Strike", "Reduced workforce available", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Reduce employees by 2
            int current = player.getResourceRack().getEmployees();
            player.getResourceRack().setEmployees(Math.max(1, current - 2));
        }
    },
    
    GOVERNMENT_SUBSIDY("Government Subsidy", "Receive funding for infrastructure", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            // Add 500 DB coins
            player.getResourceRack().setDbCoin(player.getResourceRack().getDbCoin() + 500);
        }
    };

    private final String name;
    private final String description;
    private final EventType type;

    GameEvent(String name, String description, EventType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public abstract void apply(Player player);

    public static GameEvent getRandomEvent() {
        GameEvent[] events = values();
        return events[new Random().nextInt(events.length)];
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventType getType() { return type; }
}

enum EventType {
    POSITIVE, NEGATIVE, NEUTRAL
}
