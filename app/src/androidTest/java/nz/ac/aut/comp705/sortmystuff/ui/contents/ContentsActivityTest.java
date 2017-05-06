package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

/**
 * Created by Yuan on 2017/5/6.
 */

@RunWith(AndroidJUnit4.class)
public class ContentsActivityTest {

    public ContentsActivityTest() {
    }

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        app = (SortMyStuffApp) context.getApplicationContext();
        activity = contentsActivityActivityTestRule.getActivity();
        dm = app.getFactory().getDataManager();
    }

    @After
    public void tearDown() {
        File userDir = new File(
                app.getFilesDir().getPath() + File.separator + "default-user");

        try {
            FileUtils.cleanDirectory(userDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        context = null;
        app = null;
        activity = null;
        dm = null;
    }

    @Rule
    public ActivityTestRule<ContentsActivity> contentsActivityActivityTestRule
            = new ActivityTestRule<ContentsActivity>(ContentsActivity.class);

    @Test
    public void onLaunch_displayRootAssetTitle() {
        onView(withId(R.id.toolbarMain)).check(matches(isDisplayed()));

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        Assert.assertTrue(toolbar.getTitle().equals(ROOT_ASSET_NAME));
    }

    @Test
    public void onLaunch_displayRootOnPathBar() {
        onView(withId(R.id.pathbar_root)).check(matches(isDisplayed()));
        onView(withId(R.id.pathbar_root)).check(matches(withText(ROOT_ASSET_NAME + " >")));
    }

    @Test
    public void clickAddAssetButton() {
        onView(withId(R.id.addAssetButton)).check(matches(isDisplayed()));
        onView(withId(R.id.addAssetButton)).perform(click());
        onView(withText("Add Asset")).check(matches(isDisplayed()));
    }

    @Test
    public void addAsset_inputNameAndConfirm() {
        addAsset(ASSET1_NAME);

        onView(withId(R.id.index_list)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.index_list))
                .atPosition(0).onChildView(withId(R.id.asset_name))
                .check(matches(withText(ASSET1_NAME)));
    }

    @Test
    public void addAsset_cancelDialogBox(){
        onView(withId(R.id.addAssetButton)).perform(click());
        //type asset name into text area in dialog box
        onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(ASSET1_NAME));
        //click on cancel button in dialog box
        onView(withText("Cancel")).perform(click());
        //asset should not be added
        Assert.assertFalse(onData(anything()).inAdapterView(withId(R.id.index_list)).atPosition(0).equals(ASSET1_NAME));
    }

    @Test
    public void onClickAssetFromList_displayAssetNameOnToolbar(){
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withId(R.id.toolbarMain)).check(matches(isDisplayed()));
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        //check that the name on the toolbar is the name of the selected asset
        Assert.assertTrue(toolbar.getTitle().toString().equals(ASSET1_NAME));
    }

    @Test
    public void onClickAssetFromList_addChildAsset(){
        addAsset(ASSET1_NAME);
        clickAsset(0);
        addAsset(ASSET2_NAME);
        onView(withId(R.id.index_list)).check(matches(isDisplayed()));
        //check that assets can added into child assets
        onData(anything()).inAdapterView(withId(R.id.index_list))
                .atPosition(0).onChildView(withId(R.id.asset_name))
                .check(matches(withText(ASSET2_NAME)));
    }


    @Test
    public void displayPathbar() {
        // add asset1 and goes into asset1
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withId(R.id.pathbar_pathview))
                .check(matches(withTextViewInPathBarAtPosition(
                        0, withText(PATH_BAR_0_PREFIX + ASSET1_NAME))));

        // add asset2 and goes into asset2, so path should be asset1 > asset2
        addAsset(ASSET2_NAME);
        clickAsset(0);
        onView(withId(R.id.pathbar_pathview))
                .check(matches(withTextViewInPathBarAtPosition(
                        1, withText(PATH_BAR_PREFIX + ASSET2_NAME))));
    }


    //region PRIVATE STUFF

    private Context context;

    private SortMyStuffApp app;

    private Activity activity;

    private IDataManager dm;

    private static final String ROOT_ASSET_NAME = "Root";
    private static final String ASSET1_NAME = "ASSET1_NAME";
    private static final String ASSET2_NAME = "ASSET2_NAME";
    private static final String PATH_BAR_0_PREFIX = "  ";
    private static final String PATH_BAR_PREFIX = " >  ";

    private void addAsset(String assetName) {
        onView(withId(R.id.addAssetButton))
                .perform(click());
        onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(assetName));
        onView(withText("Save")).perform(click());
    }

    private void clickAsset(int num) {
        onData(anything()).inAdapterView(withId(R.id.index_list))
                .atPosition(num).perform(click());
    }

    private static Matcher<View> withTextViewInPathBarAtPosition(final int position, final Matcher<View> itemMatcher) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                PathBarAdapter.ViewHolder viewHolder = (PathBarAdapter.ViewHolder) view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.getNameView());
            }
        };
    }

    //endregion

}
