package de.gts.redrail.game.models.entities;

import de.gts.redrail.game.mappers.entities.ResourceRack;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Player {
    String uId;
    String name;
    Integer points = 0;

    ResourceRack resourceRack = new ResourceRack();

    List<Station> stations = new ArrayList<>();
    List<Train> trains = new ArrayList<>();
    List<Rail> rails = new ArrayList<>();
}
