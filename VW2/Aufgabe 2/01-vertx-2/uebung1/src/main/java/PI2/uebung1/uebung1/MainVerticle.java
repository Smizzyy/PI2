package PI2.uebung1.uebung1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

  private final List<Pokemon> pokemonArray = new ArrayList<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var router = Router.router(vertx); // weil nur eine URL

    // CORS für die API-Route aktivieren
    router.route("/api/*").handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedHeader("Content-Type"));

    router.route("/images/pokemon/*").handler( // deklarieren eine Route
      StaticHandler.create("src/main/resources/pokemon") // path für die Bilder
    );
    router.get("/get").handler(req -> { // get-Methode
      String html = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Poke-Team Editor</title>" +
        "<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/@exampledev/new.css@1.1.2/new.min.css\">" +
        "</head><body><header><h1>Poke-Team Editor</h1>" +
        "<nav><a href=\"/get\">Pokemon hinzufügen</a> – <a href=\"/list\">Alle Pokemon anzeigen</a> – <a href=\"/api/list\">JSON API</a></nav>" +
        "</header><h2>Neues Pokemon hinzufügen</h2>" +
        "<form action=\"/add\" method=\"post\">" +
        "<label for=\"name\">Name:</label><br><input type=\"text\" id=\"name\" name=\"name\" required><br><br>" +
        "<label for=\"pokedexNummer\">Pokedex Nummer:</label><br><input type=\"number\" id=\"pokedexNummer\" name=\"pokedexNummer\" min=\"1\" max=\"151\" required><br><br>" +
        "<label for=\"level\">Level:</label><br><input type=\"number\" id=\"level\" name=\"level\" min=\"1\" max=\"99\" required><br><br>" +
        "<label for=\"status\">Status:</label><br><select id=\"status\" name=\"status\" required>" +
        "<option value=\"Healthy\">Healthy</option><option value=\"Burned\">Burned</option><option value=\"Frozen\">Frozen</option>" +
        "<option value=\"Paralyzed\">Paralyzed</option><option value=\"Poisoned\">Poisoned</option><option value=\"Asleep\">Asleep</option>" +
        "</select><br><br><button type=\"submit\">Speichern</button></form></body></html>";
      req.response() // gibt eine Antwort zurück
        .setStatusCode(200)
        .putHeader("content-type", "text/html") // typ: text und html
        .end(html);
    });

    // Route für die Liste der Pokémon
    router.get("/list").handler(req -> {
      StringBuilder html = new StringBuilder("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">" +
        "<title>Gespeicherte Pokémon</title></head><body><h2>Alle gespeicherten Pokemon</h2><table border=\"1\">" +
        "<tr><th>Image</th><th>Name</th><th>Pokedex Nummer</th><th>Level</th><th>Status</th></tr>");

      // Pokémon aus dem Array zur HTML-Tabelle hinzufügen
      pokemonArray.forEach(pokemon -> {String imageUrl = "/images/pokemon/" + String.format("%03d", pokemon.getNumber()) + ".png"; // Bildpfad auf Basis der Pokedex-Nummer
        html.append("<tr>")
        .append("<td><img src='").append(imageUrl).append("' alt='Pokemon Bild' width='50'></td>")
        .append("<td>").append(pokemon.getName()).append("</td>")
        .append("<td>").append(pokemon.getNumber()).append("</td>")
        .append("<td>").append(pokemon.getLevel()).append("</td>")
        .append("<td>").append(pokemon.getStatus()).append("</td>")
        .append("</tr>");
      });

      html.append("</table></body></html>");

      req.response().putHeader("content-type", "text/html").end(html.toString());
    });

    // POST-Route zum Hinzufügen eines neuen Pokémon
    router.post("/add").handler(BodyHandler.create()).handler(req -> {
      try {
        // Werte aus dem Formular lesen
        String name = req.request().getFormAttribute("name");
        int number = Integer.parseInt(req.request().getFormAttribute("pokedexNummer"));
        int level = Integer.parseInt(req.request().getFormAttribute("level"));
        String status = req.request().getFormAttribute("status");

        // Neues Pokémon zum Array hinzufügen
        Pokemon pokemon = new Pokemon(name, number, level, status);
        pokemonArray.add(pokemon);

        // Redirect zur Liste, um die hinzugefügten Pokémon zu sehen
        req.response().setStatusCode(303).putHeader("Location", "/list").end();
      } catch (Exception e) {
        // Fehlerbehandlung und Fehlermeldung im Server-Log
        e.printStackTrace();
        req.response().setStatusCode(400).end("Fehler beim Hinzufügen des Pokémon");
      }
    });

    // API-Route, die die Liste der Pokémon als JSON zurückgibt
    router.get("/api/list").handler(req -> {
      JsonArray jsonArray = new JsonArray();

      // Durchläuft jedes Pokemon-Objekt im Array und erstellt ein JSON-Objekt
      pokemonArray.forEach(pokemon -> {
        JsonObject jsonPokemon = new JsonObject()
          .put("name", pokemon.getName())
          .put("number", pokemon.getNumber())
          .put("level", pokemon.getLevel())
          .put("status", pokemon.getStatus());
        jsonArray.add(jsonPokemon); // Fügt das JSON-Objekt zum Array hinzu
      });

      // Setzt den JSON-Antwortheader und sendet das JSON-Array als Antwort
      req.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(jsonArray.encode());
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8888)
      .onComplete(http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8888");
        } else {
          startPromise.fail(http.cause());
        }
      });
  }
}

class Pokemon {
  private String name;
  private int number;
  private int level;
  private String status;

  public Pokemon(String name, int number, int level, String status) {
    this.name = name;
    this.number = number;
    this.level = level;
    this.status = status;
  }

  public String getName() { return name; }
  public int getNumber() { return number; }
  public int getLevel() { return level; }
  public String getStatus() { return status; }
}
