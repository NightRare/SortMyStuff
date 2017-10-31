package nz.ac.aut.comp705.sortmystuff.ui.main;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.desmond.squarecamera.CameraActivity;
import com.google.common.base.Preconditions;
import com.google.firebase.auth.FirebaseAuth;

import junit.framework.Assert;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;
import rx.Observable;
import rx.schedulers.Schedulers;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static nz.ac.aut.comp705.sortmystuff.ui.testutils.TestConfigs.TEST_USER_EMAIL;
import static nz.ac.aut.comp705.sortmystuff.ui.testutils.TestConfigs.TEST_USER_ID;
import static nz.ac.aut.comp705.sortmystuff.ui.testutils.TestConfigs.TEST_USER_PASSWORD;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SwipeActivityTest {

    public SwipeActivityTest() {
    }

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
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
        mApp = (SortMyStuffApp) mContext.getApplicationContext();
        if (mApp.getFactory() == null)
            mApp.initialiseFactory(TEST_USER_ID);
        mFactory = mApp.getFactory();
        mDataManager = mFactory.getDataManager();

        // remove current user data
        mFactory.getDataDebugHelper().removeCurrentUserData();

        Intent intent = new Intent(mApp, SwipeActivity.class);
        mActivity = swipeActivityRule.launchActivity(intent);
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

    /* Instantiate an IntentsTestRule object.*/
    @Rule
    public IntentsTestRule<SwipeActivity> intentsRule =
            new IntentsTestRule<>(SwipeActivity.class, true, false);

    //region TESTS

    @Test
    public void onLaunch_displayRootAssetTitle() {
        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()));

        checkToolbarTitle(ROOT_ASSET_NAME);
    }

    @Test
    public void onLaunch_displayRootOnPathBar() {
        onView(withId(R.id.pathbar_root)).check(matches(isDisplayed()));
        onView(withId(R.id.pathbar_root)).check(matches(withText(ROOT_ASSET_NAME + " >")));
    }

    @Test
    public void addAsset_clickAddAssetButton() {
        onView(withId(R.id.add_asset_button)).check(matches(isDisplayed()));
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(withText(R.string.add_asset_dialog_title)).check(matches(isDisplayed()));
    }

    @Test
    public void addAsset_inputNameAndConfirm() {
        addAsset(ASSET1_NAME);

        onView(withId(R.id.assets_list)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).onChildView(withId(R.id.asset_name))
                .check(matches(withText(ASSET1_NAME)));
    }

    @Test
    public void addAsset_cancelDialogBox() {
        onView(withId(R.id.add_asset_button)).perform(click());
        //type asset name into text area in dialog box
        onView(allOf(withClassName(endsWith("EditText")), withText(is("")))).perform(replaceText(ASSET1_NAME));
        //click on cancel button in dialog box
        onView(withText(R.string.cancel_button)).perform(click());
        //asset should not be added
        Assert.assertFalse(onData(anything()).inAdapterView(withId(R.id.assets_list)).atPosition(0).equals(ASSET1_NAME));
    }

    @Test
    public void addAsset_addAssetsWithIllegalNames() {
        addAsset("");
        onView(withId(R.id.assets_list)).check(matches(isDisplayed()));

        String longAssetName = "";
        for (int i = 0; i <= AppConstraints.ASSET_NAME_CAP; i++) {
            longAssetName += "a";
        }
        addAsset(longAssetName);
        onView(withId(R.id.assets_list)).check(matches(isDisplayed()));
        onView(withChild(withText(longAssetName))).check(doesNotExist());
    }

    @Test
    public void addAsset_withDefaultCategory() {
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(allOf(is(instanceOf(EditText.class)), withText(is(""))))
                .perform(replaceText(ASSET1_NAME));

        // check if the category has been selected as the default
        onView(withId(R.id.category_spinner))
                .check(matches(withSpinnerText(containsString(DEFAULT_CATEGORY.toString()))));
        onView(withText(R.string.add_asset_confirm_button)).perform(click());

        clickAsset(0);
        swipeToDetailsPage();
        checkToolbarTitle(ASSET1_NAME);

        // check if the category is displayed correctly
        onView(withId(R.id.assetCategory_detail))
                .check(matches(withText(DEFAULT_CATEGORY.toString().toUpperCase())));

        checkDetailsListOfMiscellaneous();
    }

    @Test
    public void addAsset_withSpecifiedCategory() {
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(allOf(is(instanceOf(EditText.class)), withText(is(""))))
                .perform(replaceText(ASSET1_NAME));
        onView(withId(R.id.category_spinner)).perform(click());

        // select "Food" category
        onData(allOf(is(instanceOf(CategoryType.class)), is(SPECIFIED_CATEGORY)))
                .inRoot(RootMatchers.isPlatformPopup()).perform(click());

        // check if the "Food" has been selected
        onView(withId(R.id.category_spinner))
                .check(matches(withSpinnerText(containsString(SPECIFIED_CATEGORY.toString()))));
        onView(withText(R.string.add_asset_confirm_button)).perform(click());

        clickAsset(0);
        swipeToDetailsPage();
        checkToolbarTitle(ASSET1_NAME);

        // check if the category is displayed correctly
        onView(withId(R.id.assetCategory_detail))
                .check(matches(withText(SPECIFIED_CATEGORY.toString().toUpperCase())));

        checkDetailsListOfFood();
    }

    @Test
    public void onClickAssetFromList_displayAssetNameOnToolbar() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()));
        //check that the name on the toolbar is the name of the selected asset
        checkToolbarTitle(ASSET1_NAME);
    }

    @Test
    public void onClickAssetFromList_addChildAsset() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        addAsset(ASSET2_NAME);
        onView(withId(R.id.assets_list)).check(matches(isDisplayed()));
        //check that assets can added into child assets
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).onChildView(withId(R.id.asset_name))
                .check(matches(withText(ASSET2_NAME)));
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

        for (int i = 0; i < names.size(); i++) {
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
        checkToolbarTitle(ASSET1_NAME);

        onView(withId(R.id.pathbar_root)).perform(click());

        // after click on Root asset on path bar
        // should direct back to Root's contents view and asset1 removed from path bar
        checkToolbarTitle(ROOT_ASSET_NAME);
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
        checkToolbarTitle(ASSET2_NAME);

        // click on asset1 while in asset2's contents view
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + ASSET1_NAME))
                .perform(click());

        // should direct back to asset1's contents view and asset2 removed from path bar
        checkToolbarTitle(ASSET1_NAME);
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + ASSET2_NAME))
                .check(doesNotExist());
    }

    @Test
    public void clickOnPathbarAsset_lengthExceedsScreen() {
        List<String> names = new ArrayList<>(Arrays.asList(
                ASSET_NAME + 0, ASSET_NAME + 1, ASSET_NAME + 2, ASSET_NAME + 3, ASSET_NAME + 4
        ));

        for (int i = 0; i < names.size(); i++) {
            addAsset(names.get(i));
            clickAsset(0);
        }

        // make sure its directed to asset2's contents view
        checkToolbarTitle(names.get(4));

        // swipe the path bar to the first asset (asset0), then the first asset shall be displayed
        onView(withId(R.id.pathbar_pathview))
                .perform(RecyclerViewActions.scrollToPosition(0));
        onView(withItemTextOnPathBar(PATH_BAR_0_PREFIX + names.get(0)))
                .perform(click());

        // should direct back to asset0's contents view and all other assets
        // should be removed from path bar
        checkToolbarTitle(names.get(0));
        onView(withItemTextOnPathBar(PATH_BAR_PREFIX + names.get(1)))
                .check(doesNotExist());
    }


    @Test
    public void selectionMode_enterModeByClickingButton() {
        //add 3 assets
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        //enter the selection mode
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.selection_mode)).perform(click());

        //now all the checkbox should display
        for (int i = 0; i < 3; i++) {
            onData(anything()).inAdapterView(withId(R.id.assets_list))
                    .atPosition(i).onChildView(withId(R.id.asset_checkbox))
                    .check(matches(isDisplayed()));
        }
    }


    @Test
    public void selectionMode_enterModeByLongClickingAnAsset() {
        //add 3 assets
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        //enter the selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        //now all the checkbox should display
        for (int i = 0; i < 3; i++) {
            onData(anything()).inAdapterView(withId(R.id.assets_list))
                    .atPosition(i).onChildView(withId(R.id.asset_checkbox))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void selectionMode_hideUIInSelectionMode() {
        // some UI components should be hided when in selection mode,
        // in case unexpected interactions are performed by the user

        addAsset(ASSET1_NAME);
        clickAsset(0);
        addAsset(ASSET2_NAME);

        // The following UI components should be displayed in the normal mode:
        // PATH BAR
        onView(withId(R.id.pathbar_layout)).check(matches(isDisplayed()));

        // ADD ASSET BUTTON
        onView(withId(R.id.add_asset_button)).check(matches(isDisplayed()));

        // SEARCH BUTTON (menu items)
        onView(withId(R.id.search_view_button)).check(matches(isDisplayed()));

        // DETAILS TAB
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.details_page)))
                .check(matches(isDisplayed()));

        // enter selection mode
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.selection_mode)).perform(click());

        // NOW THEY SHOULD BE HIDDEN
        onView(withId(R.id.pathbar_layout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.add_asset_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.search_view_button)).check(doesNotExist());
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.details_page)))
                .check(doesNotExist());
    }

    @Test
    public void selectionMode_tickTheCheckbox() {
        //add 3 assets
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        //enter the selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        //tick the first asset's checkbox
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).perform(click());
        //now the first asset should be ticked
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).onChildView(withId(R.id.asset_checkbox))
                .check(matches(isChecked()));
    }

    @Test
    public void selectionMode_untickTheCheckbox() {
        //add 3 assets
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        //enter the selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());
        //click the item twice to tick and then untick the checkbox
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).perform(click(), click());
        //now the checkbox should not be checked
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(0).onChildView(withId(R.id.asset_checkbox))
                .check(matches(isNotChecked()));
    }


    @Test
    public void selectionMode_quitMode() {
        //add 3 assets
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        //enter the selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        //quit the selection mode by clicking the CANCEL button
        onView(withId(R.id.selection_cancel_button)).check(matches(isDisplayed()));
        onView(withId(R.id.selection_cancel_button)).perform(click());

        //now the CANCEL button and all the checkbox should disappear
        onView(withId(R.id.selection_cancel_button)).check(matches(not(isDisplayed())));
        for (int i = 0; i < 3; i++) {
            onData(anything()).inAdapterView(withId(R.id.assets_list))
                    .atPosition(i).onChildView(withId(R.id.asset_checkbox))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void moveAsset_hideUIWhenMoving() {
        // some UI components should be hided when moving assets,
        // in case unexpected interactions are performed by the user

        addAsset(ASSET1_NAME);
        clickAsset(0);
        addAsset(ASSET2_NAME);

        // The following UI components should be displayed in the normal mode:
        // ADD ASSET BUTTON
        onView(withId(R.id.add_asset_button)).check(matches(isDisplayed()));

        // SEARCH BUTTON (menu items)
        onView(withId(R.id.search_view_button)).check(matches(isDisplayed()));

        // DETAILS TAB
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.details_page)))
                .check(matches(isDisplayed()));

        // enter selection mode
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.selection_mode)).perform(click());

        // select item and click move button
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(click());
        onView(withId(R.id.selection_move_button)).perform(click());

        // NOW THEY SHOULD BE HIDDEN
        onView(withId(R.id.add_asset_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.search_view_button)).check(doesNotExist());
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.details_page)))
                .check(doesNotExist());

        // PATH BAR should be DISPLAYED
        onView(withId(R.id.pathbar_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void moveAsset_seleteNothing() {
        //add some assets following such structure:
        //dining room > table big, table small
        //living room > (empty)
        prepareAssetsToMove();

        //enter selection mode by long-click an item
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        //select nothing and click MOVE button
        onView(withId(R.id.selection_move_button)).perform(click());

        //cancel move
        onView(withId(R.id.selection_cancel_button)).perform(click());

        //two tables should stay where they were
        onView(withChild(withText(ASSET_TABLE_BIG))).check(matches(isDisplayed()));
        onView(withChild(withText(ASSET_TABLE_SMALL))).check(matches(isDisplayed()));

        //and the living room should still be empty
        onView(withId(R.id.pathbar_root)).perform(click());
        clickAsset(1);
        onView(withChild(withText(ASSET_TABLE_BIG))).check(doesNotExist());
        onView(withChild(withText(ASSET_TABLE_SMALL))).check(doesNotExist());

    }

    @Test
    public void moveAsset_seleteSomeAssets() {
        //add some assets following such structure:
        //dining room > table big, table small
        //living room > (empty)
        prepareAssetsToMove();

        //enter selection mode by long-click an item
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        //select two tables and click MOVE button
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(1).perform(click());
        onView(withId(R.id.selection_move_button)).perform(click());

        //the selected two items should not be interactive
        // (to prevent from moving assets to themselves or their children)
        checkToolbarTitle(ASSET_DINING_ROOM);
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(click());
        checkToolbarTitle(ASSET_DINING_ROOM);
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(1).perform(click());
        checkToolbarTitle(ASSET_DINING_ROOM);

        //go to the living room where tables will be moved
        onView(withId(R.id.pathbar_root)).perform(click());
        clickAsset(1);

        //click CONFIRM button
        onView(withId(R.id.confirm_move_button)).perform(click());

        //now the living room should contain two tables
        onView(withChild(withText(ASSET_TABLE_BIG))).check(matches(isDisplayed()));
        onView(withChild(withText(ASSET_TABLE_SMALL))).check(matches(isDisplayed()));

        //and two tables should disappear from the dining room
        onView(withId(R.id.pathbar_root)).perform(click());
        clickAsset(0);
        onView(withChild(withText(ASSET_TABLE_BIG))).check(doesNotExist());
        onView(withChild(withText(ASSET_TABLE_SMALL))).check(doesNotExist());

    }

    @Test
    public void deleteAsset_deleteCurrentAsset() {
        addAsset(ASSET1_NAME);
        clickAsset(0);

        // check if the current asset is asset1
        checkToolbarTitle(ASSET1_NAME);

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.delete_current_asset)).perform(click());
        onView(withText(R.string.delete_asset_confirm_button)).perform(click());

        // current asset set back to the container (in this case the Root) asset and asset1 no
        // longer exists
        checkToolbarTitle(ROOT_ASSET_NAME);
        onView(withChild(withText(ASSET1_NAME))).check(doesNotExist());
    }

    @Test
    public void deleteAsset_cancelDeleteCurrentAsset() {
        addAsset(ASSET1_NAME);
        clickAsset(0);

        // check if the current asset is asset1
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.main_toolbar))))
                .check(matches(withText(ASSET1_NAME)));
