package de.gts.redrail.game.constants;
public interface ResourceGeneration {

    Integer STATION_RESOURCE_GENERATION_FACTOR = 2;
    Integer TRAIN_RESOURCE_GENERATION_FACTOR = 10;
    
    // Generation interval in seconds; set to 1s for more frequent updates
    Long RESOURCE_GENERATION_INTERVAL_IN_SECONDS = 1L;
}
