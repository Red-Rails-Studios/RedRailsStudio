package de.gts.redrail.game.mappers.dtos;

import de.gts.redrail.game.models.dtos.RailDto;
import de.gts.redrail.game.models.entities.Rail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RailDtoMapper {

    public RailDto map(Rail rail) {
        if (rail == null) {
            return null;
        }

        RailDto railDto = new RailDto();

        railDto.setUid(rail.getUId());
        railDto.setLevel(rail.getLevel());

        return railDto;
    }

    public List<RailDto> map(List<Rail> railList) {
        if (railList == null) {
            return null;
        }

        List<RailDto> railDtoList = new ArrayList<>();

        for (Rail rail : railList) {
            RailDto railDto = map(rail);

            if (railDto != null) {
                railDtoList.add(railDto);
            }
        }

        return railDtoList;
    }
}
