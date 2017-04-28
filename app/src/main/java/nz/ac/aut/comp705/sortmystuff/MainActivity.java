package nz.ac.aut.comp705.sortmystuff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        IFactory factory = ((SortMyStuffApp)getApplication()).getFactory();
        IDataManager dm = factory.getDataManager();
    }
}
