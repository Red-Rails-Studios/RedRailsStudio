package de.gts.redrail.game.constants;

public interface MaxLocation {
    Integer cornerMaxLocations = 4; // Max locations in corners
    Integer cornerMaxVillages = 2;
    Integer cornerMaxTowns = 2;

    Integer sidesMaxLocations = 36; // Max locations on the sides
    Integer sidesMaxVillages = 16;
    Integer sidesMaxTowns = 12;
    Integer sidesMaxCities = 6;
    Integer sidesMaxMetropolises = 2;

    Integer middleMaxLocations = 108; // Max locations in the middle
    Integer middleMaxVillages = 48;
    Integer middleMaxTowns = 32;
    Integer middleMaxCities = 20;
    Integer middleMaxMetropolises = 8;
}
