package controllers;

import com.typesafe.config.Config;
import models.BokunCustomer;
import play.libs.Json;
import play.mvc.*;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final WSClient ws;
    private final Config config;

    @Inject
    public HomeController(WSClient ws, Config config) {
        this.ws = ws;
        this.config = config;
    }

    @OAuthCheck
    public CompletionStage<Result> index(Http.Request request) {
        BokunCustomer customer = request.attrs().get(OAuthCheckAction.CUSTOMER);

        String vendorUsersGQL = "query VendorUsersQuery($filter: VendorUserFilterInput!, $first:Int!) {" +
                "vendorUsers(filter: $filter, first:$first) {" +
                "    totalCount" +
                "    edges {" +
                "      node {" +
                "          id" +
                "          firstName" +
                "          lastName" +
                "          email" +
                "          timeZone" +
                "          phoneNumber" +
                "          lastLoginDate" +
                "          createdDate" +
                "          emailVerified" +
                "      }" +
                "      }" +
                "    }" +
                "}"
                ;

        Map<String,Object> filter = new HashMap<>();
        filter.put("searchTerm", "");

        Map<String,Object> variables = new HashMap<>();
        variables.put("first", 10);
        variables.put("filter", filter);

        Map<String,Object> map = new HashMap<>();
        map.put("operationName", "VendorUsersQuery");
        map.put("query", vendorUsersGQL);
        map.put("variables", variables);

        return ws.url(String.format(config.getString("bokun.url"), customer.getDomain()) + "/api/graphql")
                .addHeader("X-Bokun-App-Access-Token", customer.getAccessToken())
                .post(Json.toJson(map))
                .thenApply(response -> ok(views.html.install_confirmed.render(Json.prettyPrint(response.asJson()))));
    }

}
