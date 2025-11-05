package de.gts.redrail.game.mappers.dtos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import de.gts.redrail.game.models.dtos.StationDto;
import de.gts.redrail.game.models.entities.Station;

@Component
public class StationDtoMapper {

    public StationDto map(Station station) {
        if (station == null) {
            return null;
        }

        StationDto stationDto = new StationDto();

        stationDto.setUId(station.getUId());
        stationDto.setLevel(station.getLevel());
        stationDto.setMasterUID(station.getMasterUid());

        return stationDto;
    }

    public List<StationDto> map(List<Station> stationList) {
        if (stationList == null) {
            return null;
        }

        List<StationDto> stationDtoList = new ArrayList<>();

        for (Station station : stationList) {
            StationDto stationDto = map(station);

            if (stationDto != null) {
                stationDtoList.add(stationDto);
            }
        }

        return stationDtoList;
    }
}