//        Assert.assertEquals(ASSET1_NAME, getToolbarTitle());

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.delete_current_asset)).perform(click());
        onView(withText(R.string.cancel_button)).perform(click());

        // current asset should still be asset1
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.main_toolbar))))
                .check(matches(withText(ASSET1_NAME)));
//        Assert.assertEquals(ASSET1_NAME, getToolbarTitle());
    }

    @Test
    public void deleteAsset_deleteRootAsset() {
        addAsset(ASSET1_NAME);

        // "Delete Current Asset" button should not be displayed in the options menu when
        // the current asset is the root asset
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withChild(withText(R.string.delete_current_asset))).check(doesNotExist());
    }

    @Test
    public void deleteAsset_deleteMultipleAssets() {
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);
        addAsset(ASSET3_NAME);

        // enter selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        // delete asset1 and asset2
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(1).perform(click());

        onView(withId(R.id.selection_delete_button)).perform(click());
        onView(withText(R.string.delete_asset_confirm_button)).perform(click());

        // asset1 and asset2 should be deleted while asset3 is still there
        onView(withChild(withText(ASSET1_NAME))).check(doesNotExist());
        onView(withChild(withText(ASSET2_NAME))).check(doesNotExist());
        onView(withChild(withText(ASSET3_NAME))).check(matches(isDisplayed()));
    }

    @Test
    public void deleteAsset_cancelDeleteMultipleAssets() {
        addAsset(ASSET1_NAME);
        addAsset(ASSET2_NAME);

        // enter selection mode
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(longClick());

        // delete asset1 and asset2
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(0).perform(click());
        onData(anything()).inAdapterView((withId(R.id.assets_list)))
                .atPosition(1).perform(click());

        onView(withId(R.id.selection_delete_button)).perform(click());
        onView(withText(R.string.cancel_button)).perform(click());

        // if cancel deleting, nothing should be deleted
        onView(withChild(withText(ASSET1_NAME))).check(matches(isDisplayed()));
        onView(withChild(withText(ASSET2_NAME))).check(matches(isDisplayed()));
    }

    @Test
    public void editTextDetail_inputNewText() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        swipeToDetailsPage();

        editTextDetail("", "Test text");
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_field))
                .check(matches(withText("Test text")));
    }

    @Test
    public void editTextDetail_changeExistingText() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        swipeToDetailsPage();

        editTextDetail("", "Test text");
        editTextDetail("Test text", "Text test");
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_field))
                .check(matches(withText("Text test")));
    }

    @Test
    public void editTextDetail_inputVoidText() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        swipeToDetailsPage();

        editTextDetail("", "Test text");
        editTextDetail("Test text", "");
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_field))
                .check(matches(withText("")));
    }

    @Test
    public void editTextDetail_inputVeryLongText() {
        addAsset(ASSET1_NAME);
        clickAsset(0);
        swipeToDetailsPage();

        String longField = "";
        for (int i = 0; i <= AppConstraints.TEXTDETAIL_FIELD_CAP; i++) {
            longField += "l";
        }

        editTextDetail("", longField);
        // the edit action should be canceled due to the overlong text
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_field))
                .check(matches(withText("")));
    }

    @Test
    public void photo_takePhotoTest() throws IOException {
        // in order to stub the outgoing intents, need to use IntentsTestRule
        Intent intent = new Intent(swipeActivityRule.getActivity(), SwipeActivity.class);
        intentsRule.launchActivity(intent);

        addAsset(ASSET_LIVING_ROOM);
        clickAsset(0);
        swipeToDetailsPage();

        // stubbing the return data of the camera activity
        Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(R.drawable.androidtest_image_1) + '/' +
                resources.getResourceTypeName(R.drawable.androidtest_image_1) + '/' +
                resources.getResourceEntryName(R.drawable.androidtest_image_1));
        Intent resultData = new Intent();
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Bitmap defaultThumbnail = mFactory.getLocalResourceLoader().getDefaultThumbnail();
        Bitmap defaultPhoto = mFactory.getLocalResourceLoader().getDefaultPhoto();

        // Stub out the Camera
        intending(hasComponent(CameraActivity.class.getName())).respondWith(result);

        clickPhoto(false);

        // check if the CameraActivity is intended
        intended(hasComponent(CameraActivity.class.getName()));

        mDataManager.getAssets()
                .flatMap(Observable::from)
                .filter(asset -> asset.getName().equals(ASSET_LIVING_ROOM))
                .first()
                .doOnNext(asset -> {
                    // check whether the thumbnail of the new image is different from the default one
                    Assert.assertFalse(defaultThumbnail.sameAs(asset.getThumbnail()));
                })
                .flatMap(asset -> mDataManager.getDetails(asset.getId()))
                .flatMap(Observable::from)
                .filter(detail -> detail.getLabel().equals(CategoryType.BasicDetail.PHOTO))
                .doOnNext(detail -> {
                    // check whether the "Photo" detail is updated
                    Assert.assertFalse(defaultPhoto.sameAs((Bitmap) detail.getField()));
                })
                .subscribe();
    }

    @Test
    public void photo_removePhoto() throws IOException {
        // in order to stub the outgoing intents, need to use IntentsTestRule
        Intent intent = new Intent(swipeActivityRule.getActivity(), SwipeActivity.class);
        intentsRule.launchActivity(intent);

        // get the customised photo image
        Resources resources = InstrumentationRegistry.getTargetContext().getResources();
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(R.drawable.androidtest_image_1) + '/' +
                resources.getResourceTypeName(R.drawable.androidtest_image_1) + '/' +
                resources.getResourceEntryName(R.drawable.androidtest_image_1));
        Bitmap customisedImage = MediaStore.Images.Media.getBitmap(
                InstrumentationRegistry.getTargetContext().getContentResolver(), imageUri);

        Bitmap defaultPhoto = mFactory.getLocalResourceLoader().getDefaultPhoto();
        Bitmap defaultThumbnail = mFactory.getLocalResourceLoader().getDefaultThumbnail();

        addAsset(ASSET_LIVING_ROOM);
        clickAsset(0);
        swipeToDetailsPage();

        // change the photo of the newly added asset to the customisedImage
        mDataManager.getAssets()
                .subscribeOn(Schedulers.immediate())
                .flatMap(Observable::from)
                .filter(asset -> asset.getName().equals(ASSET_LIVING_ROOM))
                .first()
                .flatMap(asset -> mDataManager.getDetails(asset.getId()))
                .first()
                .flatMap(Observable::from)
                .filter(detail -> detail.getLabel().equals(CategoryType.BasicDetail.PHOTO))
                .first()
                .doOnNext(detail -> mDataManager.updateDetail(
                        detail.getAssetId(), detail.getId(), detail.getType(), null, customisedImage))
                .doOnNext(detail -> {
                    // make sure the photo is changed
                    Assert.assertFalse(defaultPhoto.sameAs((Bitmap) detail.getField()));

                    clickPhoto(true);
                    onView(withText(R.string.photo_remove_confirm_button)).perform(click());
                })
                .subscribe(
                        //onNext
                        detail -> mDataManager.getAssets()
                                .subscribeOn(Schedulers.immediate())
                                .flatMap(Observable::from)
                                .filter(asset -> asset.getName().equals(ASSET_LIVING_ROOM))
                                .first()
                                .doOnNext(asset -> {
                                    Assert.assertTrue(defaultThumbnail.sameAs(asset.getThumbnail()));
                                    Assert.assertTrue(defaultPhoto.sameAs((Bitmap) detail.getField()));
                                })
                                .subscribe()
                );

    }

    //endregion

    //region PRIVATE STUFF

    private void addAsset(String assetName) {
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(allOf(is(instanceOf(EditText.class)), withText(is(""))))
                .perform(replaceText(assetName));
        onView(withText(R.string.add_asset_confirm_button)).perform(click());
    }

    private void addAsset(String assetName, CategoryType category) {
        onView(withId(R.id.add_asset_button)).perform(click());
        onView(allOf(is(instanceOf(EditText.class)), withText(is(""))))
                .perform(replaceText(ASSET1_NAME));
        onView(withId(R.id.category_spinner)).perform(click());

        // select "Food" category
        onData(allOf(is(instanceOf(CategoryType.class)), hasToString(category.toString())))
                .inRoot(RootMatchers.isPlatformPopup()).perform(click());

        onView(withText(R.string.add_asset_confirm_button)).perform(click());
    }

    private void clickAsset(int num) {
        onData(anything()).inAdapterView(withId(R.id.assets_list))
                .atPosition(num).perform(click());
    }

    private void checkToolbarTitle(String expectedTitle) {
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.main_toolbar))))
                .check(matches(withText(expectedTitle)));
    }

    private void swipeToDetailsPage() {
        onView(withId(R.id.viewpager)).perform(swipeLeft());
        // wait for the animation
        SystemClock.sleep(1000);
    }

    private void swipeToContentsPage() {
        onView(withId(R.id.viewpager)).perform(swipeRight());
        // wait for the animation
        SystemClock.sleep(1000);
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

    private void prepareAssetsToMove() {
        // add two rooms
        addAsset(ASSET_DINING_ROOM);
        addAsset(ASSET_LIVING_ROOM);

        //go the the dining room
        clickAsset(0);

        //add two tables
        addAsset(ASSET_TABLE_BIG);
        addAsset(ASSET_TABLE_SMALL);
    }

    // change this method if default is not "Miscellaneous"
    private void checkDetailsListOfMiscellaneous() {
        if (!DEFAULT_CATEGORY.equals(CategoryType.Miscellaneous))
            throw new IllegalStateException("Please change the checklist of the default category");

        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_label))
                .check(matches(withText(MISCELLANEOUS_DETAIL_LABELS[1])));
    }

    // change this method if the specified category is not "Food"
    private void checkDetailsListOfFood() {
        if (!SPECIFIED_CATEGORY.equals(CategoryType.Food))
            throw new IllegalStateException("Please change the checklist of the specified category");

        // check if all the details of "Food" are there
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).onChildView(withId(R.id.detail_label))
                .check(matches(withText(FOOD_DETAIL_LABELS[1])));
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(2).onChildView(withId(R.id.detail_label))
                .check(matches(withText(FOOD_DETAIL_LABELS[2])));
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(3).onChildView(withId(R.id.detail_label))
                .check(matches(withText(FOOD_DETAIL_LABELS[3])));
    }

    private void clickPhoto(boolean longClick) {
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(0).perform(longClick ? longClick() : click());
    }

    private void editTextDetail(String fromName, String newDetail) {
        onData(anything()).inAdapterView(withId(R.id.details_list))
                .atPosition(1).perform(click());
        onView(allOf(withClassName(endsWith("EditText")), withText(is(fromName))))
                .perform(replaceText(newDetail));
        onView(withText(R.string.edit_detail_confirm_button)).perform(click());
    }

    private static final String TAG = "SwipeActivityTest";

    private static final String ROOT_ASSET_NAME = "Assets";
    private static final CategoryType DEFAULT_CATEGORY = CategoryType.Miscellaneous;
    private static final CategoryType SPECIFIED_CATEGORY = CategoryType.Food;

    private static final String ASSET_NAME = "ASSET_NAME";
    private static final String ASSET1_NAME = "ASSET1_NAME";
    private static final String ASSET2_NAME = "ASSET2_NAME";
    private static final String ASSET3_NAME = "ASSET3_NAME";
    private static final String ASSET_LIVING_ROOM = "Living Room";
    private static final String ASSET_DINING_ROOM = "Dining Room";
    private static final String ASSET_TABLE_BIG = "table big";
    private static final String ASSET_TABLE_SMALL = "table small";
    private static final String PATH_BAR_0_PREFIX = "  ";
    private static final String PATH_BAR_PREFIX = " >  ";

    // Category Details List
    private static final String[] MISCELLANEOUS_DETAIL_LABELS =
            {CategoryType.BasicDetail.PHOTO, CategoryType.BasicDetail.NOTES};
    private static final String[] FOOD_DETAIL_LABELS =
            {CategoryType.BasicDetail.PHOTO, CategoryType.BasicDetail.NOTES,
                    "Expiry Date", "Purchased From"};

    private Context mContext;
    private SortMyStuffApp mApp;
    private Activity mActivity;
    private IDataManager mDataManager;
    private IFactory mFactory;

    //endregion

}
