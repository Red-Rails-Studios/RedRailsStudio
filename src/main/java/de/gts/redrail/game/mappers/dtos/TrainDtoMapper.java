package de.gts.redrail.game.mappers.dtos;

import de.gts.redrail.game.models.dtos.TrainDto;
import de.gts.redrail.game.models.entities.Train;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrainDtoMapper {

    public TrainDto map(Train train) {
        if (train == null) {
            return null;
        }

        TrainDto trainDto = new TrainDto();

        trainDto.setUId(train.getUId());
        trainDto.setLevel(trainDto.getLevel());

        return trainDto;
    }

    public List<TrainDto> map(List<Train> trainList) {
        if (trainList == null) {
            return null;
        }

        List<TrainDto> trainDtoList = new ArrayList<>();

        for (Train train : trainList) {
            TrainDto trainDto = map(train);

            if (trainDto != null) {
                trainDtoList.add(trainDto);
            }
        }

        return trainDtoList;
    }
}
