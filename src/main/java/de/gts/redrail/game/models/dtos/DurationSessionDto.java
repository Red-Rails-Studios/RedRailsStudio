package de.gts.redrail.game.models.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DurationSessionDto {
    private long duration; // in minutes
    private String message;

    public DurationSessionDto(long duration) {
        this.duration = duration;
        this.message = "Session ended successfully.";
    }

    public DurationSessionDto(String message) {
        this.message = message;
    }
}