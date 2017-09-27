package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;

import static nz.ac.aut.comp705.sortmystuff.utils.AppCode.CONTENTS_DEFAULT_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppCode.CONTENTS_MOVING_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppCode.CONTENTS_SELECTION_MODE;

public class ContentsFragment extends Fragment implements IContentsView{
//    private static final String ARG_PRESENTER = "presenter";


    public ContentsFragment() {
        // Required empty public constructor
    }

    public static ContentsFragment newInstance() {
        ContentsFragment fragment = new ContentsFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = (SwipeActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.contents_frag, container, false);

        initPathBar();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_asset_button);
        fabCancelMoveButton = (FloatingActionButton) rootView.findViewById(R.id.cancel_move_button);
        fabConfirmMoveButton = (FloatingActionButton) rootView.findViewById(R.id.confirm_move_button);
        assetListView = (ListView) rootView.findViewById(R.id.index_list);
        pathBarLayout = rootView.findViewById(R.id.pathbar_layout);
        selectedAssets = new ArrayList<>();

        initEditModeButtons();

        // register all the listeners
        registerListeners();

        // subscribe the presenter
        presenter.subscribe();
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
        this.presenter = presenter;
    }

    @Override
    public void showAssetTitle(IAsset asset) {
        activity.setCurrentAsset(asset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAssetContents(List<Asset> assets, int mode) {
        boolean displayCheckbox = false;
        List<Asset> movingAssets = new ArrayList<>();

        switch (mode) {
            case CONTENTS_SELECTION_MODE:
                displayCheckbox = true;
                setSelectionModeButtonsVisibility(true);
                fab.setVisibility(View.GONE);
                fabCancelMoveButton.setVisibility(View.GONE);
                fabConfirmMoveButton.setVisibility(View.GONE);
                pathBarLayout.setVisibility(View.GONE);

                activity.toggleMenuDisplay(false);
                activity.setDetailsPageVisibility(false);
                break;

            case CONTENTS_MOVING_MODE:
                setSelectionModeButtonsVisibility(false);
                fab.setVisibility(View.GONE);
                fabCancelMoveButton.setVisibility(View.VISIBLE);
                fabConfirmMoveButton.setVisibility(View.VISIBLE);
                pathBarLayout.setVisibility(View.VISIBLE);

                activity.toggleMenuDisplay(false);
                activity.setDetailsPageVisibility(false);

                movingAssets = new ArrayList<>(selectedAssets);
                break;

            // CONTENTS_DEFAULT_MODE falls into this
            default:
                setSelectionModeButtonsVisibility(false);
                fab.setVisibility(View.VISIBLE);
                fabCancelMoveButton.setVisibility(View.GONE);
                fabConfirmMoveButton.setVisibility(View.GONE);
                pathBarLayout.setVisibility(View.VISIBLE);

                activity.toggleMenuDisplay(true);
                activity.setDetailsPageVisibility(true);

                break;
        }
        adapter = new AssetListAdapter(assets, activity.getApplicationContext()
                , displayCheckbox, movingAssets);
        assetListView.setAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddDialog() {
        getAddAssetDialog().show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPath(List<Asset> assets) {
        PathBarAdapter pba = new PathBarAdapter(activity, assets, presenter);
        pathBar.setAdapter(pba);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDeleteDialog(boolean deletingCurrentAsset) {
        String message;
        if (deletingCurrentAsset) {
            message = "Deleting \'" + activity.getTitle().toString() + "\'\n" +
                    "and its children assets.";
        } else {
            message = "Deleting selected assets\n" +
                    "and their children assets.";
        }
        getConfirmDeleteDialogBuilder(deletingCurrentAsset, message).create().show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Asset> getSelectedAssets() {
        return new ArrayList<>(selectedAssets);
    }

    //region PRIVATE STUFF

    private IContentsPresenter presenter;
    private List<Asset> selectedAssets;
    private SwipeActivity activity;

    //region UI COMPONENTS

    private View rootView;

    private FloatingActionButton fab;
    private FloatingActionButton fabCancelMoveButton;
    private FloatingActionButton fabConfirmMoveButton;

    private View pathBarLayout;
    private TextView pathBarRoot;
    private RecyclerView pathBar;

    private ListView assetListView;
    private Button cancel_btn, selectAll_btn, move_btn, delete_btn;
    private AssetListAdapter adapter;

    //endregion

    private void setSelectionModeButtonsVisibility(boolean isVisible) {
        if(isVisible) {
            cancel_btn.setVisibility(View.VISIBLE);
            selectAll_btn.setVisibility(View.VISIBLE);
            delete_btn.setVisibility(View.VISIBLE);
            move_btn.setVisibility(View.VISIBLE);
        } else {
            cancel_btn.setVisibility(View.GONE);
            selectAll_btn.setVisibility(View.GONE);
            delete_btn.setVisibility(View.GONE);
            move_btn.setVisibility(View.GONE);
        }
    }



    /**
     * Build a dialog box format for adding assets
     * that enables a single line input
     * and has a functional save and cancel button
     *
     * @return builder the dialog box format
     */
    private AlertDialog getAddAssetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.add_asset_dialog_title);

        final View addAssetLayout = activity.getLayoutInflater().inflate(R.layout.contents_add_asset, null);
        builder.setView(addAssetLayout);

        initCategorySpinner(addAssetLayout);
        final EditText input = (EditText) addAssetLayout.findViewById(R.id.asset_name_input);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSingleLine();

        builder.setPositiveButton(R.string.add_asset_confirm_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Spinner spinner = (Spinner) addAssetLayout.findViewById(R.id.category_spinner);
                CategoryType category = (CategoryType) spinner.getSelectedItem();
                // get user input and add input as asset
                presenter.createAsset(input.getText().toString(), category);
            }
        });
        //creates the Cancel button and what happens when clicked
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private AlertDialog.Builder getConfirmDeleteDialogBuilder(final boolean deletingCurrentAsset,
                                                              String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(message);

        builder.setPositiveButton(R.string.delete_asset_confirm_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deletingCurrentAsset)
                    presenter.recycleCurrentAssetRecursively();
                else
                    presenter.recycleAssetsRecursively(selectedAssets);
            }
        });
        //creates the Cancel button and what happens when clicked
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return builder;
    }


    /**
     * Initialises path bar.
     */
    private void initPathBar() {
        pathBarRoot = (TextView) rootView.findViewById(R.id.pathbar_root);
        pathBar = (RecyclerView) rootView.findViewById(R.id.pathbar_pathview);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        llm.setStackFromEnd(true);
        pathBar.setLayoutManager(llm);
    }

    private void initEditModeButtons() {
        cancel_btn = (Button) activity.findViewById(R.id.selection_cancel_button);
        selectAll_btn = (Button) activity.findViewById(R.id.selection_select_all_button);
        delete_btn = (Button) activity.findViewById(R.id.selection_delete_button);
        move_btn = (Button) activity.findViewById(R.id.selection_move_button);

        cancel_btn.setVisibility(View.GONE);
        selectAll_btn.setVisibility(View.GONE);
        delete_btn.setVisibility(View.GONE);
        move_btn.setVisibility(View.GONE);
    }


    private void initCategorySpinner(View contextView) {
        Spinner spinner = (Spinner) contextView.findViewById(R.id.category_spinner);

        List<CategoryType> categories = new ArrayList();
        for(CategoryType cy : CategoryType.values()) {
            if(cy.equals(CategoryType.None))
                continue;
            categories.add(cy);
        }
        ArrayAdapter<CategoryType> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(CategoryType.Miscellaneous));
    }

    /**
     * Registers the listeners to UI components.
     */
    private void registerListeners() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
        fabCancelMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loadCurrentContents(false, CONTENTS_DEFAULT_MODE);
            }
        });

        fabConfirmMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAssets.isEmpty()) {
                    Toast.makeText(activity,
                            "You haven't selected any items.", Toast.LENGTH_SHORT).show();
                } else {
                    presenter.moveAssets(selectedAssets);
                    presenter.loadCurrentContents(false);
                }
                presenter.loadCurrentContents(false, CONTENTS_DEFAULT_MODE);
            }
        });

        assetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetListAdapter.ViewHolder holder = (AssetListAdapter.ViewHolder) view.getTag();

                //if the text is grey light then should not be able to interact
                if(holder.textView.getCurrentTextColor()
                        == ContextCompat.getColor(activity, R.color.light_grey))
                    return;

                AssetListAdapter adapter = (AssetListAdapter) parent.getAdapter();

                if (adapter.isCheckboxShowed()) {
                    holder.checkbox.toggle();
                    adapter.getSelectStatusMap().put(position, holder.checkbox.isChecked());
                    //adapter.notifyDataSetChanged();
                } else {
                    //fetches the selected asset in the list
                    Asset clickedAsset = (Asset) parent.getItemAtPosition(position);
                    //sets the selected asset's ID as the current asset (to be viewed)
                    presenter.setCurrentAssetId(clickedAsset.getId());
                    presenter.loadCurrentContents(false);
                }
            }
        });

        assetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.loadCurrentContents(false, CONTENTS_SELECTION_MODE);
                return true;
            }
        });

        pathBarRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setCurrentAssetIdToRoot();
                presenter.loadCurrentContents(false);
            }
        });

        View.OnClickListener selectionModeListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int btn_id = v.getId();

                switch (btn_id) {
                    case R.id.selection_cancel_button:
                        presenter.loadCurrentContents(false, CONTENTS_DEFAULT_MODE);
                        break;

                    case R.id.selection_select_all_button:
                        selectAll();
                        Toast.makeText(activity, adapter.getCount()
                                + " items selected", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.selection_delete_button:
                        selectedAssets = new ArrayList<>(adapter.getSelectedAssets().values());
                        if(selectedAssets.isEmpty()) {
                            Toast.makeText(activity, "Please select the assets to be deleted.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            showDeleteDialog(false);
                            presenter.loadCurrentContents(false, CONTENTS_DEFAULT_MODE);
                        }
                        break;

                    case R.id.selection_move_button:
                        //get the selected assets before quitting edit mode,
                        //or else the selectedAssetList will be empty
                        selectedAssets = new ArrayList<>(adapter.getSelectedAssets().values());

                        if(selectedAssets.isEmpty()) {
                            Toast.makeText(activity, "Please select the assets to be moved.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            presenter.loadCurrentContents(false, CONTENTS_MOVING_MODE);
                        }
                        break;
                }
            }
        };

        cancel_btn.setOnClickListener(selectionModeListener);
        selectAll_btn.setOnClickListener(selectionModeListener);
        delete_btn.setOnClickListener(selectionModeListener);
        move_btn.setOnClickListener(selectionModeListener);
    }

    private void selectAll() {
        for (int i = 0; i < adapter.getCount(); i++) {
            adapter.getSelectStatusMap().put(i, true);
        }
        adapter.notifyDataSetChanged();
    }

    private void selectNone() {
        for (int i = 0; i < adapter.getCount(); i++) {
            adapter.getSelectStatusMap().put(i, false);
        }
        adapter.notifyDataSetChanged();
    }

    //endregion
}
