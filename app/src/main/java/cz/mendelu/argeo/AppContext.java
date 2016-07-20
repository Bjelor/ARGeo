package cz.mendelu.argeo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * @author adamb_000
 * @since 20. 7. 2016
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface AppContext {
}
