package de.gts.redrail.game.service;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.gts.redrail.game.constants.LocationEnum;
import de.gts.redrail.game.constants.MaxLocation;
import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Map;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapSpaceGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(MapSpaceGenerationService.class);

    private final LocationGenerationService lGS;
    private final RandomPointGenerationService rPGS;
    private final RandomNameGenerationService rNGS;
    private final Map map;
    private final AtomicBoolean generated = new AtomicBoolean(false);
    private final ArrayList<String> usedNames = new ArrayList<>();
    // track used coordinate pairs as "x,y" to avoid placing two locations on same field
    private final java.util.Set<String> usedCoords = new java.util.HashSet<>();

    public Map getMap() {
        return this.map;
    }

    public boolean isGenerated() {
        return generated.get();
    }

    public void ensureGeneratedAsync() {
        if (generated.compareAndSet(false, true)) {
            Thread t = new Thread(() -> {
                try {
                    generateBordersForPlayers();
                } catch (Exception e) {
                    // on failure reset flag so a future attempt can retry
                    generated.set(false);
                    throw e;
                }
            }, "map-space-generation");
            t.setDaemon(true);
            t.start();
        }
    }

    public void generateBordersForPlayers() {
    usedNames.clear();
    usedCoords.clear();
    logger.info("Map generation started");
    fillCorners();
    fillSides();
    fillMiddle();
    logger.info("Map generation finished");
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

                // regenerate while this exact pair is already used or adjacent to an existing location
                String coordKey = CoordinateX + "," + CoordinateY;
                int safety = 0;
                while ((usedCoords.contains(coordKey) || !isAdjacentFree(CoordinateX, CoordinateY)) && safety < 40) {
                    CoordinateX = rPGS.generateRandomPointX(xStart, xEnd);
                    CoordinateY = rPGS.generateRandomPointY(yStart, yEnd);
                    coordKey = CoordinateX + "," + CoordinateY;
                    safety++;
                }
                if (safety >= 40) {
                    logger.warn("fillSpace: failed to find non-adjacent free coordinate after {} attempts in region x[{},{}] y[{},{}]",
                            safety, xStart, xEnd, yStart, yEnd);
                }
                
                Location location = lGS.generateRandomLocation(CoordinateX, CoordinateY, type, name);

                if (location != null) {
                    // bounds check before placing
                    if (location.getX() >= 0 && location.getX() < map.getMap().size()
                            && location.getY() >= 0 && location.getY() < map.getMap().get(location.getX()).size()) {
                        // only place if field is currently empty and adjacency rule is satisfied
                        if (map.getMap().get(location.getX()).get(location.getY()).getLocation() == null
                                && isAdjacentFree(location.getX(), location.getY())) {
                            locationsPlaced++;
                            map.getMap().get(location.getX()).get(location.getY()).setLocation(location);
                            usedCoords.add(coordKey);
                            logger.info("Placed location {} at x={}, y={} type={}", name, location.getX(), location.getY(), type);
                        } else {
                            if (map.getMap().get(location.getX()).get(location.getY()).getLocation() != null) {
                                logger.warn("Attempt to place location {} at x={}, y={} but field already occupied", name,
                                        location.getX(), location.getY());
                            } else {
                                logger.warn("Attempt to place location {} at x={}, y={} violates adjacency rule", name,
                                        location.getX(), location.getY());
                            }
                        }
                    } else {
                        logger.warn("Generated location out of bounds x={}, y={} for map size={}", location.getX(), location.getY(), map.getMap().size());
                    }
                } else {
                    logger.warn("LocationGenerationService returned null for coordinates x={}, y={} type={}", CoordinateX, CoordinateY, type);
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

    private boolean isAdjacentFree(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < map.getMap().size()
                        && ny >= 0 && ny < map.getMap().get(nx).size()) {
                    if (map.getMap().get(nx).get(ny).getLocation() != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
