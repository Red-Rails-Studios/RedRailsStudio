package de.gts.redrail.game.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.constants.LocationEnum;
import de.gts.redrail.game.models.entities.Location;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationGenerationService {
    Random random = new Random();

    // should generate 1 Random Location using the other services that generate the needed data
    // like random name, random point, etc.
    public Location generateRandomLocation(int x, int y, LocationEnum type) {
        
        return new Location(); 
    }

}
