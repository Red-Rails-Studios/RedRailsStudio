package de.gts.redrail.game.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActionResult {
    boolean successful = false;
    String message;
    String uid; 

    public ActionResult(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }
}
