package de.gts.redrail.game.models.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public enum GameEvent {
    WINTER_STORM("Winter Storm", "Trains move slower due to snow", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Store original capacities and reduce by 20%
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 0.8));
            }
        }
        
        @Override
        public void reverse(Player player) {
            // Restore original capacities
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    },
    
    HOLIDAY_RUSH("Holiday Rush", "Increased passenger demand!", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            // Store original amount and add 25%
            int original = player.getResourceRack().getDbCoin();
            storeOriginalDbCoin(player, original);
            int bonus = (int)(original * 0.25);
            player.getResourceRack().setDbCoin(original + bonus);
        }
        
        @Override
        public void reverse(Player player) {
            // Remove the bonus (keep coins earned during the event)
            restoreOriginalDbCoin(player);
        }
    },
    
    EMPLOYEE_STRIKE("Employee Strike", "Reduced workforce available", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Store original and reduce by 5
            int original = player.getResourceRack().getEmployees();
            storeOriginalEmployees(player, original);
            player.getResourceRack().setEmployees(Math.max(1, original - 5));
        }
        
        @Override
        public void reverse(Player player) {
            // Restore original employees
            restoreOriginalEmployees(player);
        }
    },
    
    GOVERNMENT_SUBSIDY("Government Subsidy", "Receive funding for infrastructure", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            // This is a permanent bonus, no reversal needed
            player.getResourceRack().setDbCoin(player.getResourceRack().getDbCoin() + 500);
        }
        
        @Override
        public void reverse(Player player) {
            // No reversal for permanent bonus
        }
    },

    TECHNICAL_PROBLEMS("Technical Problems", "Trains experience delays due to technical issues", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Store original capacities and reduce by 50%
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 0.5));
            }
        }
        
        @Override
        public void reverse(Player player) {
            // Restore original capacities
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    },

    RUSH_HOUR("Rush Hour", "Your Stations and Trains experience rush hour.", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            // Store original capacities and increase by 30%
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 1.3));
            }
        }
        
        @Override
        public void reverse(Player player) {
            // Restore original capacities
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    }, 

    BONUS_STATION("Bonus Station", "A new station is added to your network", EventType.POSITIVE) { // Fix 2: Fixed typo BONUSE_STATION -> BONUS_STATION
        @Override
        public void apply(Player player) {
            Station newStation = new Station();
            newStation.setUId(UUID.randomUUID().toString());
            newStation.setLevel(1); 
            newStation.setTrainCapacity(5); 
            player.getStations().add(newStation);
        }
        
        @Override
        public void reverse(Player player) {
            
            if (!player.getStations().isEmpty()) {
                player.getStations().remove(player.getStations().size() - 1);
            }
        }
    }; 

    private final String name;
    private final String description;
    private final EventType type;
    
    // Storage for original values
    private static final Map<String, Map<String, Integer>> originalCapacities = new HashMap<>();
    private static final Map<String, Integer> originalEmployees = new HashMap<>();
    private static final Map<String, Integer> originalDbCoins = new HashMap<>();

    GameEvent(String name, String description, EventType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public abstract void apply(Player player);
    public abstract void reverse(Player player);

    // Helper methods for storing/restoring original values
    protected void storeOriginalCapacity(Player player, Train train) {
        originalCapacities.computeIfAbsent(player.getUId(), k -> new HashMap<>())
                         .put(train.getUId(), train.getCapacity());
    }
    
    protected void restoreOriginalCapacity(Player player, Train train) {
        Map<String, Integer> playerCapacities = originalCapacities.get(player.getUId());
        if (playerCapacities != null && playerCapacities.containsKey(train.getUId())) {
            train.setCapacity(playerCapacities.get(train.getUId()));
            playerCapacities.remove(train.getUId());
        }
    }
    
    protected void storeOriginalEmployees(Player player, int employees) {
        originalEmployees.put(player.getUId(), employees);
    }
    
    protected void restoreOriginalEmployees(Player player) {
        Integer original = originalEmployees.get(player.getUId());
        if (original != null) {
            player.getResourceRack().setEmployees(original);
            originalEmployees.remove(player.getUId());
        }
    }
    
    protected void storeOriginalDbCoin(Player player, int dbCoin) {
        originalDbCoins.put(player.getUId(), dbCoin);
    }
    
    protected void restoreOriginalDbCoin(Player player) {
        Integer original = originalDbCoins.get(player.getUId());
        if (original != null) {
            // Calculate how much was earned during the event
            int current = player.getResourceRack().getDbCoin();
            int bonus = (int)(original * 0.25);
            int earnedDuringEvent = current - (original + bonus);
            // Set to original + what was earned during event
            player.getResourceRack().setDbCoin(original + earnedDuringEvent);
            originalDbCoins.remove(player.getUId());
        }
    }

    public static GameEvent getRandomEvent() {
        GameEvent[] events = values();
        return events[new Random().nextInt(events.length)];
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public EventType getType() { return type; }
}

enum EventType {
    POSITIVE, NEGATIVE, NEUTRAL
}
