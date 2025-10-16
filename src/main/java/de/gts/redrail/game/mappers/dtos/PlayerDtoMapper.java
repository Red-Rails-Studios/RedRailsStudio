package de.gts.redrail.game.mappers.dtos;

import de.gts.redrail.game.models.dtos.PlayerDto;
import de.gts.redrail.game.models.entities.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerDtoMapper {

    private final StationDtoMapper stationDtoMapper;
    private final TrainDtoMapper trainDtoMapper;
    private final RailDtoMapper railDtoMapper;

    public PlayerDto map(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDto playerDto = new PlayerDto();

        playerDto.setUId(player.getUId());
        playerDto.setName(player.getName());
        playerDto.setColor(player.getColor());

        playerDto.setDbCoin(player.getResourceRack().getDbCoin());
        playerDto.setEmployees(player.getResourceRack().getEmployees());
        playerDto.setPower(player.getResourceRack().getPower());

        playerDto.setStationDtos(stationDtoMapper.map(player.getStations()));
        playerDto.setTrainDtos(trainDtoMapper.map(player.getTrains()));
        playerDto.setRailDtos(railDtoMapper.map(player.getRails()));

        return playerDto;
    }
}
