package de.gts.redrail.game.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpgradeRequirements {
    private int RequiredDbCoin;
    private int RequiredPower;
    private int RequiredEmployees;
    private String UIdOfObjectToUpgrade;
}