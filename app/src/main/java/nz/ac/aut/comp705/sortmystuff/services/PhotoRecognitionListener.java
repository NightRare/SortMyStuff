package nz.ac.aut.comp705.sortmystuff.services;

import java.util.List;

public interface PhotoRecognitionListener {

    void onProgress(int progress);

    void onSucceeded(List<IPhotoRecognitionResult> results);

    void onFailed(Throwable throwable);
}


