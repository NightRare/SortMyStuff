package nz.ac.aut.comp705.sortmystuff.ui;

/**
 * Created by Vince on 2017/4/24.
 */

public interface IView<T extends IPresenter> {

    void setPresenter(T presenter);

}
