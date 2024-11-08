package praxisblock._01.pokeitems;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;

public class MainVerticle extends AbstractVerticle {

  private ArrayList<PokeItems> items = new ArrayList<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    items.add(new PokeItems("Superball", ItemType.Pokeball, "Ein Superball", 11));
    items.add(new PokeItems("Hyperball", ItemType.Pokeball, "Ein Hyperball", 24));
    var router = Router.router(vertx);
    router.get("/items").handler(this::listItems);
    router.get("/items/:id").handler(this::handleItemDetails);
    router.delete("/items/:id").handler(this::handleDeleteItem);

    vertx.createHttpServer().requestHandler(router)
      .listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void handleDeleteItem(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    try {
      int index = Integer.parseInt(pathId);
      if(index >= 0 && items.size() > index) {
        // Valid request
        items.remove(items.get(index));
        ctx.response().setStatusCode(200).end();
      } else {
        ctx.response().setStatusCode(404).end("Not found: Item does not exist");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request: invalid id");
    }
  }

  private void handleItemDetails(RoutingContext ctx) {
    String pathId = ctx.pathParam("id");
    try {
      int index = Integer.parseInt(pathId);
      if(index >= 0 && items.size() > index) {
        // Valid request
        var json = items.get(index).toJSON().toString();
        ctx.response().setStatusCode(200).end(json);
      } else {
        ctx.response().setStatusCode(404).end("Not found: Item does not exist");
      }
    } catch (Exception e) {
      ctx.response().setStatusCode(400).end("Invalid request: invalid id");
    }
  }

  private void listItems(RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();
    // items in Json-Array hinzufügen
    for (PokeItems pi : items) {
      jsonArray.add(pi.toJSON());
    }

    ctx.response()
      .setStatusCode(200)
      .putHeader("content.type", "application/json")
      .end(jsonArray.toString());
  }
}
