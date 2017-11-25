package nz.ac.aut.comp705.sortmystuff.data.remote;

import nz.ac.aut.comp705.sortmystuff.data.models.CloudSightResult;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CloudSightClient {

    @POST("images")
    Call<CloudSightResult> send(
            @Header("authorization") String authHeader,
            @Header("cache-control") String cacheHeader,
            @Header("content-type") String contentTypeHeader,
            @Body ImageData imageData);

    @GET("images/{token}")
    Call<CloudSightResult> getResult(
            @Path("token") String token,
            @Header("authorization") String authHeader
    );
}
