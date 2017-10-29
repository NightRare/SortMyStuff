package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {FactoryModule.class, FactoryBindingModule.class})
public interface FactoryComponent {

    Application application();

    void inject(Factory factory);
}
