package de.gts.redrail.game.models.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public enum GameEvent {
    WINTER_STORM("Winter Storm", "Trains move slower due to snow", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 0.8));
            }
        }
        
        @Override
        public void reverse(Player player) {
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    },
    
    HOLIDAY_RUSH("Holiday Rush", "Increased passenger demand!", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            int original = player.getResourceRack().getDbCoin();
            storeOriginalDbCoin(player, original);
            int bonus = (int)(original * 0.25);
            player.getResourceRack().setDbCoin(original + bonus);
        }
        
        @Override
        public void reverse(Player player) {
            restoreOriginalDbCoin(player);
        }
    },
    
    EMPLOYEE_STRIKE("Employee Strike", "Reduced workforce available", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            int original = player.getResourceRack().getEmployees();
            storeOriginalEmployees(player, original);
            player.getResourceRack().setEmployees(Math.max(1, original - 5));
        }
        
        @Override
        public void reverse(Player player) {
            restoreOriginalEmployees(player);
        }
    },
    
    GOVERNMENT_SUBSIDY("Government Subsidy", "Receive funding for infrastructure", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
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
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 0.5));
            }
        }
        
        @Override
        public void reverse(Player player) {
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    },

    RUSH_HOUR("Rush Hour", "Your Stations and Trains experience rush hour.", EventType.POSITIVE) {
        @Override
        public void apply(Player player) {
            for (Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 1.3));
            }
        }
        
        @Override
        public void reverse(Player player) {
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    }, 

    BONUS_STATION("Bonus Station", "A new station is added to your network", EventType.POSITIVE) {
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
    }, 

    POWER_OUTAGE("Power Outage", "Trains are unable to operate for a short period", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            // Store original capacities and set to 0 for 50% of trains
            for (int i = 0; i < player.getTrains().size() / 2; i++) {
                Train train = player.getTrains().get(i);
                storeOriginalCapacity(player, train);
                train.setCapacity(0);
            }
            // Store original capacity and set to 0 for 50% of Stations
            for (int i = 0; i < player.getStations().size() / 2; i++) {
                Station station = player.getStations().get(i);
                storeOriginalStationCapacity(player, station);
                station.setTrainCapacity(0);
            }
        }
        
        @Override
        public void reverse(Player player) {
            // Restore original capacities
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
            // Restore original capacities for Stations
            for (Station station : player.getStations()) {
                restoreOriginalStationCapacity(player, station);
            }
        }
    },

    CRACKHEADS_ON_STATION("Crackheads on Station", "Your station is overrun by crackheads, reducing capacity of trains", EventType.NEGATIVE) {
        @Override
        public void apply(Player player) {
            for(Train train : player.getTrains()) {
                storeOriginalCapacity(player, train);
                train.setCapacity((int)(train.getCapacity() * 0.5)); // Reduce capacity by 50%
            }
        }
        
        @Override
        public void reverse(Player player) {
            for (Train train : player.getTrains()) {
                restoreOriginalCapacity(player, train);
            }
        }
    }; 

    private final String name;
    private final String description;
    private final EventType type;
    
    // Storage for original values
    private static final Map<String, Map<String, Integer>> originalCapacities = new HashMap<>();
    private static final Map<String, Map<String, Integer>> originalStationCapacities = new HashMap<>();
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
   
    protected void storeOriginalStationCapacity(Player player, Station station) {
        originalStationCapacities.computeIfAbsent(player.getUId(), k -> new HashMap<>())
                                .put(station.getUId(), station.getTrainCapacity());
    }
    
    protected void restoreOriginalStationCapacity(Player player, Station station) {
        Map<String, Integer> playerStationCapacities = originalStationCapacities.get(player.getUId());
        if (playerStationCapacities != null && playerStationCapacities.containsKey(station.getUId())) {
            station.setTrainCapacity(playerStationCapacities.get(station.getUId()));
            playerStationCapacities.remove(station.getUId());
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
            int current = player.getResourceRack().getDbCoin();
            int bonus = (int)(original * 0.25);
            int earnedDuringEvent = current - (original + bonus);
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
