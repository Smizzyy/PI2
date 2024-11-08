package de.thm.pokedex.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

public class Pokemon {
  private final int id;
  private String name;
  private List<String> types;

  public Pokemon(int id, String name, String... types) {
    this.id = id;
    this.name = name;
    this.types = Arrays.asList(types);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getTypes() {
    return types;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("id", this.id);
    json.put("name", this.name);
    json.put("types", new JsonArray(this.types));
    return json;
  }


  public static Pokemon fromJson(JsonObject json) {
    int id = json.getInteger("id");
    String name = json.getString("name");
    List<String> types = json.getJsonArray("types").getList();
    return new Pokemon(id, name, types.toArray(new String[0]));
  }

}
