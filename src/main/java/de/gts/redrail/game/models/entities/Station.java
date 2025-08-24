package de.gts.redrail.game.models.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Station {
    String uId;
    Integer level;
    Integer requiredEmployees = 3;
    Integer requiredPower = 4;
    Integer trainCapacity = 10;
    Integer railCapacity = 5;
    String masterUID;
}
