package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

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

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsActivity;
import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * Created by Donna on 28 May 2017.
 */

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        app = (SortMyStuffApp) context.getApplicationContext();
        activity = contentsActivityTestRule.getActivity();
        dm = app.getFactory().getDataManager();
        addAsset("Apple");
        addAsset("Orange");
        addAsset("Apricot");
        Espresso.onView(withId(R.id.search_view)).perform(click());
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
    public ActivityTestRule<ContentsActivity> contentsActivityTestRule
            = new ActivityTestRule<>(ContentsActivity.class);

    @Test
    public void loadSearch_checkComponents(){
        //check if Search UI components (toolbar, and search bar, search button) are there
        onView(withId(R.id.search_toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void search_Orange(){
        search("Orange");
        //Orange should display in search results
        onData(anything()).inAdapterView(withId(R.id.result_list))
                .atPosition(0).check(matches(withText("Orange")));
    }

    @Test
    public void search_twoItems(){
        search("Ap");
        //there should be two items in result list: Apple and Apricot
        onView (withId (R.id.result_list)).check (ViewAssertions.matches (withListSize(2)));
    }

    @Test
    public void search_noResult(){
        search("Ba");
        //No results shown, result list should be empty
        onView (withId (R.id.result_list)).check(ViewAssertions.matches(withListSize(0)));
    }

    @Test
    public void search_noKeyword(){
        search("");
        //Result list should be empty
        onView (withId (R.id.result_list)).check(ViewAssertions.matches(withListSize(0)));
    }


    private Context context;

    private SortMyStuffApp app;

    private Activity activity;

    private IDataManager dm;

    private void addAsset(String assetName) {
        onView(withId(R.id.addAssetButton)).perform(click());
        onView(allOf(withClassName(endsWith("EditText")), withText(is(""))))
                .perform(replaceText(assetName));
        onView(withText("Save")).perform(click());
    }

    private void search(String keyword){
        onView(isAssignableFrom(EditText.class)).perform(typeText(keyword));
    }

    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((ListView) view).getCount () == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("ListView should have " + size + " items");
            }
        };
    }
}
