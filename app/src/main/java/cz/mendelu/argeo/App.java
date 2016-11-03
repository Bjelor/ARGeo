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

    public final static String WIKITUDE_KEY = "okLQUponz7zaQ5sYMrlnr1tAoluxVu0VOFZh3AMe2LuLhdSE" +
            "Mdb2xHhoMa0/y4RazHSJLaq+tWP46KFNII8M8Du1UG+cjwRbRRm7uHT1hOxIj8HQtIrCquVk8UbRJvW178H" +
            "8Wg/EPWpMuz4riyVMFyFWM5SKE2z+bdA99udI1MhTYWx0ZWRfX3IhlojA4mYspKwEKqeXbLgwzErj1tVGVG" +
            "JPEe25gJptPG/yE42Il4WS0oYHcDhqzNVIgAbwWTJJNpEc47NXmmNYzXG+UZTYd0N8kck4lepAt1v404rJW" +
            "vdQJDTlqN1Tt5gXcNFx0+4gxnXueMFMAUmi8P4STC08vxprOAuZwEGpY2YNQFRa+FdSv5poYLyt3ZGHv2c/" +
            "Z6jKVGJPD/dutXoMNEPeogilRQV+iaZbhXQSGiMUovHkYPm79h0Wi9P90Dn7zcDWvHTysO2cs0M6kns3+fO" +
            "/BZoa0cAFjgvE0QNEyaRGJ5llZ9CRCQLquXpwFYjprKJQ8t4RrilkDSPE7UqS5cpuzzK6QJKMwn1N14LBI4" +
            "djc2s1RtYjaOa5bWqrHqs5c0CjWqOdEk33QIL9pn3hUdVdATur8kdCirxkmVoiihYX3DJXq+KGta4ftLZba" +
            "LRf/diflP+8yt91DU9nDNyDKISwf3Do/mucmVmhAYMlh6QWnyOcbos=";

//    private static CameraProvider sCameraProvider;
//    private static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
//        WikitudeSDK wikitudeSDK = new WikitudeSDK();
//        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WIKITUDE_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
//        wikitudeSDK.onCreate(getApplicationContext(), startupConfiguration);
    }

//    @Provides
//    @Singleton
//    public static App getInstance(){
////        if (sApp == null)
////        {
////            synchronized (App.class)
////            {
////                if (sApp == null)
////                {
////                    sApp = new App();
////                }
////            }
////        }
////        return sApp;
//        return new App();
//    }
//
//    @Provides
//    @Singleton
//    public CameraProvider getCameraProvider(){
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
////        return new CameraProvider();
//    }

}
