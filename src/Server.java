import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {
    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);


        server.createContext("/site/signup", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                try {
                    boolean success = AuthService.register(form.get("username"), form.get("email"), form.get("password"));
                    if (success) {
                        // Редирекција кон login page
                        exchange.getResponseHeaders().add("Location", "/site/login");
                        exchange.sendResponseHeaders(302, -1); // 302 redirect
                        exchange.close();
                    } else {
                        serveFile(exchange, "web/fail.html");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, "Грешка при регистрација!");
                }
            } else {
                serveFile(exchange, "web/register.html");
            }
        });


        server.createContext("/site/login", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> form = parseForm(exchange);
                try {
                    if (AuthService.login(form.get("username"), form.get("password"))) {
                        String token = SessionManager.createSession(form.get("username"));
                        exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; HttpOnly; Path=/");
                        exchange.getResponseHeaders().add("Location", "/site/dashboard");
                        exchange.sendResponseHeaders(302, -1);
                        exchange.close();
                    } else {
                        serveFile(exchange, "web/fail.html");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, "Грешка при најава!");
                }
            } else {
                serveFile(exchange, "web/login.html");
            }
        });


        server.createContext("/site/dashboard", exchange -> {
            String token = null;
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie != null) {
                for (String c : cookie.split(";")) {
                    if (c.trim().startsWith("session=")) {
                        token = c.trim().substring("session=".length());
                        break;
                    }
                }
            }

            if (token != null && SessionManager.isValid(token)) {
                serveFile(exchange, "web/dashboard.html");
            } else {
                exchange.getResponseHeaders().add("Location", "/site/login");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
            }
        });


        server.createContext("/site/logout", exchange -> {
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie != null && cookie.contains("session=")) {
                String token = cookie.split("session=")[1];
                SessionManager.removeSession(token);
            }
            exchange.getResponseHeaders().add("Location", "/site/login");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.start();
        System.out.println("✅ Server started at http://localhost:8080");
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private static void serveFile(HttpExchange exchange, String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            sendResponse(exchange, "404 - Not Found: " + filePath);
            return;
        }
        byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
        exchange.sendResponseHeaders(200, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
        String formData = br.readLine();
        Map<String, String> map = new HashMap<>();
        if (formData != null) {
            for (String pair : formData.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) map.put(kv[0], java.net.URLDecoder.decode(kv[1], "UTF-8"));
            }
        }
        return map;
    }
}
