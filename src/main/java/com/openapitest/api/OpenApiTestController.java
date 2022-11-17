package com.openapitest.api;

import com.openapitest.api.dto.Response;
import com.openapitest.api.dto.train.TrainRequestDto;
import com.openapitest.api.dto.train.TrainResponseDto;
import com.openapitest.api.dto.weather.WeatherRequestDto;
import com.openapitest.api.dto.weather.WeatherResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api")
public class OpenApiTestController {

    @Value("${app.train.secretKey}")
    private String trainKey;

    @Value("${app.weather.secretKey}")
    private String weatherKey;

    @PostMapping("/train")
    public Response getTrainApi(@RequestBody TrainRequestDto trainRequestDto) {
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

                return Response.failure(-1000, trainResponseDto, "데이터가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.success(trainResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/weather")
    public Response getWeatherApi(@RequestBody WeatherRequestDto weatherRequestDto) {
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"; // 초단기 예보 조회
        StringBuilder sb = new StringBuilder(apiUrl);

        JSONObject jsonObject = null; // 샘플 데이터
        WeatherResponseDto weatherResponseDto = null;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddkkmm"));
        String nowDate = nowStr.substring(0, 8); // 현재 날짜
        String nowTime = nowStr.substring(8); // 현재 시각
        log.info("date=" + nowDate);
        log.info("time=" + nowTime);

        try {
            sb.append("?").append(URLEncoder.encode("ServiceKey", StandardCharsets.UTF_8)).append("=").append(weatherKey);
            sb.append("&").append(URLEncoder.encode("nx", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getNx(), StandardCharsets.UTF_8)); // 경도
            sb.append("&").append(URLEncoder.encode("ny", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(weatherRequestDto.getNy(), StandardCharsets.UTF_8)); // 위도
            sb.append("&").append(URLEncoder.encode("base_date", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(nowDate, StandardCharsets.UTF_8)); // 조회하고 싶은 날짜
            sb.append("&").append(URLEncoder.encode("base_time", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(nowTime, StandardCharsets.UTF_8)); // 조회하고 싶은 시간
            sb.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("60", StandardCharsets.UTF_8)); // 한 페이지 결과 수
            sb.append("&").append(URLEncoder.encode("dataType", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("json", StandardCharsets.UTF_8)); // 타입

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sb.toString()))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info(response.body());

            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(response.body()); // 샘플 데이터

            JSONObject responseData = (JSONObject) ((JSONObject) jsonParser.parse(response.body())).get("response");
            JSONObject header = (JSONObject) responseData.get("header");
            String resultCode = (String) header.get("resultCode");

            if (resultCode.equals("00")) {
                JSONObject items = (JSONObject) ((JSONObject) responseData.get("body")).get("items");
                JSONArray item = (JSONArray) items.get("item");

                for (Object data : item) {
                    /*
                     * T1H: 기온(C)
                     * REH: 습도(%)
                     * RN1: 1시간 강수량(mm)
                     * PTY: 강수 형태(0 ~ 7)
                     * WSD: 풍속(m/s)
                     */
                    String category = (String) ((JSONObject) data).get("category"); // 카테고리 (T1H, REH, RN1, PTY, WSD)
                    String forecastDate = (String) ((JSONObject) data).get("fcstDate"); // 예측 날짜
                    String forecastTime = (String) ((JSONObject) data).get("fcstTime"); // 예측 시각
                    String forecastValue = (String) ((JSONObject) data).get("fcstValue"); // 카테고리 별 예측 값
                    // TODO: 2022-11-17 response dto 로 가공
                }

            } else {
                String resultMsg = (String) header.get("resultMsg");
                weatherResponseDto = WeatherResponseDto.builder()
                        .resultCode(resultCode)
                        .resultMsg(resultMsg)
                        .build();

                return Response.failure(-1000, weatherResponseDto, "데이터가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.success(jsonObject, HttpStatus.CREATED); // 샘플 데이터
//        return Response.success(weatherResponseDto, HttpStatus.CREATED);
    }
}