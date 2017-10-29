package nz.ac.aut.comp705.sortmystuff.di;

import dagger.Binds;
import dagger.Module;
import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.remote.FirebaseHelper;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.ImmediateScheduler;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.RegularScheduler;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ImmediateSchedulerProvider;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.SchedulerProvider;

@Module
public abstract class FactoryBindingModule {

    @Binds
    @RegularScheduler
    public abstract ISchedulerProvider bindSchedulerProvider(SchedulerProvider schedulerProvider);

    @Binds
    @ImmediateScheduler
    public abstract ISchedulerProvider bindImmediateSchedulerProvider(ImmediateSchedulerProvider immediateSchedulerProvider);

    @Binds
    public abstract IDataRepository bindRemoteDatabaseRepository(FirebaseHelper firebaseHelper);

    @Binds
    public abstract IDataManager bindDataManager(DataManager dataManager);
}
