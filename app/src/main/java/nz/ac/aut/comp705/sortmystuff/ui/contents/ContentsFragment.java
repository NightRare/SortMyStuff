package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.ui.adding.AddingAssetActivity;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.PopupMenuHelper;

import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.INTENT_ASSET_ID;

public class ContentsFragment extends Fragment implements IContentsView {

    public ContentsFragment() {
        // Required empty public constructor
    }

    public static ContentsFragment newInstance() {
        return new ContentsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new AssetRecyclerAdapter(
                this.getContext(), new ArrayList<>(), mViewListeners);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (SwipeActivity) getActivity();
        mActivity.setContentsViewListeners(mViewListeners);
        mSortMethod = new SortMethod();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.contents_frag, container, false);

        mRefreshScrollLayout = (ScrollChildSwipeRefreshLayout) mRootView
                .findViewById(R.id.contents_refresh_layout);

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

    @Override
    public void setPresenter(IContentsPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showTitle(IAsset asset) {
        mActivity.setCurrentAsset(asset);
    }

    @Override
    public void showAssetContents(List<IAsset> assets, ContentsViewMode viewMode) {

        switch (viewMode) {
            case Selection:
                setSelectionModeButtonsVisibility(true);
                setMovingModeFabsVisibility(false);
                mFab.setVisibility(View.GONE);
                mPathBarLayout.setVisibility(View.GONE);

                mActivity.toggleMenuDisplay(false);
                mActivity.setDetailsPageVisibility(false);

                mRefreshScrollLayout.setEnabled(false);
                break;

            case Moving:
                setSelectionModeButtonsVisibility(false);
                setMovingModeFabsVisibility(true);
                mFab.setVisibility(View.GONE);
                mPathBarLayout.setVisibility(View.VISIBLE);

                mActivity.toggleMenuDisplay(false);
                mActivity.setDetailsPageVisibility(false);

                mRefreshScrollLayout.setEnabled(false);
                break;

            default:
                setSelectionModeButtonsVisibility(false);
                setMovingModeFabsVisibility(false);
                mFab.setVisibility(View.VISIBLE);
                mPathBarLayout.setVisibility(View.VISIBLE);

                mActivity.toggleMenuDisplay(true);
                mActivity.setDetailsPageVisibility(true);

                mRefreshScrollLayout.setEnabled(true);
                break;
        }
        mAdapter.replaceData(assets, viewMode, mSortMethod.sortParam, mSortMethod.desc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPath(List<IAsset> assets) {
        PathBarAdapter pba = new PathBarAdapter(mActivity, assets, mViewListeners);
        mPathBar.setAdapter(pba);
    }

    @Override
    public void showDeleteDialog() {
        String message = "Deleting selected assets\n" +
                "and their children assets.";

        buildDeleteDialog(message, mAdapter.getSelectedAssets())
                .show();
    }

    @Override
    public void showDeleteDialog(@NonNull IAsset asset) {
        String message = "Deleting \'" + asset.getName() + "\'\n" +
                "and its children assets.";

        buildDeleteDialog(message, Arrays.asList(asset.getId()))
                .show();
    }


    @Override
    public void showRenameAssetDialog(String assetId, String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.rename_asset_action_title);

        View addAssetLayout = mActivity.getLayoutInflater().inflate(R.layout.app_edittext_dialog, null);
        builder.setView(addAssetLayout);

        EditText input = (EditText) addAssetLayout.findViewById(R.id.app_edittext_input);
        input.setText(oldName);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSingleLine();
        input.setSelectAllOnFocus(true);

        builder.setPositiveButton(R.string.add_asset_confirm_button, (dialog, which) ->
                mPresenter.renameAsset(assetId, input.getText().toString()));

        builder.setNegativeButton(R.string.cancel_button, (dialog, id) -> dialog.cancel());

        builder.show()
                // auto pop up the keyboard
                .getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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

    private class ContentsViewListeners implements IContentsView.ViewListeners {

        @Override
        public void onContentAssetClick(IAsset clickedAsset) {
            //sets the selected asset's ID as the current asset (to be viewed)
            mPresenter.setCurrentAssetId(clickedAsset.getId());
            mPresenter.loadCurrentContents();
        }

        @Override
        public boolean onAssetMoreOptionsClick(IAsset clickedAsset, MenuItem clickedOption) {
            switch (clickedOption.getItemId()) {
                case R.id.asset_more_rename:
                    showRenameAssetDialog(clickedAsset.getId(), clickedAsset.getName());
                    return true;
                case R.id.asset_more_delete:
                    showDeleteDialog(clickedAsset);
                    return true;
                case R.id.asset_more_move:
                    mAdapter.addToSelectedAsset(clickedAsset.getId());
                    mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Moving);
                    return true;
            }
            return false;
        }

        @Override
        public boolean onContentAssetLongClick() {
            mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Selection);
            return true;
        }

        @Override
        public void onSortContentClick() {
            View sortButtonView = getActivity().findViewById(R.id.main_menu_sort_button);
            PopupMenuHelper.build(
                    getContext(),
                    sortButtonView,
                    R.menu.sort_menu,
                    ContentsFragment.this::sortContentAssetsBy,
                    false).show();
        }

        @Override
        public void onAddAssetFabClick() {
            Intent addAssetIntent = new Intent(mActivity, AddingAssetActivity.class);
            addAssetIntent.putExtra(AppStrings.INTENT_CONTAINER_ID, mPresenter.getCurrentAssetId());
            mActivity.startActivityForResult(addAssetIntent, AppStrings.REQUEST_NEW_ASSET);
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
            mAdapter.clearSelectedAssets();
            mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Default);
        }

        @Override
        public void onSelectionModeSelectAllClick() {
            mAdapter.selectAllAssets();
            showMessage(mAdapter.getSelectedAssets().size() + " items selected");
        }

        @Override
        public void onSelectionModeDeleteClick() {
            if (mAdapter.getSelectedAssets().isEmpty())
                showMessage("Please select the assets to be deleted.");
            else {
                showDeleteDialog();
            }
        }

        @Override
        public void onSelectionModeMoveClick() {
            if (mAdapter.getSelectedAssets().isEmpty())
                showMessage("Please select the assets to be moved.");
            else
                mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Moving);
        }

