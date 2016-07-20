package cz.mendelu.argeo;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Provides;

/**
 * @author adamb_000
 * @since 19. 7. 2016
 */

@Singleton
public class App extends Application{

    private static CameraProvider sCameraProvider;
    private static App sApp;

    @Provides
    @Singleton
    public static App getInstance(){
//        if (sApp == null)
//        {
//            synchronized (App.class)
//            {
//                if (sApp == null)
//                {
//                    sApp = new App();
//                }
//            }
//        }
//        return sApp;
        return new App();
    }

    @Provides
    @Singleton
    public CameraProvider getCameraProvider(){
//        if (sCameraProvider == null)
//        {
//            synchronized (CameraProvider.class)
//            {
//                if (sCameraProvider == null)
//                {
//                    sCameraProvider = new CameraProvider();
//                }
//            }
//        }
//        return sCameraProvider;
        return new CameraProvider();
    }

}
