package nz.ac.aut.comp705.sortmystuff;

import android.support.annotation.NonNull;

public interface ISortMyStuffAppComponent {

    void setFeatureToggle(@NonNull Features featureToggle);

    @NonNull
    Features getFeatureToggle();
}
