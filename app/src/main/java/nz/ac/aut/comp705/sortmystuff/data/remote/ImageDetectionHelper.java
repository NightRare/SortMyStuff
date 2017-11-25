package nz.ac.aut.comp705.sortmystuff.data.remote;

import android.os.Handler;

import javax.inject.Inject;

import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.CloudSightResult;
import nz.ac.aut.comp705.sortmystuff.data.models.ICloudSightResult;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageData;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImageDetectionHelper implements IImageDetectionHelper {

    // Request example
    //    curl -X POST \
    //    https://api.cloudsight.ai/v1/images \
    //            -H 'authorization: CloudSight YOUR_API_KEY' \
    //            -H 'cache-control: no-cache' \
    //            -H 'content-type: application/json' \
    //            -d '{
    //            "image": "data:image/png;base64,R0lG0dJDAhgbn...etc",
    //            "locale": "en"
    //}

    private final static String BODY_IMAGEPREFIX = "data:image/" + BitmapHelper.IMAGE_FORMAT + ";base64,";
    private final static String CLOUDSIGHT_ROOT = "https://api.cloudsight.ai/v1/";
    private final int MAX_TRY = 5;

    @Inject
    public ImageDetectionHelper(
            LocalResourceLoader localResourceLoader) {

        mAuthValue = "CloudSight " + localResourceLoader.getCloudSightApiKey();
        mClient = new Retrofit.Builder()
                .baseUrl(CLOUDSIGHT_ROOT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CloudSightClient.class);
    }

    @Override
    public Observable<ICloudSightResult> result(String base64EncodedData) {
        checkNotNull(base64EncodedData, "The Base64 encoded data string cannot be null");

        return Observable.create((Action1<Emitter<CloudSightResult>>) emitter -> {
            Callback<CloudSightResult> callback = new Callback<CloudSightResult>() {
                @Override
                public void onResponse(Call<CloudSightResult> call, Response<CloudSightResult> response) {
                    Callback<CloudSightResult> emitResult = new Callback<CloudSightResult>() {
                        @Override
                        public void onResponse(
                                Call<CloudSightResult> call,
                                Response<CloudSightResult> response) {
                            emitter.onNext(response.body());
                        }

                        @Override
                        public void onFailure(
                                Call<CloudSightResult> call,
                                Throwable t) {
                            emitter.onError(t);
                        }
                    };

                    getDetectionResult(call, response, emitResult, MAX_TRY);
                }

                @Override
                public void onFailure(Call<CloudSightResult> call, Throwable t) {
                    emitter.onError(t);
                }
            };

            ImageData imageData = new ImageData(
                    BODY_IMAGEPREFIX + base64EncodedData,
                    "en");

            mClient.send(
                    mAuthValue,
                    "no-cache",
                    "application/json",
                    imageData)
                    .enqueue(callback);

        }, Emitter.BackpressureMode.BUFFER)
                .map(cloudSightResult -> (ICloudSightResult) cloudSightResult);
    }

    @Override
    public void cancelRequest() {
        // TODO: to be implemented
    }

    //region PRIVATE STUFF

    private synchronized void getDetectionResult(
            Call<CloudSightResult> call,
            Response<CloudSightResult> response,
            Callback<CloudSightResult> callback,
            int maxTry) {

        CloudSightResult result = response.body();
        if (result == null || maxTry <= 0 || result.status.equals("completed")) {
            callback.onResponse(call, response);
            return;
        }

        new Handler().postDelayed(() -> {
            mClient.getResult(result.token, mAuthValue)
                    .enqueue(new Callback<CloudSightResult>() {
                        @Override
                        public void onResponse(Call<CloudSightResult> call, Response<CloudSightResult> response) {
                            getDetectionResult(call, response, callback, maxTry - 1);
                        }

                        @Override
                        public void onFailure(Call<CloudSightResult> call, Throwable t) {
                            callback.onFailure(call, t);
                        }
                    });
            // cloud sight only allows a max request rate of 20/min
            // so 3000 ms is the most frequent request rate for 1 api key
        }, 3000);
    }

    private CloudSightClient mClient;

    private String mAuthValue;

    //endregion
}
