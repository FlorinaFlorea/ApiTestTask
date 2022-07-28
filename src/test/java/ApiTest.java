import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class ApiTest {

    JSONParser parser = new JSONParser();
    List<String> characters = new ArrayList<>();
    String foundCharacterURL;
    List<String> starShips = new ArrayList<>();
    String foundStarship;


    @Test
    public void findTheMovie() {
        RestAssured.get("https://swapi.dev/api/films").then().statusCode(200).body("results.title", hasItems("A New Hope"));
    }

    @Test
    public void findTheCharacter() throws ParseException {
        String json = RestAssured.get("https://swapi.dev/api/films").asString();
        convertJsonArrayToCharacters(json);
        for (String characterURL : characters) {
            String jsonCharacterResponseBody = RestAssured.get(characterURL).asString();
            JsonPath jsonPath = new JsonPath(jsonCharacterResponseBody);
            if (jsonPath.get("name").equals("Biggs Darklighter")) {
                foundCharacterURL = characterURL;
            }
        }
        RestAssured.get(foundCharacterURL).then().statusCode(200)
                .body("name", equalTo("Biggs Darklighter"));
    }

    @Test
    public void findTheStarship() throws ParseException {
        JSONArray starShipsJsonArray = extractStarships();
        for (int i = 0; i < starShipsJsonArray.size(); i++) {
            starShips.add(starShipsJsonArray.get(i).toString());
        }
        for (String starShip : starShips) {
            String jsonStarShipResponseBody = RestAssured.get(starShip).asString();
            JsonPath jsonPath = new JsonPath(jsonStarShipResponseBody);
            System.out.println("The startship name is: " + jsonPath.get("starship_class"));
            foundStarship = starShip;
        }
        RestAssured.get(foundStarship).then().statusCode(200).body("starship_class", equalTo("Starfighter"));
    }

    private List<String> convertJsonArrayToCharacters(String json) throws ParseException {
        for (int i = 0; i < extractCharacters(json).size(); i++) {
            characters.add(extractCharacters(json).get(i).toString());
        }
        return characters;
    }

    private JSONArray extractCharacters(String json) throws ParseException {
        Object obj = parser.parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray results = (JSONArray) jsonObject.get("results");
        JSONArray characterJsonArray = null;
        for (int i = 0; i < results.size(); i++) {
            JSONObject result = (JSONObject) results.get(i);
            if (result.containsValue("A New Hope")) {
                characterJsonArray = (JSONArray) result.get("characters");
            }
        }
        return characterJsonArray;
    }

    private JSONArray extractStarships() throws ParseException {
        String json = RestAssured.get(foundCharacterURL).asString();
        Object obj = parser.parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray starShipsJsonArray = (JSONArray) jsonObject.get("starships");
        return starShipsJsonArray;
    }
}


