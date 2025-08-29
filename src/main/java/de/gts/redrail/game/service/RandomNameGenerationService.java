package de.gts.redrail.game.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.constants.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RandomNameGenerationService {
    private final Random r = new Random();

    public String generateRandomGermanCityName() {
        int idx = r.nextInt(GermanCityName.fickStadte.length);  // Unprofessional name for variable but I like bbg
        return GermanCityName.fickStadte[idx]; // Takes a random city name from the predefined list
    }    
}
