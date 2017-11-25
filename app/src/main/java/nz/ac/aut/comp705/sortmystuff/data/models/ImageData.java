package nz.ac.aut.comp705.sortmystuff.data.models;

public class ImageData {

    public final String image;
    public final String locale;

    public ImageData(String base64EncodedImageData, String locale) {
        image = base64EncodedImageData;
        this.locale = locale;
    }
}
