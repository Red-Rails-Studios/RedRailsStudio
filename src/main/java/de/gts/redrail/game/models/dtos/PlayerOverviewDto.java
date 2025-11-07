package de.gts.redrail.game.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerOverviewDto {
    private String uid;
    private String name;
    private String color;
}
