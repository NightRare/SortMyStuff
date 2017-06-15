package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;
import android.graphics.Bitmap;

import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.util.DemoDebugger;

/**
 * Created by Yuan on 2017/4/25.
 */

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        factory = new Factory(this);

        prepareDemoData();
    }

    public IFactory getFactory() {
        return factory;
    }

    private void prepareDemoData() {
        IDataManager dm = factory.getDataManager();
        Map<String, Bitmap> photos = factory.getLocalResourceLoader().getDemoPhotos();

        DemoDebugger dd = new DemoDebugger(this);
        dd.cleanExistingData();

        Asset root = dm.getRootAsset();

        String studyRoomId = dm.createAsset("Study Room", root.getId(), CategoryType.Places);
        dd.setPhoto(studyRoomId, photos.get("StudyRoom.png"));

        String bookshelfPhilosophyId = dm.createAsset("Bookshelf Philosophy", studyRoomId, CategoryType.Miscellaneous);
        dd.setPhoto(bookshelfPhilosophyId, photos.get("BookshelfPhilosophy.png"));

        String theEssentialHusserlId = dm.createAsset("The Essential Husserl", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(theEssentialHusserlId, photos.get("TheEssentialHusserl.png"));

        String theRepublicId = dm.createAsset("The Republic", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(theRepublicId, photos.get("TheRepublic.png"));

        String beingAndTimeId = dm.createAsset("Being and Time", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(beingAndTimeId, photos.get("BeingAndTime.png"));
    }


}
