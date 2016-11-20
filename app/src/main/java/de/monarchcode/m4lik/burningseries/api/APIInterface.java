package de.monarchcode.m4lik.burningseries.api;

import java.util.List;

import de.monarchcode.m4lik.burningseries.objects.EpisodeObj;
import de.monarchcode.m4lik.burningseries.objects.GenreMap;
import de.monarchcode.m4lik.burningseries.objects.SeasonObj;
import de.monarchcode.m4lik.burningseries.objects.ShowObj;
import de.monarchcode.m4lik.burningseries.objects.VideoObj;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Malik on 01.10.2016.
 */

public interface APIInterface {

    @GET("/api/series")
    Call<List<ShowObj>> getSeriesAlphaList(@Header("BS-Token") String token,
                                           //Call<ResponseBody> getSeriesAlphaList(@Header("BS-Token") String token,
                                           @Header("User-Agent") String userAgent,
                                           @Query("s") String session);

    @GET("/api/series:genre")
    Call<GenreMap> getSeriesGenreList(@Header("BS-Token") String token,
                                      //Call<ResponseBody> getSeriesGenreList(@Header("BS-Token") String token,
                                      @Header("User-Agent") String userAgent,
                                      @Query("s") String session);

    @GET("/api/series:genre")
    Call<ResponseBody> getSeriesJSONString(@Header("BS-Token") String token,
                                      //Call<ResponseBody> getSeriesGenreList(@Header("BS-Token") String token,
                                      @Header("User-Agent") String userAgent,
                                      @Query("s") String session);

    @GET("/api/series/{id}/{season}")
    Call<SeasonObj> getSeason(@Header("BS-Token") String token,
                              //Call<ResponseBody> getSeason(@Header("BS-Token") String token,
                              @Header("User-Agent") String userAgent,
                              @Path("id") Integer id,
                              @Path("season") Integer season,
                              @Query("s") String session);

    @GET("/api/series/{id}/{season}/{episode}")
    Call<EpisodeObj> getEpisode(@Header("BS-Token") String token,
                                //Call<ResponseBody> getEpisode(@Header("BS-Token") String token,
                                @Header("User-Agent") String userAgent,
                                @Path("id") Integer id,
                                @Path("season") Integer season,
                                @Path("episode") Integer episode,
                                @Query("s") String session);

    @GET("/api/watch/{id}")
    Call<VideoObj> watch(@Header("BS-Token") String token,
                         //Call<ResponseBody> watch(@Header("BS-Token") String token,
                         @Header("User-Agent") String userAgent,
                         @Path("id") Integer id,
                         @Query("s") String session);

    @GET("/api/unwatch/{id}")
    Call<VideoObj> unwatch(@Header("BS-Token") String token,
                           //Call<ResponseBody> unwatch(@Header("BS-Token") String token,
                           @Header("User-Agent") String userAgent,
                           @Path("id") Integer id,
                           @Query("s") String session);

    @GET("/api/user/series")
    Call<List<ShowObj>> getFavorites(@Header("BS-Token") String token,
                                     @Header("User-Agent") String userAgent,
                                     @Query("s") String session);
    @GET("/api/user/series")
    Call<ResponseBody> getFavoritesString(@Header("BS-Token") String token,
                                     @Header("User-Agent") String userAgent,
                                     @Query("s") String session);

    @FormUrlEncoded
    @POST("/api/login")
    Call<ResponseBody> login(@Header("BS-Token") String token,
                             @Header("User-Agent") String userAgent,
                             @Field("login[user]") String userName,
                             @Field("login[pass]") String password);

    @GET("/api/logout")
    Call<ResponseBody> logout(@Header("BS-Token") String token,
                              @Header("User-Agent") String userAgent,
                              @Query("s") String session);
}
