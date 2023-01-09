import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/*
 * Data Service as a vertx event-loop
 */
public class DataService extends AbstractVerticle {

    private int port;
    private static final int MAX_SIZE = 10;
    private LinkedList<DataPoint> values;
    SerialCommChannel channel = new SerialCommChannel("com12", 9600);
    static Vertx vertx = Vertx.vertx();

    public DataService(int port) throws Exception {
        values = new LinkedList<>();
        this.port = port;
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(io.vertx.ext.web.handler.CorsHandler.create(".*.")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.PUT)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        router.route().handler(BodyHandler.create());
        router.errorHandler(500, rc -> {
            System.err.println("Handling failure");
            Throwable failure = rc.failure();
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        router.post("/api/data").handler(this::handleAddNewData);
        router.get("/api/data").handler(this::handleGetData);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(port);

        log("Service ready.");

    }

    private void handleAddNewData(RoutingContext routingContext) {
        String msg = "";
        if(channel.isMsgAvailable()){
            try{
                msg = channel.receiveMsg();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        HttpServerResponse response = routingContext.response();
        JsonObject res = routingContext.getBodyAsJson();
        if (res == null) {
            sendError(400, response);
        } else {
            float lvl = res.getFloat("valLivello");
            String stato = res.getString("stato");
            long time = System.currentTimeMillis();
            int percApertura = res.getInteger("percApertura");

            String manMode = "";
            if(msg.length() != 0 && msg.equals("MANUALE") && stato.equals("ALLARME")) {
                manMode = "on";
            } else {
                manMode = "off";
            }
            values.addLast(new DataPoint(lvl, time, stato, percApertura, manMode));


            if (values.size() > MAX_SIZE) {
                values.removeLast();
            }

            log("Nuovo Livello: " + lvl + " Stato: " + stato + " Time: " + new Date(time) + "% Apertura:" + percApertura + " Modalita manuale: "+ manMode);
            response.setStatusCode(200).end();

            vertx.executeBlocking(
                    promise -> {
                        channel.sendMsg("l" + lvl + " " + "s" + stato + " " + "p" + percApertura);
                        promise.complete();
                    });
        }

    }

    private void handleGetData(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        for (DataPoint p: values) {
            JsonObject data = new JsonObject();
            data.put("time", p.getTime());
            data.put("valLivello", p.getValLivello());
            data.put("stato", p.getStato());
            data.put("percApertura", p.getPercApertura());
            data.put("manMode", p.getManMode());
            arr.add(data);
        }
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(arr.encodePrettily());
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void log(String msg) {
        System.out.println("[DATA SERVICE] "+msg);
    }

    public static void main(String[] args) throws Exception {
        DataService service = new DataService(8080);
        vertx.deployVerticle(service);
    }
}
