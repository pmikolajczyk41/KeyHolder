package pmikolajczyk.keyholder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import pmikolajczyk.keyholder.locale.LocaleResolver;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleResolver.updateLocale(base));
    }
}
