package hausaufgabe.hausaufgabe1;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class Trainer {
  String name;
  int age;
  String birthplace;
  ArrayList<Pokemon> pokemonTeam = new ArrayList<>();

  public Trainer(String name, int age, String birthplace) {
    this.name = name;
    this.age = age;
    this.birthplace = birthplace;
  }

  public JsonObject toJSON() {
    return new JsonObject()
      .put("name", name)
      .put("age", age)
      .put("birthplace", birthplace);
  }
}
