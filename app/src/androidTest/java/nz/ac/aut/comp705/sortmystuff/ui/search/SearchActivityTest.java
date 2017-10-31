package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.ac.aut.comp705.sortmystuff.ui.testutils.TestConfigs.TEST_USER_EMAIL;
import static nz.ac.aut.comp705.sortmystuff.ui.testutils.TestConfigs.TEST_USER_PASSWORD;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInWithEmailAndPassword(TEST_USER_EMAIL, TEST_USER_PASSWORD)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            Log.e(TAG, "signInWithEmail:failure", task.getException());
                        }

                        if (!task.isSuccessful()) {
                            Log.e(TAG, "signInWithEmail:failure", task.getException());
                        }
                    });
        }
    }

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        mApp = (SortMyStuffApp) mContext.getApplicationContext();
        if (mApp.getFactory() == null)
            mApp.initialiseFactory(FirebaseAuth.getInstance().getCurrentUser());
        mFactory = mApp.getFactory();
        mDataManager = mFactory.getDataManager();

        // remove current user data
        mFactory.getDataDebugHelper().removeCurrentUserData();

        Intent intent = new Intent(mApp, SwipeActivity.class);
        mActivity = swipeActivityRule.launchActivity(intent);

        addAsset(ONE_RESULT_SEARCH_ITEM);
        addAsset(TWO_RESULT_SEARCH_ITEM_1);
        addAsset(TWO_RESULT_SEARCH_ITEM_2);
        onView(withId(R.id.search_view_button)).perform(click());
    }

    @After
    public void tearDown() {
        mActivity.finish();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        FirebaseAuth.getInstance().signOut();
    }

    @Rule
    public ActivityTestRule<SwipeActivity> swipeActivityRule
            = new ActivityTestRule<>(SwipeActivity.class, false, false);

    //region TESTS

    @Test
    public void loadSearch_checkComponents() {
        //check if Search UI components (toolbar, and search bar, search button) are there
        onView(withId(R.id.search_toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void search_oneResult() {
        search(ONE_RESULT_SEARCH_ITEM);
        //Orange should display in search results
        onData(anything()).inAdapterView(withId(R.id.result_list))
                .atPosition(0).onChildView(withId(R.id.search_result_title))
                .check(matches(withText(ONE_RESULT_SEARCH_ITEM)));
    }

    @Test
    public void search_multipleResults() {
        search(TWO_RESULT_SEARCH_PREFIX);
        //there should be two items in result list: Apple and Apricot

        onView(withChild(withText(TWO_RESULT_SEARCH_ITEM_1))).check(matches(isDisplayed()));
        onView(withChild(withText(TWO_RESULT_SEARCH_ITEM_2))).check(matches(isDisplayed()));
    }

    @Test
    public void search_noResult() {
        search(NO_RESULT_SEARCH_KEYWORD);
        //No results shown, result list should be empty
        onView(withId(R.id.result_list)).check(matches(withListSize(0)));
    }

    @Test
    public void search_noKeyword() {
        search("");
        //Result list should be empty
        onView(withId(R.id.result_list)).check(matches(withListSize(0)));
    }

    //endregion

    //region PRIVATE STUFF

    private void addAsset(String assetName) {
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(allOf(withClassName(endsWith("EditText")), withText(is(""))))
                .perform(replaceText(assetName));
        onView(withText(R.string.add_asset_confirm_button)).perform(click());
    }

    private void search(String keyword) {
        onView(isAssignableFrom(EditText.class)).perform(typeText(keyword));
        // wait for the animation
        SystemClock.sleep(500);
    }

    private static Matcher<View> withListSize(final int size) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(final View view) {
                return ((ListView) view).getCount() == size;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("ListView should have " + size + " items");
            }
        };
    }

    private Context mContext;
    private SortMyStuffApp mApp;
    private Activity mActivity;
    private IDataManager mDataManager;
    private IFactory mFactory;

    private static final String TAG = "SearchActivity";

    private static final String ONE_RESULT_SEARCH_ITEM = "Orange";
    private static final String TWO_RESULT_SEARCH_PREFIX = "Ap";
    private static final String TWO_RESULT_SEARCH_ITEM_1 = TWO_RESULT_SEARCH_PREFIX + "ple";
    private static final String TWO_RESULT_SEARCH_ITEM_2 = TWO_RESULT_SEARCH_PREFIX + "ricot";
    private static final String NO_RESULT_SEARCH_KEYWORD = "Ba";

    //endregion
}
