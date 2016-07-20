package cz.mendelu.argeo;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Provides;

/**
 * @author adamb_000
 * @since 20. 7. 2016
 */
//TODO:do further research on this until used in practice
public class AppModule {

    protected final Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @AppContext
    Context provideContext() {
        return mApplication;
    }


    @Provides
    @Singleton
    CameraProvider providesCameraProvider() {
        return new CameraProvider();
    }

}
