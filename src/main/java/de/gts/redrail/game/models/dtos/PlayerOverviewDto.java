package de.gts.redrail.game.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerOverviewDto {
    private String uId;
    private String name;
    private String color;
}
