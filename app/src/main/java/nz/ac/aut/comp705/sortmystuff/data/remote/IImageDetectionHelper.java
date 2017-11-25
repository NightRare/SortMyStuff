package nz.ac.aut.comp705.sortmystuff.data.remote;

import nz.ac.aut.comp705.sortmystuff.data.models.ICloudSightResult;
import rx.Observable;

public interface IImageDetectionHelper extends IApiHelper{

    Observable<ICloudSightResult> result(String base64EncodedData);
}
