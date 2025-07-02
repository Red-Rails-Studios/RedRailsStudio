package de.gts.redrail.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import de.gts.redrail.game.models.entities.Map;
import de.gts.redrail.game.models.entities.Location;

@Service
@RequiredArgsConstructor
public class MapBorderGenerationService {

    private final SessionService sessionService;
    private final LocationGenerationService locationGenerationService;
    private final Map map;

    private final int borderWidth = 6;
    private final int borderHeight = 6;
    private final int maxLocation = 3;

    /**
     * Generates borders for players depending on player count.
     */
    public void generateBordersForPlayers() {
        int playerCount = sessionService.getAllPlayer().size();

        switch (playerCount) {
            case 1 -> fillBorder(0, borderWidth, 0, borderHeight, maxLocation);
            case 2 -> {
                fillBorder(0, borderWidth, 0, borderHeight, maxLocation);
                fillBorder(map.getMap().size() - borderWidth, map.getMap().size(), 0, borderHeight, maxLocation);
            }
            case 3 -> {
                fillBorder(0, borderWidth, 0, borderHeight, maxLocation);
                fillBorder(map.getMap().size() - borderWidth, map.getMap().size(), 0, borderHeight, maxLocation);
                fillBorder(0, borderWidth, map.getMap().get(0).size() - borderHeight, map.getMap().get(0).size(), maxLocation);
            }
            case 4 -> {
                fillBorder(0, borderWidth, 0, borderHeight, maxLocation);
                fillBorder(map.getMap().size() - borderWidth, map.getMap().size(), 0, borderHeight, maxLocation);
                fillBorder(0, borderWidth, map.getMap().get(0).size() - borderHeight, map.getMap().get(0).size(), maxLocation);
                fillBorder(map.getMap().size() - borderWidth, map.getMap().size(), map.getMap().get(0).size() - borderHeight, map.getMap().get(0).size(), maxLocation);
            }
        }
    }

    /**
     * Fills a rectangular border area with up to maxLocations random locations.
     */
    private void fillBorder(int xStart, int xEnd, int yStart, int yEnd, int maxLocations) {
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
}
