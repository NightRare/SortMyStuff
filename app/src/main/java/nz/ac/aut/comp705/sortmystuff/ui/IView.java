package nz.ac.aut.comp705.sortmystuff.ui;

/**
 * The base interface of all the Views. Views are responsible for displaying the contents as
 * requested by the corresponding Presenter as well as informing the presenter when any interaction
 * is performed on the UI.
 *
 * @author Yuan
 */

public interface IView<T extends IPresenter> {

    /**
     * Registers the presenter to this View.
     *
     * @param presenter the presenter
     */
    void setPresenter(T presenter);
}
