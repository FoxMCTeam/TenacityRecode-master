package dev.tenacity.utils.client.addons.microsoft;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import dev.tenacity.ui.notifications.NotificationManager;
import dev.tenacity.ui.notifications.NotificationType;
import dev.tenacity.utils.objects.SystemUtils;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MicrosoftLogin {
    public static final RequestConfig REQUEST_CONFIG = RequestConfig
            .custom()
            .setConnectionRequestTimeout(30_000)
            .setConnectTimeout(30_000)
            .setSocketTimeout(30_000)
            .build();
    public static final String CLIENT_ID = "42a60a84-599d-44b2-a7c6-b00cdef1d6a2";
    public static final int PORT = 25575;

    public static class LoginData {
        public final String username;
        public final String uuid;
        public final String mcToken;

        public LoginData(String username, String uuid, String mcToken) {
            this.username = username;
            this.uuid = uuid;
            this.mcToken = mcToken;
        }
    }

    public static void getRefreshToken(Consumer<LoginData> callback) {
        String state = UUID.randomUUID().toString().substring(0, 8);
        URI authLink = getMSAuthLink(state);

        NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                "Opening browser for authentication...", 5);
        SystemUtils.setClipboard(authLink != null ? authLink.toString() : "");
        SystemUtils.openWebLink(authLink);
        CompletableFuture<String> authCodeFuture = acquireMSAuthCode(state);

        authCodeFuture.thenCompose(authCode -> {
                    NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                            "Authenticating with Microsoft...", 5);
                    return acquireMSAccessTokens(authCode);
                })
                .thenCompose(tokens -> {
                    NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                            "Authenticating with Xbox Live...", 5);
                    return acquireXboxAccessToken(tokens.get("access_token"));
                })
                .thenCompose(xboxToken -> {
                    NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                            "Getting XSTS token...", 5);
                    return acquireXboxXstsToken(xboxToken);
                })
                .thenCompose(xstsData -> {
                    NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                            "Authenticating with Minecraft...", 5);
                    return acquireMCAccessToken(xstsData.get("Token"), xstsData.get("uhs"));
                })
                .thenCompose(mcToken -> {
                    NotificationManager.post(NotificationType.INFO, "Microsoft Auth",
                            "Fetching Minecraft profile...", 5);
                    return login(mcToken);
                })
                .thenAccept(session -> {
                    callback.accept(new LoginData(
                            session.getUsername(),
                            session.getPlayerID(),
                            session.getToken()
                    ));
                    NotificationManager.post(NotificationType.SUCCESS, "Microsoft Auth",
                            "Successfully logged in as " + session.getUsername(), 5);
                })
                .exceptionally(e -> {
                    NotificationManager.post(NotificationType.WARNING, "Microsoft Auth",
                            "Login failed: " + e.getCause().getMessage(), 5);
                    return null;
                });
    }

    // 以下是原有的MicrosoftAuth方法保持不变
    public static URI getMSAuthLink(String state) {
        try {
            URIBuilder uriBuilder = new URIBuilder("https://login.live.com/oauth20_authorize.srf")
                    .addParameter("client_id", CLIENT_ID)
                    .addParameter("response_type", "code")
                    .addParameter("redirect_uri", String.format("http://localhost:%d/callback", PORT))
                    .addParameter("scope", "XboxLive.signin XboxLive.offline_access")
                    .addParameter("state", state)
                    .addParameter("prompt", "select_account");
            return uriBuilder.build();
        } catch (Exception e) {
            return null;
        }
    }

    public static CompletableFuture<String> acquireMSAuthCode(String state) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> authCode = new AtomicReference<>(null);
                AtomicReference<String> errorMsg = new AtomicReference<>(null);

                server.createContext("/callback", exchange -> {
                    Map<String, String> query = URLEncodedUtils
                            .parse(exchange.getRequestURI().toString().replaceAll("/callback\\?", ""), StandardCharsets.UTF_8)
                            .stream()
                            .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

                    if (!state.equals(query.get("state"))) {
                        errorMsg.set(String.format("State mismatch! Expected '%s' but got '%s'.", state, query.get("state")));
                    } else if (query.containsKey("code")) {
                        authCode.set(query.get("code"));
                    } else if (query.containsKey("error")) {
                        errorMsg.set(String.format("%s: %s", query.get("error"), query.get("error_description")));
                    }

                    InputStream stream = MicrosoftLogin.class.getResourceAsStream("/callback.html");
                    byte[] response = stream != null ? IOUtils.toByteArray(stream) : new byte[0];
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.getResponseBody().close();
                    latch.countDown();
                });

                try {
                    server.start();
                    latch.await();
                    return Optional.ofNullable(authCode.get())
                            .filter(code -> !StringUtils.isBlank(code))
                            .orElseThrow(() -> new Exception(
                                    Optional.ofNullable(errorMsg.get())
                                            .orElse("No auth code or error description present.")
                            ));
                } finally {
                    server.stop(2);
                }
            } catch (InterruptedException e) {
                throw new CancellationException("Microsoft auth code acquisition cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to acquire Microsoft auth code!", e);
            }
        });
    }

    private static CloseableHttpClient createTrustedHttpClient() {
        try {
            SSLSocketFactory socketFactory = SSLUtil.getSSLContext().getSocketFactory();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    socketFactory,
                    new String[]{"TLSv1.2"},
                    null,
                    new BrowserCompatHostnameVerifier()
            );
            return HttpClientBuilder.create().setSSLSocketFactory(sslsf).build();
        } catch (Exception e) {
            e.printStackTrace();
            return HttpClients.createDefault();
        }
    }

    public static CompletableFuture<Map<String, String>> acquireMSAccessTokens(String authCode) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = createTrustedHttpClient()) {
                HttpPost request = new HttpPost(URI.create("https://login.live.com/oauth20_token.srf"));
                request.setConfig(REQUEST_CONFIG);
                request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                request.setEntity(new UrlEncodedFormEntity(
                        Arrays.asList(
                                new BasicNameValuePair("client_id", CLIENT_ID),
                                new BasicNameValuePair("grant_type", "authorization_code"),
                                new BasicNameValuePair("code", authCode),
                                new BasicNameValuePair("redirect_uri", String.format("http://localhost:%d/callback", PORT))
                        ),
                        "UTF-8"
                ));

                HttpResponse res = client.execute(request);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
                String accessToken = Optional.ofNullable(json.get("access_token"))
                        .map(JsonElement::getAsString)
                        .filter(token -> !StringUtils.isBlank(token))
                        .orElseThrow(() -> new Exception(json.has("error") ?
                                String.format("%s: %s", json.get("error").getAsString(), json.get("error_description").getAsString()) :
                                "No Microsoft access token or error description present."
                        ));
                String refreshToken = Optional.ofNullable(json.get("refresh_token"))
                        .map(JsonElement::getAsString)
                        .filter(token -> !StringUtils.isBlank(token))
                        .orElseThrow(() -> new Exception(json.has("error") ?
                                String.format("%s: %s", json.get("error").getAsString(), json.get("error_description").getAsString()) :
                                "No Microsoft refresh token or error description present."
                        ));

                Map<String, String> result = new HashMap<>();
                result.put("access_token", accessToken);
                result.put("refresh_token", refreshToken);
                return result;
            } catch (InterruptedException e) {
                throw new CancellationException("Microsoft access tokens acquisition cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to acquire Microsoft access tokens!", e);
            }
        });
    }

    public static CompletableFuture<String> acquireXboxAccessToken(String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = createTrustedHttpClient()) {
                HttpPost request = new HttpPost(URI.create("https://user.auth.xboxlive.com/user/authenticate"));
                JsonObject entity = new JsonObject();
                JsonObject properties = new JsonObject();
                properties.addProperty("AuthMethod", "RPS");
                properties.addProperty("SiteName", "user.auth.xboxlive.com");
                properties.addProperty("RpsTicket", String.format("d=%s", accessToken));
                entity.add("Properties", properties);
                entity.addProperty("RelyingParty", "http://auth.xboxlive.com");
                entity.addProperty("TokenType", "JWT");
                request.setConfig(REQUEST_CONFIG);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(entity.toString()));

                HttpResponse res = client.execute(request);
                JsonObject json = res.getStatusLine().getStatusCode() == 200
                        ? new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject()
                        : new JsonObject();
                return Optional.ofNullable(json.get("Token"))
                        .map(JsonElement::getAsString)
                        .filter(token -> !StringUtils.isBlank(token))
                        .orElseThrow(() -> new Exception(json.has("XErr") ?
                                String.format("%s: %s", json.get("XErr").getAsString(), json.get("Message").getAsString()) :
                                "No access token or error description present."
                        ));
            } catch (InterruptedException e) {
                throw new CancellationException("Xbox Live access token acquisition cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to acquire Xbox Live access token!", e);
            }
        });
    }

    public static CompletableFuture<Map<String, String>> acquireXboxXstsToken(String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = createTrustedHttpClient()) {
                HttpPost request = new HttpPost("https://xsts.auth.xboxlive.com/xsts/authorize");
                JsonObject entity = new JsonObject();
                JsonObject properties = new JsonObject();
                JsonArray userTokens = new JsonArray();
                userTokens.add(new JsonPrimitive(accessToken));
                properties.addProperty("SandboxId", "RETAIL");
                properties.add("UserTokens", userTokens);
                entity.add("Properties", properties);
                entity.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
                entity.addProperty("TokenType", "JWT");
                request.setConfig(REQUEST_CONFIG);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(entity.toString()));

                HttpResponse res = client.execute(request);
                JsonObject json = res.getStatusLine().getStatusCode() == 200
                        ? new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject()
                        : new JsonObject();
                return Optional.ofNullable(json.get("Token"))
                        .map(JsonElement::getAsString)
                        .filter(token -> !StringUtils.isBlank(token))
                        .map(token -> {
                            String uhs = json.get("DisplayClaims").getAsJsonObject()
                                    .get("xui").getAsJsonArray()
                                    .get(0).getAsJsonObject()
                                    .get("uhs").getAsString();

                            Map<String, String> result = new HashMap<>();
                            result.put("Token", token);
                            result.put("uhs", uhs);
                            return result;
                        })
                        .orElseThrow(() -> new Exception(json.has("XErr") ?
                                String.format("%s: %s", json.get("XErr").getAsString(), json.get("Message").getAsString()) :
                                "No access token or error description present."
                        ));
            } catch (InterruptedException e) {
                throw new CancellationException("Xbox Live XSTS token acquisition cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to acquire Xbox Live XSTS token!", e);
            }
        });
    }

    public static CompletableFuture<String> acquireMCAccessToken(String xstsToken, String userHash) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = createTrustedHttpClient()) {
                HttpPost request = new HttpPost(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"));
                request.setConfig(REQUEST_CONFIG);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(
                        String.format("{\"identityToken\": \"XBL3.0 x=%s;%s\"}", userHash, xstsToken)
                ));

                HttpResponse res = client.execute(request);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
                return Optional.ofNullable(json.get("access_token"))
                        .map(JsonElement::getAsString)
                        .filter(token -> !StringUtils.isBlank(token))
                        .orElseThrow(() -> new Exception(json.has("error") ?
                                String.format("%s: %s", json.get("error").getAsString(), json.get("errorMessage").getAsString()) :
                                "No access token or error description present."
                        ));
            } catch (InterruptedException e) {
                throw new CancellationException("Minecraft access token acquisition cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to acquire Minecraft access token!", e);
            }
        });
    }

    public static CompletableFuture<Session> login(String mcToken) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient client = createTrustedHttpClient()) {
                HttpGet request = new HttpGet(URI.create("https://api.minecraftservices.com/minecraft/profile"));
                request.setConfig(REQUEST_CONFIG);
                request.setHeader("Authorization", "Bearer " + mcToken);

                HttpResponse res = client.execute(request);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
                return Optional.ofNullable(json.get("id"))
                        .map(JsonElement::getAsString)
                        .filter(uuid -> !StringUtils.isBlank(uuid))
                        .map(uuid -> new Session(
                                json.get("name").getAsString(),
                                uuid,
                                mcToken,
                                Session.Type.MOJANG.toString()
                        ))
                        .orElseThrow(() -> new Exception(json.has("error") ?
                                String.format("%s: %s", json.get("error").getAsString(), json.get("errorMessage").getAsString()) :
                                "No profile or error description present."
                        ));
            } catch (InterruptedException e) {
                throw new CancellationException("Minecraft profile fetching cancelled!");
            } catch (Exception e) {
                throw new CompletionException("Unable to fetch Minecraft profile!", e);
            }
        });
    }
}