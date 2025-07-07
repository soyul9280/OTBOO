package com.codeit.weatherwear.domain.weather.assembler;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WeatherApiData;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.domain.weather.support.WeatherCalculationHelper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherAssembler {

  private final WeatherCalculationHelper helper;

  public Weather assemble(
      String fcstDate,
      String baseDate,
      Map<String, WeatherApiData> categoryMap,
      Location location,
      Map<String, Map<String, List<WeatherApiData>>> groupedApiData
  ) {
    String fcstTime = helper.extractMinForecastTime(categoryMap);

    Double currentHumidity = helper.parseDoubleOrNull(helper.getFcstValue(categoryMap, "REH"));
    Double comparedHumidity = (currentHumidity != null)
        ? helper.calculateDifferenceFromPreviousDay(currentHumidity, "REH", fcstDate, fcstTime,
        groupedApiData)
        : null;

    Double currentTemperature = helper.parseDoubleOrNull(helper.getFcstValue(categoryMap, "TMP"));
    Double comparedTemperature = (currentTemperature != null)
        ? helper.calculateDifferenceFromPreviousDay(currentTemperature, "TMP", fcstDate, fcstTime,
        groupedApiData)
        : null;

    WeatherApiData tmxData = categoryMap.get("TMX");
    Double tmx = (tmxData != null) ? Double.parseDouble(tmxData.getFcstValue()) : 0;

    WeatherApiData tmnData = categoryMap.get("TMN");
    Double tmn = (tmnData != null) ? Double.parseDouble(tmnData.getFcstValue()) : 0;

    return Weather.builder()
        .location(location)
        .forecastedAt(helper.toInstant(baseDate, categoryMap.get("REH").getId().getBaseTime()))
        .forecastAt(helper.toInstant(fcstDate, "1100"))
        .skyStatus(SkyStatus.fromCode(Integer.parseInt(categoryMap.get("SKY").getFcstValue())))
        .precipitation(
            Precipitation.builder()
                .type(PrecipitationsType.fromCode(categoryMap.get("PTY").getFcstValue()))
                .amount(helper.parsePrecipitation(categoryMap.get("PCP").getFcstValue()))
                .probability(Double.parseDouble(categoryMap.get("POP").getFcstValue()))
                .build()
        )
        .humidity(
            Humidity.builder()
                .current(currentHumidity)
                .comparedToDayBefore(comparedHumidity)
                .build()
        )
        .temperature(
            Temperature.builder()
                .current(currentTemperature)
                .comparedToDayBefore(comparedTemperature)
                .min(tmn)
                .max(tmx)
                .build()
        )
        .windSpeed(
            WindSpeed.builder()
                .speed(helper.parseDoubleOrNull(categoryMap.get("WSD").getFcstValue()))
                .build()
        )
        .build();
  }

}
