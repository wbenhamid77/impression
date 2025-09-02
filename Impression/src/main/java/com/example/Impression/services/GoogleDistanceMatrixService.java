package com.example.Impression.services;

import com.example.Impression.entities.Stade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleDistanceMatrixService {

    @Value("${google.maps.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<ResultatDistance> calculerDistancesEtTemps(BigDecimal origineLat, BigDecimal origineLon,
            List<Stade> stades) {
        List<ResultatDistance> resultats = new ArrayList<>();
        if (!isEnabled() || stades == null || stades.isEmpty()) {
            return resultats;
        }

        try {
            String origins = URLEncoder.encode(origineLat + "," + origineLon, StandardCharsets.UTF_8);

            StringBuilder destinationsBuilder = new StringBuilder();
            for (int i = 0; i < stades.size(); i++) {
                Stade s = stades.get(i);
                if (i > 0)
                    destinationsBuilder.append("|");
                destinationsBuilder.append(s.getLatitude()).append(",").append(s.getLongitude());
            }
            String destinations = URLEncoder.encode(destinationsBuilder.toString(), StandardCharsets.UTF_8);

            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origins
                    + "&destinations=" + destinations
                    + "&mode=driving&units=metric&key=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null)
                return resultats;

            JsonNode root = objectMapper.readTree(response);
            if (!"OK".equals(root.path("status").asText())) {
                return resultats;
            }

            JsonNode rows = root.path("rows");
            if (!rows.isArray() || rows.size() == 0)
                return resultats;
            JsonNode elements = rows.get(0).path("elements");

            for (int i = 0; i < stades.size() && i < elements.size(); i++) {
                JsonNode elem = elements.get(i);
                String status = elem.path("status").asText();
                if (!"OK".equals(status)) {
                    continue;
                }
                long meters = elem.path("distance").path("value").asLong(0);
                long seconds = elem.path("duration").path("value").asLong(0);

                BigDecimal distanceKm = BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 2,
                        RoundingMode.HALF_UP);
                Integer dureeMinutes = BigDecimal.valueOf(seconds)
                        .divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP).intValue();

                resultats.add(new ResultatDistance(stades.get(i), distanceKm, dureeMinutes));
            }
        } catch (Exception ignored) {
        }

        return resultats;
    }

    public static class ResultatDistance {
        private final Stade stade;
        private final BigDecimal distanceKm;
        private final Integer dureeMinutes;

        public ResultatDistance(Stade stade, BigDecimal distanceKm, Integer dureeMinutes) {
            this.stade = stade;
            this.distanceKm = distanceKm;
            this.dureeMinutes = dureeMinutes;
        }

        public Stade getStade() {
            return stade;
        }

        public BigDecimal getDistanceKm() {
            return distanceKm;
        }

        public Integer getDureeMinutes() {
            return dureeMinutes;
        }
    }
}
