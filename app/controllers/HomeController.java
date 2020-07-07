package controllers;

import play.mvc.*;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private static final String CLIENT_ID = "bb5d27dda5a24c4eaf8263ac5a5054f8";
    private static final String CLIENT_SECRET = "834404ae8e22453e967adcc6d6f95d93";

    private final WSClient ws;

    @Inject
    public HomeController(WSClient ws) {
        this.ws = ws;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    public Result install() {
        String state = UUID.randomUUID().toString();
        String redirectUri = "http://localhost:8181/install/confirmed";
        String scope = "BOOKINGS_READ,CUSTOMER_CONTACT_INFO";

        return redirect("http://bokun.localhost:3000/appstore/oauth/authorize" +
                "?state=" + state +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope
        );
    }

    public CompletionStage<Result> installConfirmed(Http.Request request) {
        String code = request.queryString("code").get();
        return ws.url("http://localhost:3000/appstore/oauth/access_token")
                .setContentType("application/x-www-form-urlencoded")
                .post("client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&code=" + code)
                .thenApply(response -> ok(views.html.install_confirmed.render(response.asJson().findPath("access_token").asText())))
                ;
    }

}
