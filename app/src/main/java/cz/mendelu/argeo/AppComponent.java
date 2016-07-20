package cz.mendelu.argeo;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author adamb_000
 * @since 20. 7. 2016
 */

//TODO:do further research on this until used in practice
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(CameraProvider cameraProvider);

    Context context();

    Application application();

    CameraProvider cameraProvider();

}
