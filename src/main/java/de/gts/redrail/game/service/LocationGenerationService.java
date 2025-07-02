package de.gts.redrail.game.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import de.gts.redrail.game.constants.LocationEnum;
import de.gts.redrail.game.models.entities.Location;
import de.gts.redrail.game.models.entities.Station;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor

public class LocationGenerationService {
    Random random = new Random();
    List<String> usedCityNames = new ArrayList<>();

   private static final String[] GERMAN_CITIES = {
    "Aach", "Aachen", "Aalen", "Abenberg", "Abensberg", "Achern", "Achim", "Adelsheim", "Adorf/Vogtl.", 
    "Ahaus", "Ahlen", "Ahrensburg", "Aichach", "Aken", "Albstadt", "Alfeld", "Allendorf", "Allersberg",
    "Alsdorf", "Alsfeld", "Altena", "Altenberg", "Altenburg", "Altentreptow", "Altlandsberg", 
    "Altdorf bei Nürnberg", "Altensteig", "Altötting", "Alzenau", "Amberg", "Amorbach", "Andernach", 
    "Angermünde", "Anklam", "Annaberg-Buchholz", "Annweiler", "Ansbach", "Apolda", "Arnsberg", "Arnstadt",
    "Aschaffenburg", "Aschersleben", "Asperg", "Attendorn", "Aub", "Aue-Bad Schlema", "Auerbach", 
    "Augsburg", "Augustusburg", "Aurich", "Babenhausen", "Backnang", "Bad Aibling", "Bad Berleburg",
    "Bad Dürkheim", "Bad Essen", "Bad Fallingbostel", "Bad Gandersheim", "Bad Harzburg", 
    "Bad Homburg vor der Höhe", "Bad Honnef", "Bad Kissingen", "Bad Kreuznach", "Bad Langensalza",
    "Bad Lauchstädt", "Bad Liebenwerda", "Bad Mergentheim", "Bad Münder", "Bad Nauheim", 
    "Bad Nenndorf", "Bad Neustadt an der Saale", "Bad Oldesloe", "Bad Pyrmont", "Bad Rappenau",
    "Bad Reichenhall", "Bad Salzdetfurth", "Bad Salzuflen", "Bad Säckingen", "Bad Saulgau", 
    "Bad Schandau", "Bad Schwartau", "Bad Segeberg", "Bad Soden am Taunus", "Bad Soden-Salmünster", 
    "Bad Staffelstein", "Bad Tölz", "Bad Vilbel", "Bad Waldsee", "Bad Wildbad", "Bad Wildungen",
    
};


    public Location generateRandomLocation() {
        int yesorno = random.nextInt(2);

        if(yesorno == 0)
        {
            return null;
        }

        LocationEnum randomType = getRandomLocationType();

        Integer customers = 0;
        
        switch (randomType) {
            case VILLAGE -> customers = random.nextInt(100) + 50; // 50 to 150 customers
            case TOWN -> customers = random.nextInt(300) + 200; // 200 to 500 customers
            case CITY -> customers = random.nextInt(300) + 600; // 600 to 900 customers
            case METROPOLIS -> customers = random.nextInt(4000) + 1000; // 1000 to 5000 customers
        }
        
        String name = getRandomGermanCityName();
        for (String usedName : usedCityNames) {
            if (usedName.equals(name)) {
                name = getRandomGermanCityName(); // Ensure unique city name
            }
        }

        Station pingpong = new Station(); 

        return new Location(randomType, name, customers, pingpong);
    }

    private LocationEnum getRandomLocationType() {
        LocationEnum[] locationTypes = LocationEnum.values();
        int idx = random.nextInt(locationTypes.length);
        return locationTypes[idx];
    }

    private String getRandomGermanCityName() {
        int idx = random.nextInt(GERMAN_CITIES.length);
        return GERMAN_CITIES[idx];
    }
}
