package PI2.uebung1.uebung1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var router = Router.router(vertx); // weil nur eine URL
    router.route("/images/*").handler( // deklarieren eine Route für images: http://localhost:8888/images/035.png
      StaticHandler.create("src/main/resources") // path fürs Bild
    );
    router.get("/piepi").handler(req -> { // get-Methode die eine JSON-Datei aufruft
      req.response() // gibt eine Antwort zurück
        .setStatusCode(200)
        .putHeader("content-type", "text/html") // typ: text und html
        .end("<html><body><h1>Hello Pi2!</h1><img src='images/035.png' alt='Piepi'></body></html>");
    }); // erstellt eine HTML-Seite mit Text und Bild: http://localhost:8888/piepi

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
