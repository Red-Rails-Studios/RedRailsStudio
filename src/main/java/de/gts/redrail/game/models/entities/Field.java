package de.gts.redrail.game.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Field {
    private Location location;
    private Integer x;
    private Integer y;
    private boolean isOccupied = false;
}