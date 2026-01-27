package com.chess.selfplay;
import java.net.http.*;
import java.net.URI;
import java.util.List;

public class httpClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:5000/predict";

    public static String getBestMove(String fen, List<String> legalMoves) throws Exception {
        String json = """
        {
            "fen": "%s"
            "legal_moves": %s,
            "top_k": 3

        }
        """.formatted(fen, legalMoves.toString());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = 
            client.send(request, HttpResponse.BodyHandlers.ofString());;

        return response.body();
    }
}
