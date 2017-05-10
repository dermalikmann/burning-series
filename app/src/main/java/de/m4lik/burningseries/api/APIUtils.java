package de.m4lik.burningseries.api;

import android.content.Context;

import java.util.List;

import de.m4lik.burningseries.util.Settings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by malik on 09.05.17.
 */

public class APIUtils {

    private API api;
    private APIInterface apiInterface;
    private Context context;

    private APIUtils(API api, Context context) {
        this.context = context;
        this.api = api;
        this.apiInterface = api.getInterface();
    }

    public static APIUtils with(Context context) {
        return new APIUtils(new API(), context);
    }

    public void sendFavorites(List<Integer> favList) {
        StringBuilder favsSB = new StringBuilder();
        String prefix = "";
        for (Integer fav : favList) {
            favsSB.append(prefix);
            prefix = ",";
            favsSB.append(fav.toString());
        }

        String favs = favsSB.toString();

        api.setSession(Settings.of(context).getUserSession());
        api.generateToken("user/series/set/" + favs);
        Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
