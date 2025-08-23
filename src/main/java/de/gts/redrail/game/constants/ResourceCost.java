package de.gts.redrail.game.constants;

public interface ResourceCost {
    Integer NEW_RAIL = 60;
    Integer NEW_TRAIN = 500;
    Integer NEW_STATION = 800;

    Integer UPGRADE_RAIL_FACTOR = NEW_RAIL + (NEW_RAIL / 4); // 25% increase for rail upgrade
    Integer UPGRADE_TRAIN_FACTOR = NEW_TRAIN + (NEW_TRAIN / 3);  // 33% increase for train upgrade
    Integer UPGRADE_STATION_FACTOR = NEW_STATION + (NEW_STATION / 2);  // 50% increase for station upgrade
}
