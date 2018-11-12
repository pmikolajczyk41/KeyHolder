package pmikolajczyk.keyholder;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import pmikolajczyk.keyholder.locale.LocaleResolver;

public class KeyHolderApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleResolver.updateLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleResolver.updateLocale(this);
    }
}
