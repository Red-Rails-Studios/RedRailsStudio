package de.gts.redrail.game.models.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Station {
    String uId;
    Integer level;
    Integer requierdEmployes = 3;
    Integer requiredPower = 4;
    Integer trainCapacity = 5; // Default capacity
    Integer railCapacity = 5;
}
