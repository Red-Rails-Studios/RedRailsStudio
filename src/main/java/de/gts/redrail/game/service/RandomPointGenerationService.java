package de.gts.redrail.game.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RandomPointGenerationService {

    private final Random r = new Random();

    public Integer generateRandomPointX(int xStart, int xEnd) {
        return r.nextInt(xEnd - xStart) + xStart; // Generates a random number between xStart (inclusive) and xEnd (exclusive)
    }

    public Integer generateRandomPointY(int yStart, int yEnd) {
        return r.nextInt(yEnd - yStart) + yStart; // Generates a random number between yStart (inclusive) and yEnd (exclusive)
    }
}