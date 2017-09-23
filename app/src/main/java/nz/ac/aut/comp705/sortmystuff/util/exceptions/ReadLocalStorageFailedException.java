package nz.ac.aut.comp705.sortmystuff.util.exceptions;

public class ReadLocalStorageFailedException extends IllegalStateException {

    public ReadLocalStorageFailedException(String msg) {
        super(msg);
    }

    public ReadLocalStorageFailedException() {
        super();
    }
}
