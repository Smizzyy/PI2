package hausaufgabe.hausaufgabe1;

import io.vertx.core.json.JsonObject;

public class Pokemon {
  String name;
  int level;
  String type;

  public Pokemon (String name, int level, String type) {
    this.name = name;
    this.level = level;
    this.type = type;
  }

  public JsonObject toJSON() {
    return new JsonObject()
      .put("name", name)
      .put("level", level)
      .put("type", type);
  }
}
