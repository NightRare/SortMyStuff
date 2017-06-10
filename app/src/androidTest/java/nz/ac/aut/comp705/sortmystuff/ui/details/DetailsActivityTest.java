package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by Jing on 2017/6/11.
 */

@RunWith(AndroidJUnit4.class)
public class DetailsActivityTest {

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        app = (SortMyStuffApp) context.getApplicationContext();
        activity = mActivityRule.getActivity();
        dm = app.getFactory().getDataManager();
    }

    @After
    public void tearDown() {
        File userDir = new File(
                app.getFilesDir().getPath() + File.separator + "default-user");

        try {
            FileUtils.cleanDirectory(userDir);
            dm.refreshFromLocal();
        } catch (IOException e) {
            e.printStackTrace();
        }

        context = null;
        app = null;
        activity = null;
        dm = null;
    }

    @Rule
    public ActivityTestRule<ContentsActivity> mActivityRule
            = new ActivityTestRule<>(ContentsActivity.class);

    /* Instantiate an IntentsTestRule object.*/
    @Rule
    public IntentsTestRule<DetailsActivity> mIntentsRule =
            new IntentsTestRule<>(DetailsActivity.class, true, false);


    @Test
    public void photo_addPhotoTest() {
        addAsset(ASSET_LIVING_ROOM);
        dm.getAllAssetsAsync(new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                for(Asset a : assets) {
                    if(a.getName().equals(ASSET_LIVING_ROOM)) {
                        launchIntentTest(a);
                        break;
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    //region PRIVATE STUFF

    private Context context;
    private SortMyStuffApp app;
    private Activity activity;
    private IDataManager dm;

    private static final String ASSET_LIVING_ROOM = "Living Room";
    private static final String ACTION_NAME = "android.media.action.IMAGE_CAPTURE";

    private void addAsset(String assetName) {
        onView(withId(R.id.addAssetButton)).perform(click());
        onView(allOf(is(instanceOf(EditText.class)), withText(is(""))))
                .perform(replaceText(assetName));
        onView(withText("Save")).perform(click());
    }

    private void clickAsset(int num) {
        onData(anything()).inAdapterView(withId(R.id.index_list))
                .atPosition(num).perform(click());
    }

    private void clickPhoto() {
        onData(anything()).inAdapterView(withId(R.id.detail_list))
                .atPosition(0).perform(click());
    }

    private void launchIntentTest(Asset asset) {
        Intent intent = new Intent(mActivityRule.getActivity(), DetailsActivity.class);
        intent.putExtra("AssetID", asset.getId());
        mIntentsRule.launchActivity(intent);

        Uri imageUri = Uri.parse("android.resource://nz.ac.aut.comp705.sortmystuff/drawable/placeholder2.png");

        // Build a result to return from the Camera app
        Intent resultData = new Intent();
        resultData.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
        // with the ActivityResult we just created
        intending(hasAction(ACTION_NAME)).respondWith(result);

        // Now that we have the stub in place, click on the button in our app that launches into the Camera
        clickPhoto();

        // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
        intended(hasAction(ACTION_NAME));

    }




    //endregion
}
