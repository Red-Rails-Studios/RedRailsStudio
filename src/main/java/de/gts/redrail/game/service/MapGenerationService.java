package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Random;
import de.gts.redrail.game.models.entities.Map;
import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.constants.LocationEnum;

@Service
@RequiredArgsConstructor

public class MapGenerationService {
    Map map = new Map();
    Random random = new Random();

    public Location generateRandomLocation() {
        LocationEnum[] locationTypes = LocationEnum.values();
        int randomIndex = random.nextInt(locationTypes.length);
        LocationEnum randomType = locationTypes[randomIndex];

        Integer customers = 0;
        switch (randomType) {
            case VILLAGE:
                customers = random.nextInt(100) + 50; // 50 to 150 customers
                break;
            case TOWN:
                customers = random.nextInt(300) + 200; // 200 to 500 customers
                break;
            case CITY:
                customers = random.nextInt(300) + 600; // 600 to 900 customers
                break;
            case METROPOLIS:
                customers = random.nextInt(4000) + 1000; // 1000 to 5000 customers
                break;
        }
        

        return new Location(randomType, name, customers, null);
    }
}
