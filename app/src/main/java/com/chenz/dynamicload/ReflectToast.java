package com.chenz.dynamicload;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * description: <一句话功能简述>
 *
 * @author Chenz
 * @date 2018/1/5
 */
public class ReflectToast {

    private Toast mToast;

    private Context mContext;
    private Field mField;
    private Method hideMethod, showMethod;
    private Object obj;

    public ReflectToast(Context context, View view) {
        mToast = new Toast(context);
        mToast.setView(view);

        reflectTN();
    }

    private void reflectTN() {
        try {
            mField = mToast.getClass().getDeclaredField("mTN");
            mField.setAccessible(true);
            obj = mField.get(mToast);
            showMethod = obj.getClass().getDeclaredMethod("show", null);
            hideMethod = obj.getClass().getDeclaredMethod("hide", null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void showToast(){
        try {
            showMethod.invoke(obj,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void hideToast(){
        try {
            hideMethod.invoke(obj,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
