package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String ORS_API_KEY = "апи-ключ-сюда";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
//    private static final String OSM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws URISyntaxException, IOException {

        final Scanner scanner = new Scanner(System.in);

        System.out.print("Введите начальный адрес: ");
        final String addressA = scanner.nextLine();
        final JsonObject locationAInfo = doGetRequest(
                OSM_SEARCH_URL,
                Map.of("q", addressA, "format", "json")
        ).getAsJsonArray().get(0).getAsJsonObject();

        final String coordinatesA = locationAInfo.getAsJsonPrimitive("lon").getAsString()
                + ","
                + locationAInfo.getAsJsonPrimitive("lat").getAsString();

        System.out.print("Введите конечный адрес: ");
        final String addressB = scanner.nextLine();
        final JsonObject locationBInfo = doGetRequest(
                OSM_SEARCH_URL,
                Map.of("q", addressB, "format", "json")
        ).getAsJsonArray().get(0).getAsJsonObject();

        final String coordinatesB = locationBInfo.getAsJsonPrimitive("lon").getAsString()
                + ","
                + locationBInfo.getAsJsonPrimitive("lat").getAsString();

        final JsonObject routSegment = doGetRequest(
                ORS_URL,
                Map.of(
                        "api_key", ORS_API_KEY,
                        "start", coordinatesA,
                        "end", coordinatesB
                )
        ).getAsJsonObject()
                .getAsJsonArray("features")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("properties")
                .getAsJsonArray("segments")
                .get(0)
                .getAsJsonObject();

        System.out.println("Расстояние: " + routSegment.getAsJsonPrimitive("distance") + " м.");
        System.out.println("Продолжительность маршрута: " + routSegment.getAsJsonPrimitive("duration") + " сек.");

        for (final JsonElement step : routSegment.getAsJsonArray("steps")) {
            System.out.println("-------------------------------");
            System.out.println("Дистанция: " + step.getAsJsonObject().getAsJsonPrimitive("distance") + " м.");
            System.out.println("Продолжительность: " + step.getAsJsonObject().getAsJsonPrimitive("duration") + " сек.");
            System.out.println("Инструкция: " + step.getAsJsonObject().getAsJsonPrimitive("instruction").getAsString());
        }
    }

    static JsonElement doGetRequest(
            final String url,
            final Map<String, String> queryParams
    ) throws URISyntaxException, IOException {
        final String queryString = buildQueryParams(queryParams);
        final HttpURLConnection connection = (HttpURLConnection) new URI(url + queryString)
                .toURL()
                .openConnection();
        connection.setRequestMethod("GET");
        System.out.println(connection.getResponseCode() + " " + connection.getResponseMessage());

        final Scanner scanner = new Scanner(connection.getInputStream());

        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }

        return GSON.fromJson(response.toString(), JsonElement.class);
    }

    static String buildQueryParams(final Map<String, String> queryParams) {
        if (queryParams.isEmpty()) {
            return "";
        }

        //["key1=value1", "key2=value2"]
        final List<String> formattedParams = new ArrayList<>();
        for (final Map.Entry<String, String> param : queryParams.entrySet()) {
            formattedParams.add(param.getKey() + "=" + URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
        }

        //?key1=value1&key2=value2...
        final StringBuilder stringBuilder = new StringBuilder("?");
        for (int i = 0; i < formattedParams.size(); i++) {
            final String param = formattedParams.get(i);
            stringBuilder.append(i != 0 ? "&" : "").append(param);
        }
        return stringBuilder.toString();
    }
}