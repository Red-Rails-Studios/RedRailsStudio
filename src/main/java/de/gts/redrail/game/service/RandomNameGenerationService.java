package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import de.gts.redrail.game.constants.GermanCityName;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RandomNameGenerationService {
    private final Random r = new Random();

    public String generateRandomGermanCityName() {
        int idx = r.nextInt(GermanCityName.fickStadte.length); 
        return GermanCityName.fickStadte[idx]; // Takes a random city name from the predefined list
    }    
}
