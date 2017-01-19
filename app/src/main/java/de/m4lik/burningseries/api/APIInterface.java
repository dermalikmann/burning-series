package de.m4lik.burningseries.api;

import java.util.List;

import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.GenreMap;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.api.objects.ShowObj;
import de.m4lik.burningseries.api.objects.VideoObj;
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
 * Interface for the API
 * @author Malik Mann
 */

public interface APIInterface {

    /**
     * API call for an alphabetically sorted list of ShowObjects
     * @param token
     * @param userAgent
     * @param session
     * @return showObjectList
     */
    @GET("/api/series")
    Call<List<ShowObj>> getSeriesAlphaList(@Header("BS-Token") String token,
                                           @Header("User-Agent") String userAgent,
                                           @Query("s") String session);

    /**
     * API call which returns a map of genre objects.
     * @param token
     * @param userAgent
     * @param session
     * @return genreMap
     */
    @GET("/api/series:genre")
    Call<GenreMap> getSeriesGenreList(@Header("BS-Token") String token,
                                      @Header("User-Agent") String userAgent,
                                      @Query("s") String session);

    /**
     * API call for a specific season of an specific show.
     * @param token
     * @param userAgent
     * @param id
     * @param season
     * @param session
     * @return seasonObject
     */
    @GET("/api/series/{id}/{season}")
    Call<SeasonObj> getSeason(@Header("BS-Token") String token,
                              @Header("User-Agent") String userAgent,
                              @Path("id") Integer id,
                              @Path("season") Integer season,
                              @Query("s") String session);

    /**
     * API call for an specific episode of a specific season of a specific show.
     * @param token
     * @param userAgent
     * @param id
     * @param season
     * @param episode
     * @param session
     * @return episodeObject
     */
    @GET("/api/series/{id}/{season}/{episode}")
    Call<EpisodeObj> getEpisode(@Header("BS-Token") String token,
                                @Header("User-Agent") String userAgent,
                                @Path("id") Integer id,
                                @Path("season") Integer season,
                                @Path("episode") Integer episode,
                                @Query("s") String session);

    /**
     * API call for an video object (mark an episode as watched an return the
     * link to the selected hoster).
     * @param token
     * @param userAgent
     * @param id
     * @param session
     * @return videoObject
     */
    @GET("/api/watch/{id}")
    Call<VideoObj> watch(@Header("BS-Token") String token,
                         @Header("User-Agent") String userAgent,
                         @Path("id") Integer id,
                         @Query("s") String session);

    /**
     * API call to mark a givin episode as not watched.
     * @param token
     * @param userAgent
     * @param id
     * @param session
     * @return
     */
    @GET("/api/unwatch/{id}")
    Call<VideoObj> unwatch(@Header("BS-Token") String token,
                           @Header("User-Agent") String userAgent,
                           @Path("id") Integer id,
                           @Query("s") String session);

    /**
     * API call for a list of shows that the user marked as favorites.
     * @param token
     * @param userAgent
     * @param session
     * @return showObjectList
     */
    @GET("/api/user/series")
    Call<List<ShowObj>> getFavorites(@Header("BS-Token") String token,
                                     @Header("User-Agent") String userAgent,
                                     @Query("s") String session);

    /**
     * API call to set the User's favorites.
     * @param token
     * @param userAgent
     * @param ids
     * @param session
     * @return
     */
    @GET("/api/user/series/set/{ids}")
    Call<ResponseBody> setFavorites(@Header("BS-Token") String token,
                                    @Header("User-Agent") String userAgent,
                                    @Path("ids") String ids,
                                    @Query("s") String session);

    /**
     * API call to try to log in the user.
     * @param token
     * @param userAgent
     * @param userName
     * @param password
     * @return returnString
     */
    @FormUrlEncoded
    @POST("/api/login")
    Call<ResponseBody> login(@Header("BS-Token") String token,
                             @Header("User-Agent") String userAgent,
                             @Field("login[user]") String userName,
                             @Field("login[pass]") String password);

    /**
     * API call to try to log out the user.
     * @param token
     * @param userAgent
     * @param session
     * @return returnString
     */
    @GET("/api/logout")
    Call<ResponseBody> logout(@Header("BS-Token") String token,
                              @Header("User-Agent") String userAgent,
                              @Query("s") String session);
}