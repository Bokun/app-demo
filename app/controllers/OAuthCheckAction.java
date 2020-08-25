package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import models.BokunCustomer;
import models.OAuthRecord;
import org.apache.commons.codec.binary.Hex;
import play.libs.typedmap.TypedKey;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This action filters incoming requests to our app.
 * If the request comes from Bokun, we validate the hmac.
 * If the request has a valid cookie, we let it through.
 * If not, we do the OAuth dance.
 */
public class OAuthCheckAction extends play.mvc.Action.Simple {

    public static final TypedKey<BokunCustomer> CUSTOMER = TypedKey.<BokunCustomer>create("customer");

    private final Config config;
    private final WSClient ws;

    @Inject
    public OAuthCheckAction(Config config, WSClient ws) {
        this.config = config;
        this.ws = ws;
    }

    public CompletionStage<Result> call(Http.Request req) {
        // hmac
        String hmac = req.queryString("hmac").orElse(null);
        String domain = req.queryString("domain").orElse(null);
        if ( !Strings.isNullOrEmpty(hmac) ) {
            // hmac was sent, we need to validate
            if ( !hmacIsValid(hmac, queryStringWithoutHmac(req.queryString())) ) {
                return CompletableFuture.completedFuture(unauthorized("The hmac value is no good."));
            }
            if ( Strings.isNullOrEmpty(domain) ) {
                return CompletableFuture.completedFuture(unauthorized("The domain value is no good."));
            }

            // is state parameter present? then we need to validate it
            String state = req.queryString("state").orElse(null);
            if ( !Strings.isNullOrEmpty(state) ) {
                OAuthRecord record = OAuthRecord.find.query().where().eq("state", state).findOneOrEmpty().orElse(null);
                if ( record == null ) {
                    return CompletableFuture.completedFuture(unauthorized("The state value is no good."));
                }

                // Is the code parameter in the request? If so, we can finalize the OAuth
                String code = req.queryString("code").orElse(null);
                if ( !Strings.isNullOrEmpty(code) ) {
                    // finalize: store access token and vendor ID in db, store cookie
                    return ws.url(String.format(config.getString("bokun.url"), domain) + "/appstore/oauth/access_token")
                            .setContentType("application/x-www-form-urlencoded")
                            .post("client_id=" + config.getString("app.apiKey") + "&client_secret=" + config.getString("app.secretKey") + "&code=" + code)
                            .thenComposeAsync(response -> {
                                JsonNode json = response.asJson();
                                final String vendorId = json.findPath("vendor_id").asText();
                                String accessToken = json.findPath("access_token").asText();

                                BokunCustomer customer = BokunCustomer.findByVendorId(vendorId);
                                if ( customer == null ) {
                                    customer = new BokunCustomer();
                                    customer.setVendorId(vendorId);
                                    customer.setAccessToken(accessToken);
                                    customer.setDomain(domain);
                                    customer.setPermissions(json.findPath("scope").asText());
                                    customer.save();
                                } else {
                                    customer.setAccessToken(accessToken);
                                    customer.setPermissions(json.findPath("scope").asText());
                                    customer.setDomain(domain);
                                    customer.update();
                                }

                                return delegate.call(req.addAttr(CUSTOMER, customer)).thenApply(result -> result.addingToSession(req, "vendor", vendorId));
                            })
                            ;
                } else {
                    // ok we need to do the OAuth dance
                    return CompletableFuture.completedFuture(redirect(createOAuthUrl(domain, req)));
                }

            } else {
                // do the OAuth dance
                return CompletableFuture.completedFuture(redirect(createOAuthUrl(domain, req)));
            }
        }

        // check the session cookie
        String vendorId = req.session().get("vendor").orElse(null);
        if ( Strings.isNullOrEmpty(vendorId) ) {
            // no cookie and no hmac
            return CompletableFuture.completedFuture(unauthorized("Please open this app from inside Bokun."));
        }

        // do we have a record of this vendor?
        BokunCustomer customer = BokunCustomer.findByVendorId(vendorId);
        if ( customer == null ) {
            // no record found
            return CompletableFuture.completedFuture(unauthorized("Please open this app from inside Bokun."));
        }

        // Are new permissions needed?
        if ( !customer.hasRequiredPermissions(config.getString("app.permissions")) ) {
            // need to reauthenticate
            return CompletableFuture.completedFuture(redirect(createOAuthUrl(customer.getDomain(), req)).removingFromSession(req, "vendor"));
        }

        return delegate.call(req.addAttr(CUSTOMER, customer));
    }



    private String createOAuthUrl(String domain, Http.Request req) {
        OAuthRecord record = new OAuthRecord();
        record.setTimestamp(LocalDateTime.now());
        record.setState(UUID.randomUUID().toString());
        record.save();

        String redirectUri = config.getString("app.url") + req.path();

        return String.format(config.getString("bokun.url"), domain) + "/appstore/oauth/authorize" +
                "?state=" + record.getState() +
                "&client_id=" + config.getString("app.apiKey") +
                "&redirect_uri=" + redirectUri +
                "&scope=" + config.getString("app.permissions")
                ;
    }

    private String queryStringWithoutHmac(Map<String,String[]> params) {
        StringBuilder s = new StringBuilder();
        for (String key : params.keySet().stream().filter(k -> !k.equalsIgnoreCase("hmac")).sorted().collect(Collectors.toList())) {
            if ( s.length() > 0 ) {
                s.append('&');
            }
            s.append(key);
            s.append('=');
            String[] values = params.get(key);
            if ( values.length > 1 ) {
                s.append(Joiner.on(',').join(values));
            } else {
                s.append(values[0]);
            }
        }
        return s.toString();
    }

    private boolean hmacIsValid(String hmac, String queryStringWithoutHmac) {
        try {
            String secret = config.getString("app.secretKey");
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            byte[] output = mac.doFinal(queryStringWithoutHmac.getBytes(StandardCharsets.UTF_8));
            String hex = Hex.encodeHexString(output);
            return hex.equals(hmac);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
