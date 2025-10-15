package de.gts.redrail.game.models.entities;

import de.gts.redrail.game.mappers.entities.ResourceRack;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import de.gts.redrail.game.constants.Color;

@Data
@NoArgsConstructor
public class Player {
    String uId;
    String name;
    Integer points = 0;
    String color;

    ResourceRack resourceRack = new ResourceRack();

    List<Station> stations;
    List<Train> trains;
    List<Rail> rails;
}
