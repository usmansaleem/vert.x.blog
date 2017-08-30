package info.usmans.blog.vertx;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import info.usmans.blog.model.BlogItem;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sets up vert.x routes and start the server.
 *
 * @author Usman Saleem
 */
public class ServerVerticle extends AbstractVerticle {
    private static final int ITEMS_PER_PAGE = 10;
    private static final String CATEGORIES_JSON = "[{\"id\":1,\"name\":\"Java\"}," +
            "{\"id\":2,\"name\":\"PostgreSQL\"},{\"id\":3,\"name\":\"Linux\"}," +
            "{\"id\":4,\"name\":\"IT\"},{\"id\":5,\"name\":\"General\"},{\"id\":6,\"name\":\"JBoss\"}]";

    private static final Gson gson = new Gson();

    private List<BlogItem> blogItems;
    private int totalPages;
    private int itemsOnLastPage;

    @Override
    public void start() {
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/data.json"))) {
            initData(reader);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            System.err.println("Error reading resources/data.json. Exiting ...");
            vertx.close();
            System.exit(-1);
        }

        Router router = createRoutes();
        vertx.createHttpServer().requestHandler(router::accept).
                listen(8080, "0.0.0.0");

    }

    private Router createRoutes() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/rest/blog/highestPage").handler(this::handleGetHighestPage);
        router.get("/rest/blog/listCategories").handler(this::handleGetListCategories);
        router.get("/rest/blog/blogCount").handler(this::handleGetBlogCount);
        router.get("/rest/blog/blogItems/:pageNumber").handler(this::handleGetBlogItemsMainCategoryByPageNumber);
        router.route("/*").handler(StaticHandler.create());
        return router;
    }

    private void initData(Reader reader) {
        List<BlogItem> list = gson.fromJson(reader, TypeToken.getParameterized(List.class, BlogItem.class).getType());
        blogItems = new CopyOnWriteArrayList<>(list);

        if (blogItems == null) {
            blogItems = Collections.singletonList(buildEmptyBlogItem());
        }

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
        routingContext.response().putHeader("content-type", "application/json").end(CATEGORIES_JSON);
    }

    private void handleGetBlogCount(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "text/plain").end(String.valueOf(blogItems.size()));
    }

    private void handleGetBlogItemsMainCategoryByPageNumber(RoutingContext routingContext) {
        String pageNumberParam = routingContext.request().getParam("pageNumber");
        HttpServerResponse response = routingContext.response();
        if (pageNumberParam == null) {
            sendBadRequestInvalidPageNumberError(response);
        } else {
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(pageNumberParam);
            } catch (NumberFormatException e) {
                sendBadRequestInvalidPageNumberError(response);
                return;
            }

            if (pageNumber >= 1 && pageNumber <= totalPages) {
                int endIdx = pageNumber * ITEMS_PER_PAGE;
                int startIdx = endIdx - ITEMS_PER_PAGE;

                if (pageNumber == this.totalPages && itemsOnLastPage != 0) {
                    endIdx = startIdx + itemsOnLastPage;
                }

                response.putHeader("content-type", "application/json").end(gson.toJson(blogItems.subList(startIdx, endIdx)));
            } else {
                sendBadRequestInvalidPageNumberError(response);
            }
        }
    }

    private void sendBadRequestInvalidPageNumberError(HttpServerResponse response) {
        response.setStatusCode(400).end("Bad Request - Invalid Page Number");
    }

    private BlogItem buildEmptyBlogItem() {
        BlogItem blogItem = new BlogItem();
        blogItem.setBlogSection("Main");
        blogItem.setBody("All setup");
        blogItem.setId(0);
        blogItem.setTitle("All Ready");
        blogItem.setCreateDay("00");
        blogItem.setCreateMonth("00");
        blogItem.setCreateYear("0000");
        blogItem.setCreatedOn("0000-00-00");
        blogItem.setModifiedOn("0000-00-00");
        return blogItem;
    }
}
