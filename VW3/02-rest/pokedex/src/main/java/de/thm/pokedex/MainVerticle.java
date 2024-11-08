package de.thm.pokedex;

import de.thm.pokedex.models.Pokemon;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  private List<Pokemon> pokedex;

  final int port = 8888;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //Lade das Pokedex mit Beispielwerten
    initPokedex();

    //Router
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    //Definiere die Routen
    //start
    router.get("/").handler(this::startRoute);

    //pokedex
    router.get("/pokedex").handler(this::getAllPokemon);
    router.get("/pokedex/:id").handler(this::getPokemonById);
    router.post("/pokedex").handler(this::createPokemon);
    router.delete("/pokedex/:id").handler(this::deletePokemon);
    router.patch("/pokedex/:id").handler(this::patchPokemon);


    //Start Server
    vertx.createHttpServer().requestHandler(router).listen(port).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port" + port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }


  private void startRoute(RoutingContext context) {
    context.response()
      .putHeader("content-type", "text/html")
      .end("<h1>Welcome to the Pokedex API</h1>");
  }

  // Aufgabe 2
  private void getAllPokemon(RoutingContext context) {
    // Überprüfen, ob der Typen-Filter gesetzt ist
    String typeFilter = null;

    if (!context.queryParam("type").isEmpty()) {
      typeFilter = context.queryParam("type").get(0);
    }

    List<Pokemon> filteredPokedex = new ArrayList<>(pokedex);
    // Falls ein 'type'-Filter gesetzt wurde, die Liste der Pokemon entsprechend filtern
    if (typeFilter != null) {
      final String finalTypeFilter = typeFilter.toLowerCase(); // Endgültige, unveränderliche Kopie erstellen und in Kleinbuchstaben umwandeln
      // Nur Pokemon behalten, deren Typ mit dem Filter übereinstimmt
      filteredPokedex = filteredPokedex.stream() // mit stream() kann man in der ArrayList filtern
        .filter(pokemon -> pokemon.getTypes().stream() // holt sich aus der Methode getTypes() die Strings und prüft, ob sie den richtigen Datentypen haben
          .anyMatch(type -> type.equalsIgnoreCase(finalTypeFilter))) // um auch Pokemon anzuzeigen, die einen zweiten Typ haben
        .toList(); // stream in die Liste speichern
    }

    // Pagination
    int offset;
    int limit;
    // Range-Werte festlegen:
    if (context.queryParam("offset").isEmpty()) { // wenn kein Query-Parameter "offset" vorhanden ist
      offset = 0; // Standardwert
    } else {
      offset = Integer.parseInt(context.queryParam("offset").get(0));
    }
    if (context.queryParam("limit").isEmpty()) { // wenn kein Query-Parameter "limit" vorhanden ist
      limit = 20; // Standardwert
    } else {
      limit = Integer.parseInt(context.queryParam("limit").get(0));
    }
    // Range setzen:
    // Start-Punkt
    int start = Math.min(offset, filteredPokedex.size());
    // End-Punkt
    int end = Math.min(start + limit, filteredPokedex.size());
    // Teilliste erstellen
    List<Pokemon> paginatedPokedex = filteredPokedex.subList(start, end);

    // Als Json-Array ausgeben
    JsonArray response = new JsonArray();
    paginatedPokedex.forEach(pokemon -> response.add(pokemon.toJson()));

    context.response()
      .putHeader("content-type", "application/json")
      .end(response.encodePrettily());
  }

  private void getPokemonById(RoutingContext context) {
    // Lese die ID aus der URL
    String pathId = context.pathParam("id");
    try {
      int id = Integer.parseInt(pathId);
      Pokemon pokemon = pokedex.stream()
        .filter(p -> p.getId() == id)
        .findFirst()
        .orElse(null);

      if (pokemon != null) {
        context.response()
          .putHeader("content-type", "application/json")
          .end(pokemon.toJson().encodePrettily());
      } else {
        context.response()
          .setStatusCode(404)
          .end(new JsonObject().put("message", "Pokemon not found").encodePrettily());
      }
    } catch (NumberFormatException e) {
      context.response()
        .setStatusCode(400)
        .end(new JsonObject().put("message", "Invalid ID format").encodePrettily());
    }
  }

  private void createPokemon(RoutingContext context) {
    try {
      JsonObject body = context.getBodyAsJson();
      Pokemon newPokemon = Pokemon.fromJson(body);

      if (pokedex.stream().anyMatch(p -> p.getId() == newPokemon.getId())) {
        context.response()
          .setStatusCode(409)
          .end(new JsonObject().put("message", "Pokemon with this ID already exists").encodePrettily());
        return;
      }

      pokedex.add(newPokemon);
      context.response()
        .setStatusCode(201)
        .putHeader("content-type", "application/json")
        .end(newPokemon.toJson().encodePrettily());
    } catch (Exception e) {
      context.response()
        .setStatusCode(400)
        .end(new JsonObject().put("message", "Invalid Pokemon data").encodePrettily());
    }
  }

  private void deletePokemon(RoutingContext context) {
    String pathId = context.pathParam("id");
    try {
      int id = Integer.parseInt(pathId);
      boolean removed = pokedex.removeIf(pokemon -> pokemon.getId() == id);

      if (removed) {
        context.response()
          .setStatusCode(204)
          .end();
      } else {
        context.response()
          .setStatusCode(404)
          .end(new JsonObject().put("message", "Pokemon not found").encodePrettily());
      }
    } catch (NumberFormatException e) {
      context.response()
        .setStatusCode(400)
        .end(new JsonObject().put("message", "Invalid ID format").encodePrettily());
    }
  }

  // Aufgabe 3
  private void patchPokemon(RoutingContext context) {
    String pathId = context.pathParam("id");
    try {
      int id = Integer.parseInt(pathId);
      // nach der richtigen ID filtern
      Pokemon patchedPokemon = pokedex.stream()
        .filter(pokemon -> pokemon.getId() == id)
        .findFirst()
        .orElse(null);

      if (patchedPokemon != null) {
        JsonObject body = context.getBodyAsJson();
        if (body.containsKey("name")) patchedPokemon.setName(body.getString("name"));
        if (body.containsKey("types")) patchedPokemon.setTypes(body.getJsonArray("types").getList());
        context.response()
          .setStatusCode(201) // 201 Created
          .putHeader("content-type", "application/json")
          .end(patchedPokemon.toJson().encodePrettily());
      } else {
        context.response().setStatusCode(404)
          .end(new JsonObject().put("message", "Pokemon not found").encodePrettily());
      }
    } catch (Exception e) {
      context.response()
        .setStatusCode(400)
        .end(new JsonObject().put("message", "Invalid ID format").encodePrettily());
    }
  }

  // Aufgabe 4
  /*
  1.)
  Endpunkt: GET /pokedex/{id}/abilities
  Anfrage-Body: Kein Body erforderlich, da die id des Pokemon im Pfadparameter übergeben wird.
  Antwort-Body: Gibt eine Liste der Abilities des Pokemon zurück.
                Jede Ability enthält die Felder name, attack_points, und effect_details.
  Statuscodes:
    200 OK: Fähigkeiten des Pokemon wurden erfolgreich abgerufen.
    404 Not Found: Ein Pokemon mit der angegebenen id existiert nicht.

  2.)
  Endpunkt: GET /pokedex/abilities?name={abilityName}
  Anfrage-Body: Kein Body erforderlich, da die Abfrage über den Query-Parameter name erfolgt.
  Antwort-Body: Gibt eine Liste von Pokemon zurück, die die angegebene Fähigkeit besitzen.
  Statuscodes:
    200 OK: Pokemon mit der angegebenen Fähigkeit wurden erfolgreich abgerufen.
    404 Not Found: Keine Pokemon mit der gesuchten Fähigkeit gefunden.
  */

  private void initPokedex() {
    pokedex = new ArrayList<>();

    pokedex.add(new Pokemon(1, "Bulbasaur", "Grass", "Poison"));
    pokedex.add(new Pokemon(2, "Ivysaur", "Grass", "Poison"));
    pokedex.add(new Pokemon(3, "Venusaur", "Grass", "Poison"));
    pokedex.add(new Pokemon(4, "Charmander", "Fire"));
    pokedex.add(new Pokemon(5, "Charmeleon", "Fire"));
    pokedex.add(new Pokemon(6, "Charizard", "Fire", "Flying"));
    pokedex.add(new Pokemon(7, "Squirtle", "Water"));
    pokedex.add(new Pokemon(8, "Wartortle", "Water"));
    pokedex.add(new Pokemon(9, "Blastoise", "Water"));
    pokedex.add(new Pokemon(10, "Caterpie", "Bug"));
    pokedex.add(new Pokemon(11, "Metapod", "Bug"));
    pokedex.add(new Pokemon(12, "Butterfree", "Bug", "Flying"));
    pokedex.add(new Pokemon(13, "Weedle", "Bug", "Poison"));
    pokedex.add(new Pokemon(14, "Kakuna", "Bug", "Poison"));
    pokedex.add(new Pokemon(15, "Beedrill", "Bug", "Poison"));
    pokedex.add(new Pokemon(16, "Pidgey", "Normal", "Flying"));
    pokedex.add(new Pokemon(17, "Pidgeotto", "Normal", "Flying"));
    pokedex.add(new Pokemon(18, "Pidgeot", "Normal", "Flying"));
    pokedex.add(new Pokemon(19, "Rattata", "Normal"));
    pokedex.add(new Pokemon(20, "Raticate", "Normal"));
    pokedex.add(new Pokemon(21, "Spearow", "Normal", "Flying"));
    pokedex.add(new Pokemon(22, "Fearow", "Normal", "Flying"));
    pokedex.add(new Pokemon(23, "Ekans", "Poison"));
    pokedex.add(new Pokemon(24, "Arbok", "Poison"));
    pokedex.add(new Pokemon(25, "Pikachu", "Electric"));
    pokedex.add(new Pokemon(26, "Raichu", "Electric"));
    pokedex.add(new Pokemon(27, "Sandshrew", "Ground"));
    pokedex.add(new Pokemon(28, "Sandslash", "Ground"));
    pokedex.add(new Pokemon(29, "Nidoran♀", "Poison"));
    pokedex.add(new Pokemon(30, "Nidorina", "Poison"));
    pokedex.add(new Pokemon(31, "Nidoqueen", "Poison", "Ground"));
    pokedex.add(new Pokemon(32, "Nidoran♂", "Poison"));
    pokedex.add(new Pokemon(33, "Nidorino", "Poison"));
    pokedex.add(new Pokemon(34, "Nidoking", "Poison", "Ground"));
    pokedex.add(new Pokemon(35, "Clefairy", "Fairy"));
    pokedex.add(new Pokemon(36, "Clefable", "Fairy"));
    pokedex.add(new Pokemon(37, "Vulpix", "Fire"));
    pokedex.add(new Pokemon(38, "Ninetales", "Fire"));
    pokedex.add(new Pokemon(39, "Jigglypuff", "Normal", "Fairy"));
    pokedex.add(new Pokemon(40, "Wigglytuff", "Normal", "Fairy"));
    pokedex.add(new Pokemon(41, "Zubat", "Poison", "Flying"));
    pokedex.add(new Pokemon(42, "Golbat", "Poison", "Flying"));
    pokedex.add(new Pokemon(43, "Oddish", "Grass", "Poison"));
    pokedex.add(new Pokemon(44, "Gloom", "Grass", "Poison"));
    pokedex.add(new Pokemon(45, "Vileplume", "Grass", "Poison"));
    pokedex.add(new Pokemon(46, "Paras", "Bug", "Grass"));
    pokedex.add(new Pokemon(47, "Parasect", "Bug", "Grass"));
    pokedex.add(new Pokemon(48, "Venonat", "Bug", "Poison"));
    pokedex.add(new Pokemon(49, "Venomoth", "Bug", "Poison"));
    pokedex.add(new Pokemon(50, "Diglett", "Ground"));
    pokedex.add(new Pokemon(51, "Dugtrio", "Ground"));
    pokedex.add(new Pokemon(52, "Meowth", "Normal"));
    pokedex.add(new Pokemon(53, "Persian", "Normal"));
    pokedex.add(new Pokemon(54, "Psyduck", "Water"));
    pokedex.add(new Pokemon(55, "Golduck", "Water"));
    pokedex.add(new Pokemon(56, "Mankey", "Fighting"));
    pokedex.add(new Pokemon(57, "Primeape", "Fighting"));
    pokedex.add(new Pokemon(58, "Growlithe", "Fire"));
    pokedex.add(new Pokemon(59, "Arcanine", "Fire"));
    pokedex.add(new Pokemon(60, "Poliwag", "Water"));
    pokedex.add(new Pokemon(61, "Poliwhirl", "Water"));
    pokedex.add(new Pokemon(62, "Poliwrath", "Water", "Fighting"));
    pokedex.add(new Pokemon(63, "Abra", "Psychic"));
    pokedex.add(new Pokemon(64, "Kadabra", "Psychic"));
    pokedex.add(new Pokemon(65, "Alakazam", "Psychic"));
    pokedex.add(new Pokemon(66, "Machop", "Fighting"));
    pokedex.add(new Pokemon(67, "Machoke", "Fighting"));
    pokedex.add(new Pokemon(68, "Machamp", "Fighting"));
    pokedex.add(new Pokemon(69, "Bellsprout", "Grass", "Poison"));
    pokedex.add(new Pokemon(70, "Weepinbell", "Grass", "Poison"));
    pokedex.add(new Pokemon(71, "Victreebel", "Grass", "Poison"));
    pokedex.add(new Pokemon(72, "Tentacool", "Water", "Poison"));
    pokedex.add(new Pokemon(73, "Tentacruel", "Water", "Poison"));
    pokedex.add(new Pokemon(74, "Geodude", "Rock", "Ground"));
    pokedex.add(new Pokemon(75, "Graveler", "Rock", "Ground"));
    pokedex.add(new Pokemon(76, "Golem", "Rock", "Ground"));
    pokedex.add(new Pokemon(77, "Ponyta", "Fire"));
    pokedex.add(new Pokemon(78, "Rapidash", "Fire"));
    pokedex.add(new Pokemon(79, "Slowpoke", "Water", "Psychic"));
    pokedex.add(new Pokemon(80, "Slowbro", "Water", "Psychic"));
    pokedex.add(new Pokemon(81, "Magnemite", "Electric", "Steel"));
    pokedex.add(new Pokemon(82, "Magneton", "Electric", "Steel"));
    pokedex.add(new Pokemon(83, "Farfetch'd", "Normal", "Flying"));
    pokedex.add(new Pokemon(84, "Doduo", "Normal", "Flying"));
    pokedex.add(new Pokemon(85, "Dodrio", "Normal", "Flying"));
    pokedex.add(new Pokemon(86, "Seel", "Water"));
    pokedex.add(new Pokemon(87, "Dewgong", "Water", "Ice"));
    pokedex.add(new Pokemon(88, "Grimer", "Poison"));
    pokedex.add(new Pokemon(89, "Muk", "Poison"));
    pokedex.add(new Pokemon(90, "Shellder", "Water"));
    pokedex.add(new Pokemon(91, "Cloyster", "Water", "Ice"));
    pokedex.add(new Pokemon(92, "Gastly", "Ghost", "Poison"));
    pokedex.add(new Pokemon(93, "Haunter", "Ghost", "Poison"));
    pokedex.add(new Pokemon(94, "Gengar", "Ghost", "Poison"));
    pokedex.add(new Pokemon(95, "Onix", "Rock", "Ground"));
    pokedex.add(new Pokemon(96, "Drowzee", "Psychic"));
    pokedex.add(new Pokemon(97, "Hypno", "Psychic"));
    pokedex.add(new Pokemon(98, "Krabby", "Water"));
    pokedex.add(new Pokemon(99, "Kingler", "Water"));
    pokedex.add(new Pokemon(100, "Voltorb", "Electric"));
    pokedex.add(new Pokemon(101, "Electrode", "Electric"));
    pokedex.add(new Pokemon(102, "Exeggcute", "Grass", "Psychic"));
    pokedex.add(new Pokemon(103, "Exeggutor", "Grass", "Psychic"));
    pokedex.add(new Pokemon(104, "Cubone", "Ground"));
    pokedex.add(new Pokemon(105, "Marowak", "Ground"));
    pokedex.add(new Pokemon(106, "Hitmonlee", "Fighting"));
    pokedex.add(new Pokemon(107, "Hitmonchan", "Fighting"));
    pokedex.add(new Pokemon(108, "Lickitung", "Normal"));
    pokedex.add(new Pokemon(109, "Koffing", "Poison"));
    pokedex.add(new Pokemon(110, "Weezing", "Poison"));
    pokedex.add(new Pokemon(111, "Rhyhorn", "Ground", "Rock"));
    pokedex.add(new Pokemon(112, "Rhydon", "Ground", "Rock"));
    pokedex.add(new Pokemon(113, "Chansey", "Normal"));
    pokedex.add(new Pokemon(114, "Tangela", "Grass"));
    pokedex.add(new Pokemon(115, "Kangaskhan", "Normal"));
    pokedex.add(new Pokemon(116, "Horsea", "Water"));
    pokedex.add(new Pokemon(117, "Seadra", "Water"));
    pokedex.add(new Pokemon(118, "Goldeen", "Water"));
    pokedex.add(new Pokemon(119, "Seaking", "Water"));
    pokedex.add(new Pokemon(120, "Staryu", "Water"));
    pokedex.add(new Pokemon(121, "Starmie", "Water", "Psychic"));
    pokedex.add(new Pokemon(122, "Mr. Mime", "Psychic", "Fairy"));
    pokedex.add(new Pokemon(123, "Scyther", "Bug", "Flying"));
    pokedex.add(new Pokemon(124, "Jynx", "Ice", "Psychic"));
    pokedex.add(new Pokemon(125, "Electabuzz", "Electric"));
    pokedex.add(new Pokemon(126, "Magmar", "Fire"));
    pokedex.add(new Pokemon(127, "Pinsir", "Bug"));
    pokedex.add(new Pokemon(128, "Tauros", "Normal"));
    pokedex.add(new Pokemon(129, "Magikarp", "Water"));
    pokedex.add(new Pokemon(130, "Gyarados", "Water", "Flying"));
    pokedex.add(new Pokemon(131, "Lapras", "Water", "Ice"));
    pokedex.add(new Pokemon(132, "Ditto", "Normal"));
    pokedex.add(new Pokemon(133, "Eevee", "Normal"));
    pokedex.add(new Pokemon(134, "Vaporeon", "Water"));
    pokedex.add(new Pokemon(135, "Jolteon", "Electric"));
    pokedex.add(new Pokemon(136, "Flareon", "Fire"));
    pokedex.add(new Pokemon(137, "Porygon", "Normal"));
    pokedex.add(new Pokemon(138, "Omanyte", "Rock", "Water"));
    pokedex.add(new Pokemon(139, "Omastar", "Rock", "Water"));
    pokedex.add(new Pokemon(140, "Kabuto", "Rock", "Water"));
    pokedex.add(new Pokemon(141, "Kabutops", "Rock", "Water"));
    pokedex.add(new Pokemon(142, "Aerodactyl", "Rock", "Flying"));
    pokedex.add(new Pokemon(143, "Snorlax", "Normal"));
    pokedex.add(new Pokemon(144, "Articuno", "Ice", "Flying"));
    pokedex.add(new Pokemon(145, "Zapdos", "Electric", "Flying"));
    pokedex.add(new Pokemon(146, "Moltres", "Fire", "Flying"));
    pokedex.add(new Pokemon(147, "Dratini", "Dragon"));
    pokedex.add(new Pokemon(148, "Dragonair", "Dragon"));
    pokedex.add(new Pokemon(149, "Dragonite", "Dragon", "Flying"));
    pokedex.add(new Pokemon(150, "Mewtwo", "Psychic"));
    pokedex.add(new Pokemon(151, "Mew", "Psychic"));
    pokedex.add(new Pokemon(152, "Chikorita", "Grass"));
    pokedex.add(new Pokemon(153, "Bayleef", "Grass"));
    pokedex.add(new Pokemon(154, "Meganium", "Grass"));
    pokedex.add(new Pokemon(155, "Cyndaquil", "Fire"));
    pokedex.add(new Pokemon(156, "Quilava", "Fire"));
    pokedex.add(new Pokemon(157, "Typhlosion", "Fire"));
    pokedex.add(new Pokemon(158, "Totodile", "Water"));
    pokedex.add(new Pokemon(159, "Croconaw", "Water"));
    pokedex.add(new Pokemon(160, "Feraligatr", "Water"));
    pokedex.add(new Pokemon(161, "Sentret", "Normal"));
    pokedex.add(new Pokemon(162, "Furret", "Normal"));
    pokedex.add(new Pokemon(163, "Hoothoot", "Normal", "Flying"));
    pokedex.add(new Pokemon(164, "Noctowl", "Normal", "Flying"));
    pokedex.add(new Pokemon(165, "Ledyba", "Bug", "Flying"));
    pokedex.add(new Pokemon(166, "Ledian", "Bug", "Flying"));
    pokedex.add(new Pokemon(167, "Spinarak", "Bug", "Poison"));
    pokedex.add(new Pokemon(168, "Ariados", "Bug", "Poison"));
    pokedex.add(new Pokemon(169, "Crobat", "Poison", "Flying"));
    pokedex.add(new Pokemon(170, "Chinchou", "Water", "Electric"));
    pokedex.add(new Pokemon(171, "Lanturn", "Water", "Electric"));
    pokedex.add(new Pokemon(172, "Pichu", "Electric"));
    pokedex.add(new Pokemon(173, "Cleffa", "Fairy"));
    pokedex.add(new Pokemon(174, "Igglybuff", "Normal", "Fairy"));
    pokedex.add(new Pokemon(175, "Togepi", "Fairy"));
    pokedex.add(new Pokemon(176, "Togetic", "Fairy", "Flying"));
    pokedex.add(new Pokemon(177, "Natu", "Psychic", "Flying"));
    pokedex.add(new Pokemon(178, "Xatu", "Psychic", "Flying"));
    pokedex.add(new Pokemon(179, "Mareep", "Electric"));
    pokedex.add(new Pokemon(180, "Flaaffy", "Electric"));
    pokedex.add(new Pokemon(181, "Ampharos", "Electric"));
    pokedex.add(new Pokemon(182, "Bellossom", "Grass"));
    pokedex.add(new Pokemon(183, "Marill", "Water", "Fairy"));
    pokedex.add(new Pokemon(184, "Azumarill", "Water", "Fairy"));
    pokedex.add(new Pokemon(185, "Sudowoodo", "Rock"));
    pokedex.add(new Pokemon(186, "Politoed", "Water"));
    pokedex.add(new Pokemon(187, "Hoppip", "Grass", "Flying"));
    pokedex.add(new Pokemon(188, "Skiploom", "Grass", "Flying"));
    pokedex.add(new Pokemon(189, "Jumpluff", "Grass", "Flying"));
    pokedex.add(new Pokemon(190, "Aipom", "Normal"));
    pokedex.add(new Pokemon(191, "Sunkern", "Grass"));
    pokedex.add(new Pokemon(192, "Sunflora", "Grass"));
    pokedex.add(new Pokemon(193, "Yanma", "Bug", "Flying"));
    pokedex.add(new Pokemon(194, "Wooper", "Water", "Ground"));
    pokedex.add(new Pokemon(195, "Quagsire", "Water", "Ground"));
    pokedex.add(new Pokemon(196, "Espeon", "Psychic"));
    pokedex.add(new Pokemon(197, "Umbreon", "Dark"));
    pokedex.add(new Pokemon(198, "Murkrow", "Dark", "Flying"));
    pokedex.add(new Pokemon(199, "Slowking", "Water", "Psychic"));
    pokedex.add(new Pokemon(200, "Misdreavus", "Ghost"));
  }
}
