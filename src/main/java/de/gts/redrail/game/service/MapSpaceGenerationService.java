package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Map;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapSpaceGenerationService {

    private final LocationGenerationService locationGenerationService;
    private final Map map;

    private final int borderMaxLocations = 3;
    private final int sidesMaxLocations = 36;
    private final int middleMaxLocations = 108;
    

    /**
     * Generates borders for players depending on player count.
     */
    public void generateBordersForPlayers() {
        fillBorders();
        fillSides();
        fillMiddle();
    }

    /**
     * Fills a rectangular border area with up to maxLocations random locations.
     */
    private void fillSpace(int xStart, int xEnd, int yStart, int yEnd, int maxLocations) {
        int locationsPlaced = 0;
        for (int x = xStart; x < xEnd && x < map.getMap().size(); x++) {
            for (int y = yStart; y < yEnd && y < map.getMap().get(x).size(); y++) {

                if (locationsPlaced >= maxLocations) return;

                Location location = locationGenerationService.generateRandomLocation();

                if (location != null) {
                    map.getMap().get(x).get(y).setLocation(location);
                    locationsPlaced++;
                }
            }
        }
    }

    private void fillBorders() {
        fillSpace(0, 6, 0, 6, borderMaxLocations); // Bottom left corner
        fillSpace(24, 30, 0, 6, borderMaxLocations); // Bottom right corner
        fillSpace(0, 6, 24, 30, borderMaxLocations); // Top left corner
        fillSpace(24, 30, 24, 30, borderMaxLocations); // Top right corner
    }

    private void fillSides() {
        fillSpace(6, 24, 0, 6, sidesMaxLocations); // Under side
        fillSpace(6,24, 24, 30, sidesMaxLocations); // Upper side
        fillSpace(0, 6, 6, 24, sidesMaxLocations); // Left side
        fillSpace(24, 30, 6, 24, sidesMaxLocations); // Right side
    }

    private void fillMiddle() {
        fillSpace(6, 24, 6, 24, middleMaxLocations); // Middle area
    }
}
