package de.gts.redrail.game.service;

import org.springframework.stereotype.Service;
import de.gts.redrail.game.constants.MaxLocation;
import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Map;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapSpaceGenerationService {

    private final LocationGenerationService lGS;
    private final RandomPointGenerationService rPGS;
    private final Map map;

    public void generateBordersForPlayers() {
        fillCorners();
        fillSides();
        fillMiddle();
    }

    private void fillSpace(int xStart, int xEnd, int yStart, int yEnd, int maxLocations) {
        int locationsPlaced = 0;

        for (int x = xStart; x < xEnd && x < map.getMap().size(); x++) {
            for (int y = yStart; y < yEnd && y < map.getMap().get(x).size(); y++) {

                if (locationsPlaced >= maxLocations) {
                    return ; // Stop if max locations reached
                }

                Location location = lGS.generateRandomLocation(rPGS.generateRandomPointX(xStart, xEnd), rPGS.generateRandomPointY(yStart, yEnd));

                if (location != null) {
                    locationsPlaced++;
                }
            }
        }
    }

    private void fillCorners() {
        fillSpace(0, 6, 0, 6, MaxLocation.cornerMaxLocations); // Bottom left corner
        fillSpace(24, 30, 0, 6, MaxLocation.cornerMaxLocations); // Bottom right corner
        fillSpace(0, 6, 24, 30, MaxLocation.cornerMaxLocations); // Top left corner
        fillSpace(24, 30, 24, 30, MaxLocation.cornerMaxLocations); // Top right corner
    }

    private void fillSides() {
        fillSpace(6, 24, 0, 6, MaxLocation.sidesMaxLocations); // Under side
        fillSpace(6,24, 24, 30, MaxLocation.sidesMaxLocations); // Upper side
        fillSpace(0, 6, 6, 24, MaxLocation.sidesMaxLocations); // Left side
        fillSpace(24, 30, 6, 24, MaxLocation.sidesMaxLocations); // Right side
    }

    private void fillMiddle() {
        fillSpace(6, 24, 6, 24, MaxLocation.middleMaxLocations); // Middle area
    }
}
