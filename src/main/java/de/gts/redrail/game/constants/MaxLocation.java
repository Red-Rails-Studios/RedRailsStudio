package de.gts.redrail.game.constants;

public interface MaxLocation {
    Integer cornerMaxLocations = 4; // Max locations in corners
    Integer cornerMaxVillages = 2;
    Integer cornerMaxTowns = 2;

    Integer sidesMaxLocations = 20; // Max locations on the sides
    Integer sidesMaxVillages = 8;
    Integer sidesMaxTowns = 6;
    Integer sidesMaxCities = 4;
    Integer sidesMaxMetropolises = 2;

    Integer middleMaxLocations = 40; // Max locations in the middle
    Integer middleMaxVillages = 16;
    Integer middleMaxTowns = 12;
    Integer middleMaxCities = 8;
    Integer middleMaxMetropolises = 4;
}
