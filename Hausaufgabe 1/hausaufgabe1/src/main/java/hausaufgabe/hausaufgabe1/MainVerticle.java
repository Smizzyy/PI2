package hausaufgabe.hausaufgabe1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.ArrayList;
import java.util.UUID;

public class MainVerticle extends AbstractVerticle {
  ArrayList<Trainer> trainers = new ArrayList<>();

  // Ash
  Trainer ash = new Trainer("Ash", 10, "Alabastia");
  Pokemon pikachu = new Pokemon("Pikachu", 5, "Elektro");

  // Gary
  Trainer gary = new Trainer("Gary", 10, "Alabastia");
  Pokemon schiggy = new Pokemon("Shiggy", 6, "Wasser");

  // Session
  private String sessionId = null; // Globale Session-ID
  private Cookie sessionCookie; // Globaler Session-Cookie
  private String ashUser = "Ash"; // Username für Login von Ash
  private String ashPassword = "Papr1KA"; // Passwort für Login von Ash

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ash.pokemonTeam.add(pikachu);
    gary.pokemonTeam.add(schiggy);
    trainers.add(ash);
    trainers.add(gary);

    // CORS (Aufgabe 3)
    CorsHandler corsHandler = CorsHandler.create("*") // "*" für alle Ursprünge oder spezifische Domain
      .allowedMethod(HttpMethod.GET) // Erlaube nur GET-Anfragen für CORS
      .allowedHeader("content-type"); // Erlaubte Header für CORS

    var router = Router.router(vertx);
    // Routen
    router.route().handler(corsHandler); // Route für den CORS-Handler
    router.route().handler(BodyHandler.create()); // JSON-Daten im Anfragekörper verwenden und darauf zugreifen
    router.get("/trainers").handler(this::listTrainers); // Trainer ausgeben
    router.get("/trainers/:id").handler(this::getTrainerById); // spezifischen Trainer ausgeben
    router.get("/trainers/:id/pokemon").handler(this::getPokemonTeamFromTrainer); // das Team von einem Trainer ausgeben
    router.get("/trainers/:id/pokemon/:pokemonId").handler(this::getPokemonIdFromTrainer); // spezifisches Pokemon ausgeben
    router.post("/trainers/:id/pokemon").handler(this::addNewPokemon); // neues Pokemon hinzufügen
    router.put("/trainers/:id/pokemon/:pokemonId").handler(this::replaceDataByPokemonId); // alle Details eines Pokemons aktualisieren
    router.patch("/trainers/:id").handler(this::refreshTrainerDataById); // einzelne/alle Daten vom Trainer ändern
    router.delete("/trainers/:id/pokemon/:pokemonId").handler(this::deletePokemonById); // Pokemon löschen
    router.post("/login").handler(this::login); // Ash einloggen
    router.post("/logout").handler(this::logout); // Ash ausloggen

    vertx.createHttpServer().requestHandler(router)
      .listen(3000).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 3000");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  // Aufgabe 1
  // GET /trainers
  private void listTrainers(RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();
    for (Trainer tr : trainers) {
      jsonArray.add(tr.toJSON());
    }
    ctx.response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json")
      .end(jsonArray.toString());
  }

