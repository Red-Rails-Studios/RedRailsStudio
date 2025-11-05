package de.gts.redrail.mappers.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gts.redrail.game.models.dtos.PlayerOverviewDto;
import org.junit.jupiter.api.Test;

class PlayerOverviewDtoMapperTest {

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        PlayerOverviewDto playerOverviewDto = new PlayerOverviewDto();
        playerOverviewDto.setUid("12345678");
        playerOverviewDto.setName("Player1");

        String jsonStr = objectMapper.writeValueAsString(playerOverviewDto);
    }
}