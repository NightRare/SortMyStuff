package nz.ac.aut.comp705.sortmystuff.ui;

/**
 * The base interface of all the Presenters. Presenters are responsible for controlling the
 * procedure of the corresponding activity, communicating with IDataManager and persisting state
 * data of the activity.
 *
 * @author Yuan
 */

public interface IPresenter {

    /**
     * When the corresponding activity is on created or on resume, this method will be invoked.
     */
    void start();

    /**
     * When the corresponding activity is on pause, this method will be invoked.
     */
    void unsubscribe();
}