  // GET /trainers/{id}
  private void getTrainerById(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    try {
      int index = Integer.parseInt(pathId);
      if (index >= 0 && trainers.size() > index) {
        // Valid request
        var json = trainers.get(index).toJSON().toString();
        ctx.response().setStatusCode(200).putHeader("content-type", "application/json").end(json);
      } else {
        ctx.response().setStatusCode(404).end("Not found: Trainer does not exist");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request: invalid id");
    }
  }

  // GET /trainers/{id}/pokemon
  private void getPokemonTeamFromTrainer(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    JsonArray jsonArray = new JsonArray();
    try {
      int index = Integer.parseInt(pathId);
      if (index >= 0 && trainers.size() > index) {
        // Valid request
        for (Pokemon p : trainers.get(index).pokemonTeam) {
          jsonArray.add(p.toJSON());
        }
        ctx.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(jsonArray.toString());
      } else {
        ctx.response().setStatusCode(404).end("Not found: Trainer or Pokemon does not exist");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request: invalid id or Pokemon-Team");
    }
  }

  // GET /trainers/{id}/pokemon/{pokemonId}
  private void getPokemonIdFromTrainer(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    String pokemonPathId = ctx.pathParam("pokemonId");
    try {
      int index = Integer.parseInt(pathId);
      int pokemonIndex = Integer.parseInt(pokemonPathId);
      if (index >= 0 && trainers.size() > index) {
        if (pokemonIndex >= 0 && trainers.get(index).pokemonTeam.size() > pokemonIndex) {
          var json = trainers.get(index).pokemonTeam.get(pokemonIndex).toJSON().toString();
          ctx.response().setStatusCode(200).putHeader("content-type", "application/json").end(json);
        } else {
          ctx.response().setStatusCode(404).end("Not found: Pokemon does not exist");
        }
      } else {
        ctx.response().setStatusCode(404).end("Not found: TrainerId does not exist");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request: invalid id");
    }
  }

  // POST /trainers/{id}/pokemon
  private void addNewPokemon(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    Cookie cookie = ctx.getCookie("session_id"); // Beim Ausführen in HTTPie im Header übergeben
    try {
      // Session-ID prüfen
      if (cookie != null && sessionId != null && sessionId.equals(cookie.getValue())) {
        int index = Integer.parseInt(pathId);
        if (index >= 0 && index < trainers.size()) {
          // JSON-Daten für das neue Pokémon aus der Anfrage lesen
          JsonObject body = ctx.getBodyAsJson();
          String name = body.getString("name");
          int level = body.getInteger("level");
          String type = body.getString("type");
          Pokemon newPokemon = new Pokemon(name, level, type);
          if (body.containsKey("name")) {
            newPokemon.name = name;
          }
          if (body.containsKey("level")) {
            newPokemon.level = level;
          }
          if (body.containsKey("type")) {
            newPokemon.type = type;
          }

          // Neues Pokémon erstellen und hinzufügen
          trainers.get(index).pokemonTeam.add(newPokemon);

          ctx.response()
            .setStatusCode(201) // 201 Created
            .putHeader("content-type", "application/json")
            .end(newPokemon.toJSON().toString());
        } else {
          ctx.response().setStatusCode(404).end("Trainer not found");
        }
      } else {
        // Wenn keine gültige Session vorhanden ist
        ctx.response()
          .setStatusCode(401) // 401 Unauthorized
          .putHeader("content-type", "text/plain")
          .end("No valid session id");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request data");
    }
  }

  // PUT /trainers/{id}/pokemon/{pokemonId}
  private void replaceDataByPokemonId(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    String pokemonPathId = ctx.pathParam("pokemonId");
    Cookie cookie = ctx.getCookie("session_id"); // Beim Ausführen in HTTPie im Header übergeben
    try {
      // Session-ID prüfen
      if (cookie != null && sessionId != null && sessionId.equals(cookie.getValue())) {
        int index = Integer.parseInt(pathId);
        int pokemonIndex = Integer.parseInt(pokemonPathId);
        if (index >= 0 && index < trainers.size()) {
          if (pokemonIndex >= 0 && trainers.get(index).pokemonTeam.size() > pokemonIndex) {
            // JSON-Daten für das neue Pokémon aus der Anfrage lesen
            JsonObject body = ctx.getBodyAsJson();
            String name = body.getString("name");
            int level = body.getInteger("level");
            String type = body.getString("type");
            Pokemon newPokemon = new Pokemon(name, level, type);
            if (body.containsKey("name")) {
              newPokemon.name = name;
            }
            if (body.containsKey("level")) {
              newPokemon.level = level;
            }
            if (body.containsKey("type")) {
              newPokemon.type = type;
            }

            // Neues Pokémon erstellen und ersetzen
            trainers.get(index).pokemonTeam.set(pokemonIndex, newPokemon);

            ctx.response()
              .setStatusCode(201) // 201 Created
              .putHeader("content-type", "application/json")
              .end(newPokemon.toJSON().toString());
          } else {
            ctx.response().setStatusCode(404).end("Pokemon not found");
          }
        }
      } else {
        // Wenn keine gültige Session vorhanden ist
        ctx.response()
          .setStatusCode(401) // 401 Unauthorized
          .putHeader("content-type", "text/plain")
          .end("No valid session id");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request data");
    }
  }

  // PATCH /trainers/{id}
  private void refreshTrainerDataById(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    Cookie cookie = ctx.getCookie("session_id"); // Beim Ausführen in HTTPie im Header übergeben
    try {
      // Session-ID prüfen
      if (cookie != null && sessionId != null && sessionId.equals(cookie.getValue())) {
        int index = Integer.parseInt(pathId);
        if (index >= 0 && index < trainers.size()) {
          // auf bisherigen Trainer zugreifen
          Trainer patchedTrainer = trainers.get(index);
          // JSON-Daten für den neuen Trainer aus der Anfrage lesen
          JsonObject body = ctx.getBodyAsJson();
          if (body.containsKey("name")) {
            patchedTrainer.name = body.getString("name");
          }
          if (body.containsKey("age")) {
            patchedTrainer.age = body.getInteger("age");
          }
          if (body.containsKey("birthplace")) {
            patchedTrainer.birthplace = body.getString("birthplace");
          }
          ctx.response()
            .setStatusCode(201) // 201 Created
            .putHeader("content-type", "application/json")
            .end(patchedTrainer.toJSON().toString());
        } else {
          ctx.response().setStatusCode(404).end("Trainer not found");
        }
      } else {
        // Wenn keine gültige Session vorhanden ist
        ctx.response()
          .setStatusCode(401) // 401 Unauthorized
          .putHeader("content-type", "text/plain")
          .end("No valid session id");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request data");
    }
  }

  // DELETE /trainers/{id}/pokemon/{pokemonId}
  private void deletePokemonById(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    String pokemonPathId = ctx.pathParam("pokemonId");
    Cookie cookie = ctx.getCookie("session_id"); // Beim Ausführen in HTTPie im Header übergeben
    try {
      // Session-ID prüfen
      if (cookie != null && sessionId != null && sessionId.equals(cookie.getValue())) {
        int index = Integer.parseInt(pathId);
        int pokemonIndex = Integer.parseInt(pokemonPathId);
        if (index >= 0 && index < trainers.size()) {
          if (pokemonIndex >= 0 && trainers.get(index).pokemonTeam.size() > pokemonIndex) {
            var json = trainers.get(index).pokemonTeam.remove(pokemonIndex).toJSON().toString();
            ctx.response().setStatusCode(200).putHeader("content-type", "application/json").end(json);
          } else {
            ctx.response().setStatusCode(404).end("Pokemon not found not found");
          }
        } else {
          ctx.response().setStatusCode(404).end("Trainer not found");
        }
      } else {
        // Wenn keine gültige Session vorhanden ist
        ctx.response()
          .setStatusCode(401) // 401 Unauthorized
          .putHeader("content-type", "text/plain")
          .end("No valid session id");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request data");
    }
  }

  // Aufgabe 2
  // POST /login
  private void login(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    String user = body.getString("user");
    String password = body.getString("password");
    // auf user und password prüfen
    if (ashUser.equals(user) && ashPassword.equals(password)) {
      if (sessionId == null) {
        sessionId = UUID.randomUUID().toString(); // generiert eine zufällige Session-ID
        // Cookie mit der Session-ID setzen
        sessionCookie = Cookie.cookie("session_id", sessionId)
          .setPath("/") // Cookie gilt für die ganze Domain
          .setHttpOnly(true) // nur über Http und nicht über Scripte
          .setMaxAge(3600); // 1 Stunde
        ctx.response()
          .addCookie(sessionCookie)
          .setStatusCode(201) // 201 Created
          .putHeader("content-type", "text/plain")
          .end("Session cookie has been set with value: " + sessionId);
      } else {
        ctx.response()
          .setStatusCode(409) // 409 Conflict
          .putHeader("content-type", "text/plain")
          .end("Already logged in. Session cookie: " + sessionId);
      }
    } else {
      ctx.response()
        .setStatusCode(401) // 401 Unauthorized
        .putHeader("content-type", "text/plain")
        .end("Unauthorized Login");
    }
  }

  // POST /logout
  private void logout(RoutingContext ctx) {
    JsonObject body = ctx.getBodyAsJson();
    String user = body.getString("user");

    // Überprüfen, ob der Benutzer Ash ist
    if ("Ash".equals(user)) {
      // Versuche, das Cookie zu holen
      Cookie cookie = ctx.getCookie("session_id");

      if (cookie != null) {
        // Überprüfen, ob eine gültige Session existiert und ob die Session-ID übereinstimmt
        if (sessionId != null && sessionId.equals(cookie.getValue())) {
          sessionId = null; // Session-ID zurücksetzen, um den Benutzer abzumelden

          // Cookie mit abgelaufenem Datum setzen, um es zu löschen
          Cookie expiredCookie = Cookie.cookie("session_id", "")
            .setPath("/")
            .setMaxAge(0); // Setzt das Cookie auf abgelaufen
          ctx.response()
            .addCookie(expiredCookie)
            .setStatusCode(200)
            .putHeader("content-type", "text/plain")
            .end("User Ash has been logged out, and session cookie deleted.");
            // Wichtig: Beim Header in HTTPie den Cookie mit dem Namen und Session-ID übergeben
        } else {
          // Wenn keine gültige Session vorhanden ist oder die IDs nicht übereinstimmen
          ctx.response()
            .setStatusCode(401) // 401 Unauthorized
            .putHeader("content-type", "text/plain")
            .end("Unauthorized logout attempt.");
        }
      } else {
        // Wenn das Cookie nicht vorhanden ist
        ctx.response()
          .setStatusCode(400) // Bad Request
          .putHeader("content-type", "text/plain")
          .end("No session cookie found.");
      }
    } else {
      // Falls der Benutzer nicht Ash ist
      ctx.response()
        .setStatusCode(403) // 403 Forbidden
        .putHeader("content-type", "text/plain")
        .end("User not allowed to perform this action.");
    }
  }
}
