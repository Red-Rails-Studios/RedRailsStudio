package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Map;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapSpaceGenerationService {

    private final SessionService sessionService;
    private final LocationGenerationService locationGenerationService;
    private final Map map;

    private final int borderWidth = 6;
    private final int borderHeight = 6;
    private final int borderMaxLocation = 3;

    private final int sidesWidthHeight = 18;
    private final int sidesHeightWidth = 6;
    private final int sidesMaxLocation = 36;

    private final int middleWidthHeight = 18;
    private final int middleMaxLocation = 108;
    

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
        fillSpace(0, borderWidth, 0, borderHeight, borderMaxLocation);
        fillSpace(map.getMap().size() - borderWidth, map.getMap().size(), 0, borderHeight, borderMaxLocation);
        fillSpace(0, borderWidth, map.getMap().get(0).size() - borderHeight, map.getMap().get(0).size(), borderMaxLocation);
        fillSpace(map.getMap().size() - borderWidth, map.getMap().size(), map.getMap().get(0).size() - borderHeight, map.getMap().get(0).size(), borderMaxLocation);
    }

    private void fillSides() {
        fillSpace(6, 24, 0, 6, sidesMaxLocation); // Under side
        fillSpace(6,24, 24, 30, sidesMaxLocation); // Upper side
        fillSpace(0, 6, 6, 24, sidesMaxLocation); // Left side
        fillSpace(24, 30, 6, 24, sidesMaxLocation); // Right side
    }

    private void fillMiddle() {
        fillSpace(6, 24, 6, 24, middleMaxLocation); // Middle area
    }
}
