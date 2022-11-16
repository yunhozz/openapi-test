package com.openapitest.api;

import com.openapitest.api.dto.TrainRequestDto;
import com.openapitest.api.dto.TrainResponseDto;
import com.openapitest.api.dto.WeatherRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api")
public class OpenApiTestController {

    @Value("${app.train.secretKey}")
    private String trainKey;

    @Value("${app.weather.secretKey}")
    private String weatherKey;

    @GetMapping("/train")
    public ResponseEntity<TrainResponseDto> getTrainApi(@RequestBody TrainRequestDto trainRequestDto) {
        TrainResponseDto trainResponseDto = null;
        String apiUrl = "https://apis.openapi.sk.com/puzzle/congestion-train/rltm/trains/"
                + trainRequestDto.getSubwayLine()
                + "/"
                + trainRequestDto.getTrainY();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("accept", "application/json")
                    .header("appkey", trainKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info(response.body());

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
            Boolean isSuccess = (Boolean) jsonObject.get("success");

            if (isSuccess) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                JSONObject congestionResult = (JSONObject) data.get("congestionResult");

                trainResponseDto = TrainResponseDto.builder()
                        .line(String.valueOf(data.get("subwayLine")))
                        .number(String.valueOf(data.get("trainY")))
                        .congestionRatio(String.valueOf(congestionResult.get("congestionTrain")))
                        .congestionCar(String.valueOf(congestionResult.get("congestionCar")))
                        .congestionType(String.valueOf(congestionResult.get("congestionType")))
                        .build();

            } else {
                trainResponseDto = TrainResponseDto.builder()
                        .code(Integer.parseInt(String.valueOf(jsonObject.get("code"))))
                        .msg(String.valueOf(jsonObject.get("msg")))
                        .build();

                return new ResponseEntity<>(trainResponseDto, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(trainResponseDto);
    }

    @GetMapping("/weather")
    public ResponseEntity<JSONObject> getWeatherApi(@RequestBody WeatherRequestDto weatherRequestDto) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        StringBuilder sb = new StringBuilder(apiUrl);

        try {
            sb.append("?").append(URLEncoder.encode("ServiceKey", StandardCharsets.UTF_8)).append("=").append(weatherKey);
            sb.append("&").append(URLEncoder.encode("nx", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getNx(), StandardCharsets.UTF_8)); //경도
            sb.append("&").append(URLEncoder.encode("ny", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getNy(), StandardCharsets.UTF_8)); //위도
            sb.append("&").append(URLEncoder.encode("base_date", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getBaseDate(), StandardCharsets.UTF_8)); // 조회하고싶은 날짜
            sb.append("&").append(URLEncoder.encode("base_time", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getBaseTime(), StandardCharsets.UTF_8)); // 조회하고싶은 시간 AM 02시부터 3시간 단위
            sb.append("&").append(URLEncoder.encode("dataType", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("json", StandardCharsets.UTF_8)); // 타입

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sb.toString()))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info(response.body());

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());

            return ResponseEntity.ok(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}