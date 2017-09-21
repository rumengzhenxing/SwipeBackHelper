
package com.jude.swipbackhelper;

import android.app.Activity;
import android.app.ActivityOptions;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Chaojun Wang on 6/9/14.
 */
public class Utils {
    private Utils() {
    }

    /**
     * Convert a translucent themed Activity
     * {@link android.R.attr#windowIsTranslucent} to a fullscreen opaque
     * Activity.
     * <p>
     * Call this whenever the background of a translucent Activity has changed
     * to become opaque. Doing so will allow the {@link android.view.Surface} of
     * the Activity behind to be released.
     * <p>
     * This call has no effect on non-translucent activities or on activities
     * with the {@link android.R.attr#windowIsFloating} attribute.
     */
    public static boolean convertActivityFromTranslucent(Activity activity) {
        try {
            Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
            method.setAccessible(true);
            method.invoke(activity);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }


    public static void convertActivityToTranslucent(Activity activity, PageTranslucentListener listener) {
        try {
            Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
            getActivityOptions.setAccessible(true);
            Object options = getActivityOptions.invoke(activity);

            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }

            MyInvocationHandler myInvocationHandler = new MyInvocationHandler(new WeakReference<PageTranslucentListener>(listener));
            Object obj = Proxy.newProxyInstance(Activity.class.getClassLoader(), new Class[]{translucentConversionListenerClazz}, myInvocationHandler);

            Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent",
                    translucentConversionListenerClazz, ActivityOptions.class);
            convertToTranslucent.setAccessible(true);
            Log.d("MyInvocationHandler", "start time: " + System.currentTimeMillis());
            convertToTranslucent.invoke(activity, obj, options);
        } catch (Throwable t) {
        }
    }

    public interface PageTranslucentListener {
        void onPageTranslucent();
    }

    public static class MyInvocationHandler implements InvocationHandler {

        private WeakReference<PageTranslucentListener> listener;

        public MyInvocationHandler(WeakReference<PageTranslucentListener> listener) {
            this.listener = listener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                boolean success = (boolean) args[0];
                if (success && listener.get() != null) {
                    listener.get().onPageTranslucent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
