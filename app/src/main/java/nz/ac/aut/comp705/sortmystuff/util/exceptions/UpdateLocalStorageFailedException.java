package nz.ac.aut.comp705.sortmystuff.util.exceptions;

import java.io.IOException;

/**
 * Created by Vince on 2017/4/26.
 */

public class UpdateLocalStorageFailedException extends IllegalStateException {

    public UpdateLocalStorageFailedException(String msg) {
        super(msg);
    }

    public UpdateLocalStorageFailedException() {
        super();
    }
}
