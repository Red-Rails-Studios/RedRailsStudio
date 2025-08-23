package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;
import de.gts.redrail.game.constants.LocationEnum;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RandomCustomersGenerationService {

    public Integer generateRandomCustomers(LocationEnum Type) {

        int min = 0;
        int max = 0;

        switch (Type) {
            case VILLAGE -> {
                min = 50;
                max = 150;
            }
            case TOWN -> {
                min = 200;
                max = 500;
            }
            case CITY -> {
                min = 600;
                max = 900;
            }
            case METROPOLIS -> {
                min = 1000;
                max = 5000;
            }
        }

        return (int) (Math.random() * (max - min + 1)) + min; // Generates a random number between min (inclusive) and max (inclusive)
    }
    
}
