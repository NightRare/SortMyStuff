package nz.ac.aut.comp705.sortmystuff.util.exceptions;

public class UpdateLocalStorageFailedException extends IllegalStateException {

    public UpdateLocalStorageFailedException(String msg) {
        super(msg);
    }

    public UpdateLocalStorageFailedException() {
        super();
    }
}
