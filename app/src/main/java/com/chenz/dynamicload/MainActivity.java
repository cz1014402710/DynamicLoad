package com.chenz.dynamicload;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chenz.ailddemo.IQueryName;
import com.chenz.loaderdemo.interfaces.IDynamic;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.concurrent.TimeUnit;

import dalvik.system.DexClassLoader;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    //动态加载接口
    private IDynamic lib;

    private IQueryName mQueryName;

    private QueryNameConnection mQueryNameConnection = new QueryNameConnection();

    private ReflectToast mToast;
    boolean hasShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent i = getIntent();
        String action = i.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = i.getData();
            if (uri != null) {
                String name = uri.getQueryParameter("name");
                String age = uri.getQueryParameter("age");
                Log.e("fromURL", name + "=====" + age);
            }
        }

        Intent intent = new Intent("com.chenz.name.query");
        intent.setPackage("com.chenz.ailddemo");//设置service包 否则报错
        bindService(intent, mQueryNameConnection, BIND_AUTO_CREATE);

        Button button = (Button) findViewById(R.id.button);
        requestPermissions();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lib.showToast();
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(MainActivity.this, mQueryName.queryName(2), Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        TextView textView = new TextView(this);
        textView.setText("ReflectToast");
        mToast = new ReflectToast(this, textView);

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasShow) {
                    mToast.hideToast();
                    hasShow=false;
                } else {
                    mToast.showToast();
                    hasShow=true;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(mQueryNameConnection);
        super.onDestroy();
    }

    private void requestPermissions() {
        //权限处理
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .compose(RxPermissions.getInstance(this)
                        .ensureEach(Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Permission>() {
                    @Override
                    public void onCompleted() {
                        loadDex();
                    }

                    @Override
                    public void onError(Throwable e) {
                        return;
                    }

                    @Override
                    public void onNext(Permission permission) {

                    }
                });
    }

    public void loadDex() {
        // Toast the show the method has been invoked correctly
        // Toast.makeText(getApplicationContext(), "loadDex() Method invoked", Toast.LENGTH_LONG).show();

        // name of the DEX file
        String dexFile = File.separator + "dynamic_temp.jar";

        // Get the path to the SD card
        File f = new File(Environment.getExternalStorageDirectory().toString() + dexFile);

        // optimized directory, the applciation and package directory
        final File optimizedDexOutputPath = getDir("outdex", 0);

        // DexClassLoader to get the file and write it to the optimised directory
        DexClassLoader dexClassLoader = new DexClassLoader(f.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(), null, getClassLoader());

        //dex压缩文件的路径(可以是apk,jar,zip格式)
//        String dexPath = Environment.getExternalStorageDirectory().toString() + File.separator + "dynamic_temp.jar";
//        String dexOutPath = Environment.getExternalStorageDirectory().toString();
//        DexClassLoader  dexClassLoader= new DexClassLoader(dexPath, dexOutPath, null, getClassLoader());

        try {
            Class libClass = dexClassLoader.loadClass("com.chenz.loaderdemo.impl.Dynamic");
            lib = (IDynamic) libClass.newInstance();

            if (lib != null) {
                lib.init(MainActivity.this);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


//        // The classpath is created for the new class
//        String completeClassName = "poc.example.del.mylibrary.name";
//        String methodToInvoke = "display";
//
//        try {
//            Class<?> myClass = classLoader.loadClass(completeClassName);
//            Object obj = (Object)myClass.newInstance();
//            Method m = myClass.getMethod(methodToInvoke);
//            String s = ""+m.invoke(obj);
//            makeToast(s);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            makeToast("Something went wrong!");
//        }
    }


    private final class QueryNameConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mQueryName = IQueryName.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mQueryName = null;
        }
    }


}
