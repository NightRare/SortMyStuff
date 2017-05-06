package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.common.base.Preconditions;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
    public void displayPathbar_lengthWithinScreen() {
        // add asset1 and goes into asset1
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .check(matches(isDisplayed()));

        // add asset2 and goes into asset2, so path should be asset1 > asset2
        addAsset(ASSET2_NAME);
        clickAsset(0);
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + ASSET2_NAME))
                .check(matches(isDisplayed()));

        // Root asset should always display
        onView(withId(R.id.pathbar_root))
                .check(matches(isDisplayed()));
    }

    @Test
    public void displayPathbar_lengthExceedsScreen() throws InterruptedException {
        List<String> names = new ArrayList<>(Arrays.asList(
                ASSET_NAME + 0, ASSET_NAME + 1, ASSET_NAME + 2, ASSET_NAME + 3, ASSET_NAME + 4
        ));

        for(int i = 0; i < names.size(); i++) {
            addAsset(names.get(i));
            clickAsset(0);
        }

        // the path bar shall not display the first added asset as the list is too long
        // while the last asset shall be displayed
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + names.get(0)))
                .check(doesNotExist());
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + names.get(4)))
                .check(matches(isDisplayed()));

        // Root asset should also be displayed
        onView(withId(R.id.pathbar_root))
                .check(matches(isDisplayed()));

        // swipe the path bar to the first asset, then the first asset shall be displayed
        onView(withId(R.id.pathbar_pathview))
                .perform(RecyclerViewActions.scrollToPosition(0));
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + names.get(0)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickOnPathbarRootAsset() {
        // add asset1 and goes into asset1
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .check(matches(isDisplayed()));

        // make sure its directed to asset1's contents view
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        Assert.assertTrue(toolbar.getTitle().equals(ASSET1_NAME));

        onView(withId(R.id.pathbar_root)).perform(click());

        // after click on Root asset on path bar
        // should direct back to Root's contents view and asset1 removed from path bar
        Assert.assertTrue(toolbar.getTitle().equals(ROOT_ASSET_NAME));
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .check(doesNotExist());
    }

    @Test
    public void clickOnPathbarAsset_lengthWithinScreen() {
        // add asset1 and goes into asset1
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .check(matches(isDisplayed()));

        addAsset(ASSET2_NAME);
        clickAsset(0);
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + ASSET2_NAME))
                .check(matches(isDisplayed()));

        // make sure its directed to asset2's contents view
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        Assert.assertTrue(toolbar.getTitle().equals(ASSET2_NAME));

        // click on asset1 while in asset2's contents view
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .perform(click());

        // should direct back to asset1's contents view and asset2 removed from path bar
        Assert.assertTrue(toolbar.getTitle().equals(ASSET1_NAME));
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + ASSET2_NAME))
                .check(doesNotExist());
    }

    @Test
    public void clickOnPathbarAsset_lengthExceedsScreen() {
        List<String> names = new ArrayList<>(Arrays.asList(
                ASSET_NAME + 0, ASSET_NAME + 1, ASSET_NAME + 2, ASSET_NAME + 3, ASSET_NAME + 4
        ));

        for(int i = 0; i < names.size(); i++) {
            addAsset(names.get(i));
            clickAsset(0);
        }

        // make sure its directed to asset2's contents view
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        Assert.assertTrue(toolbar.getTitle().equals(names.get(4)));

        // swipe the path bar to the first asset (asset0), then the first asset shall be displayed
        onView(withId(R.id.pathbar_pathview))
                .perform(RecyclerViewActions.scrollToPosition(0));
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + names.get(0)))
                .perform(click());

        // should direct back to asset0's contents view and all other assets
        // should be removed from path bar
        toolbar = (Toolbar) activity.findViewById(R.id.toolbarMain);
        Assert.assertTrue(toolbar.getTitle().equals(names.get(0)));
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + names.get(1)))
                .check(doesNotExist());
    }


    //region PRIVATE STUFF

    private Context context;

    private SortMyStuffApp app;

    private Activity activity;

    private IDataManager dm;

    private static final String ROOT_ASSET_NAME = "Root";
    private static final String ASSET_NAME = "ASSET_NAME";
    private static final String ASSET1_NAME = "ASSET1_NAME";
    private static final String ASSET2_NAME = "ASSET2_NAME";
    private static final String PATH_BAR_0_PREFIX = "  ";
    private static final String PATH_BAR_PREFIX = " >  ";

    private void addAsset(String assetName) {
        onView(withId(R.id.addAssetButton)).perform(click());
        onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(assetName));
        onView(withText("Save")).perform(click());
    }

    private void clickAsset(int num) {
        onData(anything()).inAdapterView(withId(R.id.index_list))
                .atPosition(num).perform(click());
    }

    private static Matcher<View> withItemTextOnPathBar(final String itemText) {
        Preconditions.checkArgument(!itemText.isEmpty(), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(isDescendantOfA(withId(R.id.pathbar_layout)), withText(itemText))
                        .matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA Pathbar layout with text " + itemText);
            }
        };
    }

    @Deprecated
    private static Matcher<View> withPathBarTextViewAtPosition(
            final int position, final Matcher<View> itemMatcher) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                PathBarAdapter.ViewHolder viewHolder = (PathBarAdapter.ViewHolder)
                        view.findViewHolderForAdapterPosition(position);
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
