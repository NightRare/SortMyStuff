package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.ui.adding.AddingAssetActivity;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;
import nz.ac.aut.comp705.sortmystuff.utils.Log;

import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.CONTENTS_DEFAULT_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.CONTENTS_MOVING_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.CONTENTS_SELECTION_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.INTENT_ASSET_ID;

public class ContentsFragment extends Fragment implements IContentsView {

    public ContentsFragment() {
        // Required empty public constructor
    }

    public static ContentsFragment newInstance() {
        ContentsFragment fragment = new ContentsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (SwipeActivity) getActivity();
        mActivity.setContentsViewListeners(mViewListeners);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.contents_frag, container, false);
        mSelectedAssets = new ArrayList<>();

        initPathBar();
        initAddAssetFab();
        initMovingModeFabs();
        initAssetsListView();
        initSelectionModeButtons();
        initProgressIndicator();
        setSelectionModeButtonsVisibility(false);

        return mRootView;
    }

    private void initProgressIndicator() {
        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) mRootView.findViewById(R.id.contents_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        View listView = mRootView.findViewById(R.id.assets_list);

        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(() -> mPresenter.loadCurrentContents());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // start the presenter
        checkIntendedAsset();
        mPresenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIntendedAsset();
        mPresenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPresenter(IContentsPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showTitle(IAsset asset) {
        mActivity.setCurrentAsset(asset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAssetContents(List<IAsset> assets, int mode) {
        boolean displayCheckbox = false;
        List<IAsset> movingAssets = new ArrayList<>();

        switch (mode) {
            case CONTENTS_SELECTION_MODE:
                displayCheckbox = true;
                setSelectionModeButtonsVisibility(true);
                setMovingModeFabsVisibility(false);
                mFab.setVisibility(View.GONE);
                mPathBarLayout.setVisibility(View.GONE);

                mActivity.toggleMenuDisplay(false);
                mActivity.setDetailsPageVisibility(false);
                break;

            case CONTENTS_MOVING_MODE:
                setSelectionModeButtonsVisibility(false);
                setMovingModeFabsVisibility(true);
                mFab.setVisibility(View.GONE);
                mPathBarLayout.setVisibility(View.VISIBLE);

                mActivity.toggleMenuDisplay(false);
                mActivity.setDetailsPageVisibility(false);

                movingAssets = new ArrayList<>(mSelectedAssets);
                break;

            // CONTENTS_DEFAULT_MODE falls into this
            default:
                setSelectionModeButtonsVisibility(false);
                setMovingModeFabsVisibility(false);
                mFab.setVisibility(View.VISIBLE);
                mPathBarLayout.setVisibility(View.VISIBLE);

                mActivity.toggleMenuDisplay(true);
                mActivity.setDetailsPageVisibility(true);

                break;
        }
        mAdapter = new AssetListAdapter(assets, mActivity.getApplicationContext()
                , displayCheckbox, movingAssets);
        mAssetListView.setAdapter(mAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPath(List<IAsset> assets) {
        PathBarAdapter pba = new PathBarAdapter(mActivity, assets, mViewListeners);
        mPathBar.setAdapter(pba);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDeleteDialog(boolean deletingCurrentAsset) {
        String message;
        if (deletingCurrentAsset) {
            message = "Deleting \'" + mActivity.getTitle().toString() + "\'\n" +
                    "and its children assets.";
        } else {
            message = "Deleting selected assets\n" +
                    "and their children assets.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(message);

        builder.setPositiveButton(R.string.delete_asset_confirm_button,
                (dialog, which) -> mViewListeners.onDeleteDialogConfirmClick(deletingCurrentAsset));
        //creates the Cancel button and what happens when clicked
        builder.setNegativeButton(R.string.cancel_button, (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadingContentsError(Throwable exception) {
        Log.e("LoadingAssetsError", "LoadingAssetsError", exception);
        showMessage("Loading assets error.");
        //TODO: to be implemented
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (mRootView == null) return;

        SwipeRefreshLayout srl = (SwipeRefreshLayout) mRootView.findViewById(R.id.contents_refresh_layout);
        srl.post(() -> srl.setRefreshing(active));
    }

    //region PRIVATE STUFF

    private void checkIntendedAsset() {
        String intendedAssetId = mActivity.getIntent().getStringExtra(INTENT_ASSET_ID);
        if (intendedAssetId != null) {
            // once acquired the intended asset, remove it from the intent
            mActivity.getIntent().removeExtra(INTENT_ASSET_ID);

            mPresenter.setCurrentAssetId(intendedAssetId);
        }
    }

    private void setSelectionModeButtonsVisibility(boolean isVisible) {
        if (isVisible) {
            mCancel_btn.setVisibility(View.VISIBLE);
            mSelectAll_btn.setVisibility(View.VISIBLE);
            mDelete_btn.setVisibility(View.VISIBLE);
            mMove_btn.setVisibility(View.VISIBLE);
        } else {
            mCancel_btn.setVisibility(View.GONE);
            mSelectAll_btn.setVisibility(View.GONE);
            mDelete_btn.setVisibility(View.GONE);
            mMove_btn.setVisibility(View.GONE);
        }
    }

    private void setMovingModeFabsVisibility(boolean isVisible) {
        if (isVisible) {
            mFabCancelMoveButton.setVisibility(View.VISIBLE);
            mFabConfirmMoveButton.setVisibility(View.VISIBLE);
        } else {
            mFabCancelMoveButton.setVisibility(View.GONE);
            mFabConfirmMoveButton.setVisibility(View.GONE);
        }
    }

    private void initAddAssetFab() {
        mFab = (FloatingActionButton) mRootView.findViewById(R.id.add_asset_button);
        mFab.setOnClickListener(v -> mViewListeners.onAddAssetFabClick());
    }

    private void initPathBar() {
        mPathBarRoot = (TextView) mRootView.findViewById(R.id.pathbar_root);
        mPathBarRoot.setOnClickListener(v -> mViewListeners.onPathbarRootClick());

        mPathBar = (RecyclerView) mRootView.findViewById(R.id.pathbar_pathview);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        llm.setStackFromEnd(true);
        mPathBar.setLayoutManager(llm);

        mPathBarLayout = mRootView.findViewById(R.id.pathbar_layout);
    }

    private void initMovingModeFabs() {
        mFabConfirmMoveButton = (FloatingActionButton) mRootView.findViewById(R.id.confirm_move_button);
        mFabConfirmMoveButton.setOnClickListener(v -> mViewListeners.onMovingModeConfirmClick());

        mFabCancelMoveButton = (FloatingActionButton) mRootView.findViewById(R.id.cancel_move_button);
        mFabCancelMoveButton.setOnClickListener(v -> mViewListeners.onMovingModeCancelClick());
    }

    private void initAssetsListView() {
        mAssetListView = (ListView) mRootView.findViewById(R.id.assets_list);
        mAssetListView.setOnItemClickListener((parent, view, position, id) ->
                mViewListeners.onContentAssetClick(parent, view, position, id));

        mAssetListView.setOnItemLongClickListener(((parent, view, position, id) ->
                mViewListeners.onContentAssetLongClick()));
    }

    private void initSelectionModeButtons() {
        mCancel_btn = (Button) mRootView.findViewById(R.id.selection_cancel_button);
        mSelectAll_btn = (Button) mRootView.findViewById(R.id.selection_select_all_button);
        mDelete_btn = (Button) mRootView.findViewById(R.id.selection_delete_button);
        mMove_btn = (Button) mRootView.findViewById(R.id.selection_move_button);

        mCancel_btn.setOnClickListener(v -> mViewListeners.onSelectionModeCancelClick());
        mSelectAll_btn.setOnClickListener(v -> mViewListeners.onSelectionModeSelectAllClick());
        mDelete_btn.setOnClickListener(v -> mViewListeners.onSelectionModeDeleteClick());
        mMove_btn.setOnClickListener(v -> mViewListeners.onSelectionModeMoveClick());
    }

    private class ContentsViewListeners implements IContentsView.ViewListeners {

        @Override
        public void onContentAssetClick(AdapterView<?> parent, View view, int position, long id) {
            AssetListAdapter.ViewHolder holder = (AssetListAdapter.ViewHolder) view.getTag();
            //if the text is grey light then should not be able to interact
            if (holder.textView.getCurrentTextColor()
                    == ContextCompat.getColor(mActivity, R.color.light_grey)) return;
            AssetListAdapter adapter = (AssetListAdapter) parent.getAdapter();

            //if it's in selection mode
            if (adapter.isCheckboxShowed()) {
                holder.checkbox.toggle();
                adapter.getmSelectStatusMap().put(position, holder.checkbox.isChecked());
            } else {
                //fetches the selected asset in the list
                IAsset clickedAsset = (IAsset) parent.getItemAtPosition(position);
                //sets the selected asset's ID as the current asset (to be viewed)
                mPresenter.setCurrentAssetId(clickedAsset.getId());
                mPresenter.loadCurrentContents();
            }
        }

        @Override
        public boolean onContentAssetLongClick() {
            mPresenter.loadCurrentContentsWithMode(CONTENTS_SELECTION_MODE);
            return true;
        }

        @Override
        public void onOptionsDeleteCurrentAssetSelected() {
            mPresenter.deleteCurrentAsset();
        }

        @Override
        public void onOptionsSelectionModeSelected() {
            mPresenter.loadCurrentContentsWithMode(CONTENTS_SELECTION_MODE);
        }

        @Override
        public void onAddAssetFabClick() {
            Intent addAssetIntent = new Intent(mActivity, AddingAssetActivity.class);
            addAssetIntent.putExtra(AppStrings.INTENT_CONTAINER_ID, mPresenter.getCurrentAssetId());
            mActivity.startActivityForResult(addAssetIntent, AppStrings.REQUEST_NEW_ASSET);
        }

        @Override
        public void onAddAssetConfirmClick(String name, CategoryType category) {
            mPresenter.createAsset(name, category);
        }

        @Override
        public void onPathbarRootClick() {
            mPresenter.setCurrentAssetIdToRoot();
            mPresenter.loadCurrentContents();
        }

        @Override
        public void onPathbarItemClick(String intendingAssetId) {
            mPresenter.setCurrentAssetId(intendingAssetId);
            mPresenter.loadCurrentContents();
        }

        @Override
        public void onSelectionModeCancelClick() {
            mPresenter.loadCurrentContentsWithMode(CONTENTS_DEFAULT_MODE);
        }

        @Override
        public void onSelectionModeSelectAllClick() {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                mAdapter.getmSelectStatusMap().put(i, true);
            }
            mAdapter.notifyDataSetChanged();

            showMessage(mAdapter.getCount() + " items selected");
        }

        @Override
        public void onSelectionModeDeleteClick() {
            mSelectedAssets = new ArrayList<>(mAdapter.getSelectedAssets().values());
            if (mSelectedAssets.isEmpty())
                showMessage("Please select the assets to be deleted.");
            else {
                showDeleteDialog(false);
                mPresenter.loadCurrentContentsWithMode(CONTENTS_DEFAULT_MODE);
            }
        }

        @Override
        public void onSelectionModeMoveClick() {
            mSelectedAssets = new ArrayList<>(mAdapter.getSelectedAssets().values());

            if (mSelectedAssets.isEmpty())
                showMessage("Please select the assets to be moved.");
            else
                mPresenter.loadCurrentContentsWithMode(CONTENTS_MOVING_MODE);
        }

        @Override
        public void onMovingModeConfirmClick() {
            if (mSelectedAssets.isEmpty()) {
                Toast.makeText(mActivity,
                        "You haven't selected any items.", Toast.LENGTH_SHORT).show();
            } else {
                mPresenter.moveAssets(mSelectedAssets);
                mPresenter.loadCurrentContents();
            }
            mPresenter.loadCurrentContentsWithMode(CONTENTS_DEFAULT_MODE);
        }

        @Override
        public void onMovingModeCancelClick() {
            mPresenter.loadCurrentContentsWithMode(CONTENTS_DEFAULT_MODE);
        }

        @Override
        public void onDeleteDialogConfirmClick(boolean deletingCurrentAsset) {
            if (deletingCurrentAsset)
                mPresenter.recycleCurrentAssetRecursively();
            else
                mPresenter.recycleAssetsRecursively(mSelectedAssets);
        }
    }

    //region UI COMPONENTS

    private View mRootView;

    private FloatingActionButton mFab;
    private FloatingActionButton mFabCancelMoveButton;
    private FloatingActionButton mFabConfirmMoveButton;

    private View mPathBarLayout;
    private TextView mPathBarRoot;
    private RecyclerView mPathBar;

    private ListView mAssetListView;
    private Button mCancel_btn, mSelectAll_btn, mMove_btn, mDelete_btn;
    private AssetListAdapter mAdapter;

    //endregion

    private IContentsPresenter mPresenter;
    private List<IAsset> mSelectedAssets;
    private SwipeActivity mActivity;
    private IContentsView.ViewListeners mViewListeners = new ContentsViewListeners();

    //endregion
}