        @Override
        public void onMovingModeConfirmClick() {
            List<String> movingAssets = mAdapter.getSelectedAssets();
            if (movingAssets.isEmpty()) return;
            mPresenter.moveAssets(movingAssets);
            mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Default);
        }

        @Override
        public void onMovingModeCancelClick() {
            mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Default);
        }

        @Override
        public void onDeleteDialogConfirmClick(List<String> deletingAssetIds) {
            mPresenter.recycleAssetsRecursively(deletingAssetIds);
        }
    }

    private void checkIntendedAsset() {
        String intendedAssetId = mActivity.getIntent().getStringExtra(INTENT_ASSET_ID);
        if (intendedAssetId != null) {
            // once acquired the intended asset, remove it from the intent
            mActivity.getIntent().removeExtra(INTENT_ASSET_ID);

            mPresenter.setCurrentAssetId(intendedAssetId);
        }
    }

    private void setSelectionModeButtonsVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        mCancel_btn.setVisibility(visibility);
        mSelectAll_btn.setVisibility(visibility);
        mDelete_btn.setVisibility(visibility);
        mMove_btn.setVisibility(visibility);
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
        TextView pathBarRoot = (TextView) mRootView.findViewById(R.id.pathbar_root);
        pathBarRoot.setOnClickListener(v -> mViewListeners.onPathbarRootClick());

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
        RecyclerView assetListView = (RecyclerView) mRootView.findViewById(R.id.assets_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                assetListView.getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                assetListView.getContext(),
                layoutManager.getOrientation());
        assetListView.setLayoutManager(layoutManager);
        assetListView.addItemDecoration(dividerItemDecoration);
        assetListView.setAdapter(mAdapter);
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

    private AlertDialog buildDeleteDialog(String message, List<String> deletingAssetIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(message);

        builder.setPositiveButton(R.string.delete_asset_confirm_button,
                (dialog, which) -> mViewListeners.onDeleteDialogConfirmClick(deletingAssetIds));
        //creates the Cancel button and what happens when clicked
        builder.setNegativeButton(R.string.cancel_button, (dialog, id) ->
                mPresenter.loadCurrentContentsWithMode(ContentsViewMode.Default));
        return builder.create();
    }

    private boolean sortContentAssetsBy(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_menu_by_name:
                mSortMethod = new SortMethod(
                        AssetRecyclerAdapter.SortParam.Name,
                        false);
                break;
            case R.id.sort_menu_by_create:
                mSortMethod = new SortMethod(
                        AssetRecyclerAdapter.SortParam.CreatedAt,
                        true);
                break;
            case R.id.sort_menu_by_modify:
                mSortMethod = new SortMethod(
                        AssetRecyclerAdapter.SortParam.ModifiedAt,
                        true);
                break;
            case R.id.sort_menu_by_default:
                mSortMethod = new SortMethod();
                break;
        }
        mAdapter.sortData(mSortMethod.sortParam, mSortMethod.desc);
        return true;
    }

    private static final class SortMethod {
        private final AssetRecyclerAdapter.SortParam sortParam;
        private final boolean desc;

        private SortMethod() {
            this.sortParam = AssetRecyclerAdapter.SortParam.Default;
            this.desc = false;
        }

        private SortMethod(
                AssetRecyclerAdapter.SortParam sortParam,
                boolean desc) {
            this.sortParam = sortParam;
            this.desc = desc;
        }

        private boolean isDefault() {
            return this.sortParam.equals(AssetRecyclerAdapter.SortParam.Default)
                    && !desc;
        }
    }

    //region UI COMPONENTS

    private View mRootView;

    private FloatingActionButton mFab;
    private FloatingActionButton mFabCancelMoveButton;
    private FloatingActionButton mFabConfirmMoveButton;
    private ScrollChildSwipeRefreshLayout mRefreshScrollLayout;

    private View mPathBarLayout;
    private RecyclerView mPathBar;

    private Button mCancel_btn, mSelectAll_btn, mMove_btn, mDelete_btn;
    private AssetRecyclerAdapter mAdapter;

    //endregion

    private IContentsPresenter mPresenter;
    private SwipeActivity mActivity;
    private IContentsView.ViewListeners mViewListeners = new ContentsViewListeners();
    private SortMethod mSortMethod;

    //endregion
}
