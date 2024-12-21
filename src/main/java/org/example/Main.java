package org.example;

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

    private static final String ADDRESS_A = "Санкт-Петербург, Парк Авиаторов";
    private static final String COORDINATES_A = "30.3008211724963,59.8673116";

    private static final String ADDRESS_B = "Санкт-Петербург, Парк Победы";
    private static final String COORDINATES_B = "30.3214922,59.8676183";

    private static final String ORS_API_KEY = "здесь-должен-быть-ваш-ключ";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    public static void main(String[] args) throws URISyntaxException, IOException {
        final String response = doGetRequest(ORS_URL, Map.of(
                                                     "api_key", ORS_API_KEY,
                                                     "start", COORDINATES_A,
                                                     "end", COORDINATES_B
                                             )
        );
        System.out.println(response);

        final String coordinatesAResponse = doGetRequest(OSM_SEARCH_URL, Map.of(
                                                                 "q", ADDRESS_A,
                                                                 "format", "json"
                                                         )
        );
        System.out.println(coordinatesAResponse);

        final String coordinatesBResponse = doGetRequest(OSM_SEARCH_URL, Map.of(
                                                                 "q", ADDRESS_B,
                                                                 "format", "json"
                                                         )
        );
        System.out.println(coordinatesBResponse);
    }

    static String doGetRequest(final String url,
                               final Map<String, String> queryParams) throws URISyntaxException, IOException {
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

        return response.toString();
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
            stringBuilder.append(i != 0 ? "&" : "")
                    .append(param);
        }
        return stringBuilder.toString();
    }
}