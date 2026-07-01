package org.example.weddingcard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TransitSuggestController {

    @Value("${kakao.rest-key:}")
    private String kakaoRestKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    @GetMapping("/transit-suggest")
    public ResponseEntity<?> suggest(@RequestParam String address) {
        if (kakaoRestKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "KAKAO_REST_KEY 환경변수가 설정되지 않았습니다."));
        }

        double[] coords = geocode(address);
        if (coords == null) {
            return ResponseEntity.ok(Map.of("error", "주소를 찾을 수 없습니다. 더 자세한 주소를 입력해주세요."));
        }

        String subway = searchNearbySubway(coords[0], coords[1]);
        return ResponseEntity.ok(Map.of("subway", subway));
    }

    private double[] geocode(String address) {
        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);

        Map<String, Object> body = kakaoGet(
                "https://dapi.kakao.com/v2/local/search/address.json?query=" + encoded);
        List<Map<String, Object>> docs = castList(body.get("documents"));

        if (docs == null || docs.isEmpty()) {
            body = kakaoGet("https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encoded);
            docs = castList(body.get("documents"));
        }
        if (docs == null || docs.isEmpty()) return null;

        Map<String, Object> loc = docs.get(0);
        double x = Double.parseDouble(String.valueOf(loc.get("x")));
        double y = Double.parseDouble(String.valueOf(loc.get("y")));
        return new double[]{x, y};
    }

    private String searchNearbySubway(double x, double y) {
        String url = "https://dapi.kakao.com/v2/local/search/category.json"
                + "?category_group_code=SW8&x=" + x + "&y=" + y
                + "&radius=1000&sort=distance&size=15";

        Map<String, Object> body = kakaoGet(url);
        List<Map<String, Object>> stations = castList(body.get("documents"));
        if (stations == null || stations.isEmpty()) return "";

        Map<String, List<String>> stationLines = new LinkedHashMap<>();
        Map<String, Integer> stationDist = new LinkedHashMap<>();

        for (Map<String, Object> s : stations) {
            String name = (String) s.get("place_name");
            String line = extractLine((String) s.get("category_name"));
            int dist = Integer.parseInt(String.valueOf(s.getOrDefault("distance", "0")));
            stationLines.computeIfAbsent(name, k -> new ArrayList<>()).add(line);
            stationDist.putIfAbsent(name, dist);
        }

        List<String> parts = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, List<String>> entry : stationLines.entrySet()) {
            if (count++ >= 2) break;
            String name = entry.getKey();
            String lines = String.join("·", entry.getValue());
            int dist = stationDist.getOrDefault(name, 0);
            String walk = dist > 0 ? " (도보 약 " + Math.max(1, dist / 70) + "분)" : "";
            parts.add(lines + " " + name + walk);
        }
        return String.join(", ", parts);
    }

    private String extractLine(String categoryName) {
        if (categoryName == null) return "";
        String[] parts = categoryName.split(">");
        return parts[parts.length - 1].trim();
    }

    private Map<String, Object> kakaoGet(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, MAP_TYPE);
        return resp.getBody() != null ? resp.getBody() : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        if (obj instanceof List<?> list) return (List<Map<String, Object>>) list;
        return null;
    }
}
