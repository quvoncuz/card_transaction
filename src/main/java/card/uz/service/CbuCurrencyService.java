package card.uz.service;

import card.uz.dto.CurrencyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CbuCurrencyService {

    private static final String BASE_URL =
            "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";

    @Autowired
    private static HttpClient httpClient;
    @Autowired
    private static ObjectMapper objectMapper;

    public static CurrencyDTO getByCurrencyCode(String currencyCode) {

        try {
            String url = BASE_URL + currencyCode + "/";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return objectMapper.readValue(
                    response.body(),
                    CurrencyDTO.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Valyuta bo‘yicha ma'lumot olishda xatolik", e);
        }
    }
}
