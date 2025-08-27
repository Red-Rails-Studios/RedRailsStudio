package de.gts.redrail.game.models.entities;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import de.gts.redrail.game.service.LocationGenerationService;
import de.gts.redrail.game.models.entities.Field;
import lombok.Data;

@Component
@Data
public class Map {
 private List<List<Field>> map;
 private LocationGenerationService locationGenerationService;

 public Map() {
  map = new ArrayList<>();
  locationGenerationService = new LocationGenerationService();

  for (int x = 0; x < 30; x++) {
    List<Field> row = new ArrayList<>();

    for (int y = 0; y < 30; y++) {
      Field field = new Field();
      row.add(field);
    }
    
    map.add(row);
  }
 }
}   
 