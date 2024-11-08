package praxisblock._01.pokeitems;

import io.vertx.core.json.JsonObject;

enum ItemType {
  Pokeball,
  HealingItem,
  StatusCure
}

class PokeItems {
  String name;
  ItemType type;
  String description;
  int quantity;

  public PokeItems(String name, ItemType type, String description, int quantity) {
    this.name = name;
    this.type = type;
    this.description = description;

  }
  public JsonObject toJSON() {
    return  new JsonObject()
      .put("name", name)
      .put("type", type)
      .put("description", description)
      .put("quantity", quantity);
  }

  public static PokeItems fromJSON(JsonObject json) {
    return new PokeItems(
      json.getString("name"),
      ItemType.valueOf(json.getString("type")),
      json.getString("description"),
      json.getInteger("quantity")
    );
  }
}
