package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dagger.Module;
import dagger.Provides;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.AppResDatabaseRef;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.UserDatabaseRef;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

@Module
public class FactoryModule {

    private static final String USER_DATA = "user_data";
    private static final String APP_RESOURCES = "app_resources";

    private final Application mApp;
    private String mUserId;

    public FactoryModule (Application app, String userId) {
        mApp = app;
        mUserId = userId;
    }

    public void setUserId(String userId) {
        mUserId = checkNotNull(userId);
    }

    @Provides
    public Application application() {
        return mApp;
    }

    @Provides
    @UserDatabaseRef
    public DatabaseReference userDatabaseRef() {
        return FirebaseDatabase.getInstance().getReference().child(USER_DATA).child(mUserId);
    }

    @Provides
    @AppResDatabaseRef
    public DatabaseReference appResDatabaseRef() {
        return FirebaseDatabase.getInstance().getReference().child(APP_RESOURCES);
    }

    @Provides
    public StorageReference storageReference() {
        return FirebaseStorage.getInstance().getReference().child(USER_DATA).child(mUserId);
    }
}
