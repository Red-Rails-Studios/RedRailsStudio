package de.gts.redrail.game.models.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import de.gts.redrail.game.models.entities.Field;

@Data


public class Map {
 private List<List<Field>> map;

 public Map() {
  map = new ArrayList<>();
  for (int x = 0; x < 40; x++) {
    List<Field> row = new ArrayList<>();
    for (int y = 0; y < 40; y++) {
        row.add(new Field());
    }
    map.add(row);
  }
 }
}   
 