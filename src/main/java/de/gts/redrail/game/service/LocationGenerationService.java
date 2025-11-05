package de.gts.redrail.game.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.constants.LocationEnum;
import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Station;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LocationGenerationService {
    RandomCustomersGenerationService customerService = new RandomCustomersGenerationService();
    
    // should generate 1 Random Location using the other services that generate the needed data
    // like random name, random point, etc.
    public Location generateRandomLocation(int x, int y, LocationEnum type, String name) {
        
        int customers = customerService.generateRandomCustomers(type);
        
        // Create a properly initialized station
        Station station = new Station();
        station.setUId(java.util.UUID.randomUUID().toString());
        station.setLevel(1);
        station.setRequiredEmployees(3);
        station.setRequiredPower(4);
        station.setTrainCapacity(10);
        station.setRailCapacity(5);
        station.setTrains(new ArrayList<>());
        // masterUID will be set when a player claims/buys the station

        return new Location(type, name, customers, station, x, y); 
    }

}
