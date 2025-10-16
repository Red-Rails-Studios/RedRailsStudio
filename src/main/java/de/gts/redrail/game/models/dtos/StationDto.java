package de.gts.redrail.game.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationDto {

    String uId;
    Integer level;
    String masterUID;
}
