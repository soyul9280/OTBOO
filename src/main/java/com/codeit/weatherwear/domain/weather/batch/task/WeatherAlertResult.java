package com.codeit.weatherwear.domain.weather.batch.task;

import java.util.List;
import lombok.Builder;

@Builder
public record WeatherAlertResult(boolean alertNeeded, List<WeatherAlertReason> reasons) {

}
