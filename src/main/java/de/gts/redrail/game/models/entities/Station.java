package de.gts.redrail.game.models.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import de.gts.redrail.game.models.entities.Train;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    String uId;
    Integer level;
    Integer requiredEmployees = 3;
    Integer requiredPower = 4;
    Integer trainCapacity = 10;
    Integer railCapacity = 5;
    String masterUid;
    List<Train> trains = new ArrayList<>();

}
