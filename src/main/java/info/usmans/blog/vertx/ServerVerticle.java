package info.usmans.blog.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Sets up vert.x routes and start the server.
 *
 * @author Usman Saleem
 */
public class ServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/rest/blog/listCategories").handler(this::handleGetListCategories);

        vertx.createHttpServer().requestHandler(router::accept).
                listen(Integer.getInteger("http.port"), System.getProperty("http.address"));
    }

    private void handleGetListCategories(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json").end("[{\"id\":1,\"name\":\"Java\"},{\"id\":2,\"name\":\"PostgreSQL\"},{\"id\":3,\"name\":\"Linux\"},{\"id\":4,\"name\":\"IT\"},{\"id\":5,\"name\":\"General\"},{\"id\":6,\"name\":\"JBoss\"}]");
    }
}
