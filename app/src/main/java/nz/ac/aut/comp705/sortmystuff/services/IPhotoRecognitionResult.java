package nz.ac.aut.comp705.sortmystuff.services;

import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;

public interface IPhotoRecognitionResult {

    IAsset asset();

    String errorMessage();

    boolean failed();
}
