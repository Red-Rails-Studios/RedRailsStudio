package de.gts.redrail.game.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Map;
import de.gts.redrail.game.constants.LocationEnum;
import de.gts.redrail.game.constants.MaxLocation;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapSpaceGenerationService {

    private final LocationGenerationService lGS;
    private final RandomPointGenerationService rPGS;
    private final RandomNameGenerationService rNGS;
    private final Map map;
    private final ArrayList<String> usedNames = new ArrayList<>();
    private final ArrayList<Integer> usedX = new ArrayList<>();
    private final ArrayList<Integer> usedY = new ArrayList<>();

    public Map getMap() {
        return this.map;
    }

    public void generateBordersForPlayers() {
        fillCorners();
        fillSides();
        fillMiddle();
    }

    private void fillSpace(int xStart, int xEnd, int yStart, int yEnd, int maxLocations, LocationEnum type) {
        int locationsPlaced = 0;
        String name;
        Integer CoordinateX;
        Integer CoordinateY;

        for (int x = xStart; x < xEnd && x < map.getMap().size(); x++) {
            for (int y = yStart; y < yEnd && y < map.getMap().get(x).size(); y++) {

                if (locationsPlaced >= maxLocations) {
                    return ; // Stop if max locations reached
                }

                name = rNGS.generateRandomGermanCityName();

                while (usedNames.contains(name)) {
                    name = rNGS.generateRandomGermanCityName();
                }

                usedNames.add(name);
                CoordinateX = rPGS.generateRandomPointX(xStart, xEnd);
                CoordinateY = rPGS.generateRandomPointY(yStart, yEnd);
                
                while (usedX.contains(CoordinateX) && usedY.contains(CoordinateY)) {
                    CoordinateX = rPGS.generateRandomPointX(xStart, xEnd);
                    CoordinateY = rPGS.generateRandomPointY(yStart, yEnd);
                }

                usedX.add(CoordinateX);
                usedY.add(CoordinateY);
                
                Location location = lGS.generateRandomLocation(CoordinateX, CoordinateY, type, name);

                if (location != null) {
                    locationsPlaced++;
                    map.getMap().get(location.getX()).get(location.getY()).setLocation(location);
                }
            }
        }
    }

    private void fillCorners() {
        fillCornersVillages();
        fillCornersTowns();
    }

    private void fillCornersVillages() {
        fillSpace(0, 6, 0, 6, MaxLocation.cornerMaxVillages, LocationEnum.VILLAGE); // Bottom left corner
        fillSpace(24, 30, 0, 6, MaxLocation.cornerMaxVillages, LocationEnum.VILLAGE); // Bottom right corner
        fillSpace(0, 6, 24, 30, MaxLocation.cornerMaxVillages, LocationEnum.VILLAGE); // Top left corner
        fillSpace(24, 30, 24, 30, MaxLocation.cornerMaxVillages, LocationEnum.VILLAGE); // Top right corner
    }

    private void fillCornersTowns() {
        fillSpace(0, 6, 0, 6, MaxLocation.cornerMaxTowns, LocationEnum.TOWN); // Bottom left corner
        fillSpace(24, 30, 0, 6, MaxLocation.cornerMaxTowns, LocationEnum.TOWN); // Bottom right corner
        fillSpace(0, 6, 24, 30, MaxLocation.cornerMaxTowns, LocationEnum.TOWN); // Top left corner
        fillSpace(24, 30, 24, 30, MaxLocation.cornerMaxTowns, LocationEnum.TOWN); // Top right corner
    }


    private void fillSides() {
        fillSidesVillages();
        fillSidesTown();
        fillSidesCities();
        fillSidesMetropolises();
    }

    private void fillSidesVillages() {
        fillSpace(6, 24, 0, 6, MaxLocation.sidesMaxVillages, LocationEnum.VILLAGE); // Under side
        fillSpace(6,24, 24, 30, MaxLocation.sidesMaxVillages, LocationEnum.VILLAGE); // Upper side
        fillSpace(0, 6, 6, 24, MaxLocation.sidesMaxVillages, LocationEnum.VILLAGE); // Left side
        fillSpace(24, 30, 6, 24, MaxLocation.sidesMaxVillages, LocationEnum.VILLAGE); // Right side
    }

    private void fillSidesTown() {
        fillSpace(6, 24, 0, 6, MaxLocation.sidesMaxTowns, LocationEnum.TOWN); // Under side
        fillSpace(6,24, 24, 30, MaxLocation.sidesMaxTowns, LocationEnum.TOWN); // Upper side
        fillSpace(0, 6, 6, 24, MaxLocation.sidesMaxTowns, LocationEnum.TOWN); // Left side
        fillSpace(24, 30, 6, 24, MaxLocation.sidesMaxTowns, LocationEnum.TOWN); // Right side
    }

    private void fillSidesCities() {
        fillSpace(6, 24, 0, 6, MaxLocation.sidesMaxCities, LocationEnum.CITY); // Under side
        fillSpace(6,24, 24, 30, MaxLocation.sidesMaxCities, LocationEnum.CITY); // Upper side
        fillSpace(0, 6, 6, 24, MaxLocation.sidesMaxCities, LocationEnum.CITY); // Left side
        fillSpace(24, 30, 6, 24, MaxLocation.sidesMaxCities, LocationEnum.CITY); // Right side
    }

    private void fillSidesMetropolises() {
        fillSpace(6, 24, 0, 6, MaxLocation.sidesMaxMetropolises, LocationEnum.METROPOLIS); // Under side
        fillSpace(6,24, 24, 30,  MaxLocation.sidesMaxMetropolises, LocationEnum.METROPOLIS); // Upper side
        fillSpace(0, 6, 6, 24,  MaxLocation.sidesMaxMetropolises, LocationEnum.METROPOLIS); // Left side
        fillSpace(24, 30, 6, 24,  MaxLocation.sidesMaxMetropolises, LocationEnum.METROPOLIS); // Right side
    }

    private void fillMiddle() {
        fillMiddleVillages();
        fillMiddleTown();
        fillMiddleCities();
        fillMiddleMetropolises();
    }

    private void fillMiddleVillages() {
        fillSpace(6, 24, 6, 24, MaxLocation.middleMaxVillages, LocationEnum.VILLAGE); // Middle area
    }

    private void fillMiddleTown() {
        fillSpace(6, 24, 6, 24, MaxLocation.middleMaxTowns, LocationEnum.TOWN); // Middle area
    }

    private void fillMiddleCities() {
        fillSpace(6, 24, 6, 24, MaxLocation.middleMaxCities, LocationEnum.CITY); // Middle area
    }

    private void fillMiddleMetropolises() {
        fillSpace(6, 24, 6, 24, MaxLocation.middleMaxMetropolises, LocationEnum.METROPOLIS); // Middle area
    }
}
