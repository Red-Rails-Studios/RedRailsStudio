package de.gts.redrail.game.models.entities;

import de.gts.redrail.game.constants.LocationEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private LocationEnum type;  
    private String name;
    private Integer population;
    private Station station;
}