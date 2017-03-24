package info.usmans.blog.vertx;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import info.usmans.blog.model.BlogItem;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up vert.x routes and start the server.
 *
 * @author Usman Saleem
 */
public class ServerVerticle extends AbstractVerticle {
    public static final int ITEMS_PER_PAGE = 10;
    private List<BlogItem> blogItems;
    private Gson gson = new Gson();
    private int totalPages;
    private int itemsOnLastPage;

    private HttpClient client;

    @Override
    public void start() {
        HttpClientOptions options = new HttpClientOptions().setDefaultHost("raw.githubusercontent.com")
                .setDefaultPort(443).setSsl(true).setLogActivity(true);
        client = vertx.createHttpClient(options);

        client.getNow("/usmansaleem/vert.x.blog/master/src/main/resources/data.json", response -> response.bodyHandler(totalBuffer -> {
            if (response.statusCode() == 200) {
                updateData(totalBuffer);
                Router router = createRoutes();

                vertx.createHttpServer().requestHandler(router::accept).
                        listen(Integer.getInteger("http.port"), System.getProperty("http.address"));
            } else {
                throw new RuntimeException("Invalid Status code while reading data." + response.statusCode() + ", " + totalBuffer.toString());
            }

        }));
    }

    private Router createRoutes() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/rest/blog/refresh").handler(this::refreshData);
        router.get("/rest/blog/highestPage").handler(this::handleGetHighestPage);
        router.get("/rest/blog/listCategories").handler(this::handleGetListCategories);
        router.get("/rest/blog/blogCount").handler(this::handleGetBlogCount);
        router.get("/rest/blog/blogItems/:pageNumber").handler(this::handleGetBlogItemsMainCategoryByPageNumber);
        router.route("/*").handler(StaticHandler.create());
        return router;
    }

    private void refreshData(RoutingContext ignore) {
        client.getNow("/usmansaleem/vert.x.blog/master/src/main/resources/data.json", response -> response.bodyHandler(totalBuffer -> {
            if (response.statusCode() == 200) {
                updateData(totalBuffer);
            }
        }));
    }

    private void updateData(Buffer totalBuffer) {
        blogItems = gson.fromJson(totalBuffer.toString(), new TypeToken<ArrayList<BlogItem>>() {
        }.getType());

        totalPages = blogItems.size() / ITEMS_PER_PAGE;
        itemsOnLastPage = blogItems.size() % ITEMS_PER_PAGE;
        if (itemsOnLastPage != 0) {
            totalPages++;
        }
    }

    private void handleGetHighestPage(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "text/plain").end(String.valueOf(totalPages));
    }

    private void handleGetListCategories(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json").end("[{\"id\":1,\"name\":\"Java\"},{\"id\":2,\"name\":\"PostgreSQL\"},{\"id\":3,\"name\":\"Linux\"},{\"id\":4,\"name\":\"IT\"},{\"id\":5,\"name\":\"General\"},{\"id\":6,\"name\":\"JBoss\"}]");
    }

    private void handleGetBlogCount(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "text/plain").end(String.valueOf(blogItems.size()));
    }

    private void handleGetBlogItemsMainCategoryByPageNumber(RoutingContext routingContext) {
        String pageNumberParam = routingContext.request().getParam("pageNumber");
        HttpServerResponse response = routingContext.response();
        if (pageNumberParam == null) {
            sendBadRequestError(response, "Bad Request - Invalid Page Number");
        } else {
            try {
                int pageNumber = Integer.parseInt(pageNumberParam);
                if (pageNumber >= 1 && pageNumber <= totalPages) {
                    int endIdx = pageNumber * ITEMS_PER_PAGE;
                    int startIdx = endIdx - ITEMS_PER_PAGE;

                    if (pageNumber == this.totalPages && itemsOnLastPage != 0) {
                        endIdx = startIdx + itemsOnLastPage;
                    }

                    response.putHeader("content-type", "application/json").end(gson.toJson(blogItems.subList(startIdx, endIdx)));
                } else {
                    sendBadRequestError(response, "Bad Request - Invalid Page Number");
                }
            } catch (NumberFormatException e) {
                sendBadRequestError(response, "Bad Request - Invalid Page Number");
            }
        }
    }

    private void sendBadRequestError(HttpServerResponse response, String message) {
        response.setStatusCode(400).end(message);
    }


}
