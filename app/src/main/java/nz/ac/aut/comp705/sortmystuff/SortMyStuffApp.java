package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.SystemClock;

import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.utils.DemoDebugger;

import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        factory = new Factory(this, "default-user");

//        prepareDemoData();
    }

    public IFactory getFactory() {
        return factory;
    }

    private void prepareDemoData() {
        IDataManager dm = factory.getDataManager();
        Map<String, Bitmap> photos = factory.getLocalResourceLoader().getDemoPhotos();

        DemoDebugger dd = new DemoDebugger(factory);
        dd.cleanExistingData();

        String studyRoomId = dm.createAsset("Study Room", ROOT_ASSET_ID, CategoryType.Places);
        dd.setPhoto(studyRoomId, photos.get("StudyRoom.jpg"));

        String office = dm.createAsset("Office", ROOT_ASSET_ID, CategoryType.Places);
        dd.setPhoto(office, photos.get("Office.jpg"));

        String bedroomId = dm.createAsset("Bedroom", ROOT_ASSET_ID, CategoryType.Places);
        dd.setPhoto(bedroomId, photos.get("Bedroom.jpg"));

        String bookshelfPhilosophyId = dm.createAsset("Bookshelf Philosophy", studyRoomId, CategoryType.Miscellaneous);
        dd.setPhoto(bookshelfPhilosophyId, photos.get("BookshelfPhilosophy.jpg"));

        String bookshelfLiteratureId = dm.createAsset("Bookshelf Literature", studyRoomId, CategoryType.Miscellaneous);
        dd.setPhoto(bookshelfLiteratureId, photos.get("BookshelfLiterature.jpg"));

        String kindleId = dm.createAsset("Kindle", bedroomId, CategoryType.Appliances);
        dd.setPhoto(kindleId, photos.get("Kindle.jpg"));
        dd.updateTextDetail(kindleId, "Purchase Date", "14/01/2016");
        dd.updateTextDetail(kindleId, "Warranty Expiry", "14/01/2019");
        dd.updateTextDetail(kindleId, "Model Number", "B0186FET66");
        dd.updateTextDetail(kindleId, "Serial Number", "9Q8EWR7923");

        String theEssentialHusserlId = dm.createAsset("The Essential Husserl", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(theEssentialHusserlId, photos.get("TheEssentialHusserl.jpg"));

        String theRepublicId = dm.createAsset("The Republic", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(theRepublicId, photos.get("TheRepublic.jpg"));

        String beingAndTimeId = dm.createAsset("Being and Time", bookshelfPhilosophyId, CategoryType.Books);
        dd.setPhoto(beingAndTimeId, photos.get("BeingAndTime.jpg"));

        SystemClock.sleep(1000);
    }


}
