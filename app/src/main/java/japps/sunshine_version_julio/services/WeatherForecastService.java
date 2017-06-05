package japps.sunshine_version_julio.services;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Julio on 4/6/2017.
 */

public interface WeatherForecastService {
    @GET("data/2.5/forecast/daily")
    Call<String> requestWeatherForecast(
            @Query("q") String city,
            @Query("units") String units,
            @Query("cnt") String days,
            @Query("lang") String lang,
            @Query("APPID") String appID
    );
}
