package org.tinybag.www.myapplication;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.app.RxActivity;
import rx.android.lifecycle.LifecycleObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity  extends RxActivity {
    private static final String TEST_URL = "http://www.example.com";

    private TextView mTextView;

    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(android.R.id.text1);

        ((Button) findViewById(R.id.rxjava)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("");
                loadDataRxJava();
                mTextView.setText("");
                loadDataRxJava();
            }
        });

        ((Button) findViewById(R.id.rxandroid2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("");
                loadDataRxAndroid2();
                mTextView.setText("");
                loadDataRxAndroid2();
            }
        });

        ((Button) findViewById(R.id.rxandroid1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("");
                loadDataRxAndroid1();
                mTextView.setText("");
                loadDataRxAndroid1();
            }
        });

        ((Button) findViewById(R.id.rxasynctask)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataAsyncTask();

            }
        });

   //     loadDataAsyncTask(); // AsyncTask

//        loadDataRxJava(); // RxJava

//        loadDataRxAndroid1(); // RxAndroid AppObservable

      //  loadDataRxAndroid2(); // RxAndroid LifecycleObservable
    }

    @Override
    protected void onDestroy() {
        // onDestroy() might not be the best place to unsubscribe, demonstration purpose only
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }

        super.onDestroy();
    }

    //
    //
    //

    /**
     * Load http://www.example.com/ as String into TextView.
     * <p/>
     * AsyncTask Version
     */
    private void loadDataAsyncTask() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection con = null;
                Scanner scanner = null;
                try {
                    con = (HttpURLConnection) new URL(TEST_URL).openConnection();
                    scanner = new Scanner(con.getInputStream());
                    try{Thread.sleep(1000);}catch (Exception ex){}
                    return scanner.useDelimiter("\\A").next();
                } catch (IOException e) {
                    return e.getMessage();
                } finally {
                    if (scanner != null) {
                        scanner.close();
                    }
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                mTextView.setText(s);
            }
        }.execute();
    }


    /**
     * Load http://www.example.com/ as String into TextView.
     * <p/>
     * RxJava Version
     */
    private void loadDataRxJava() {
        // must explicitly call unsubscribe() on mSubscription
        // doesn't know anything about Android's lifecycle
        mSubscription = createObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        // no-op
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e != null) {
                            mTextView.setText(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        mTextView.setText(s);
                    }
                });
    }

    /**
     * Load http://www.example.com/ as String into TextView.
     * <p/>
     * RxAndroid Version
     */
    private void loadDataRxAndroid1() {
        // must explicitly call unsubscribe() on mSubscription
        // prevents items from being emitted past the proper point in the lifecycle
        // observed on UI thread
        final Observable<String> o = AppObservable.bindActivity(this, createObservable());
        mSubscription = o.subscribeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                // no-op
            }

            @Override
            public void onError(Throwable e) {
                if (e != null) {
                    mTextView.setText(e.getMessage());
                }
            }

            @Override
            public void onNext(String s) {
                mTextView.setText(s);
            }
        });
    }

    /**
     * Load http://www.example.com/ as String into TextView.
     * <p/>
     * RxAndroid Framework Version
     */
    public void loadDataRxAndroid2() {
        // need to extend rx.android.app.RxActivity
        // unsubscribe() is automatically handled
        // DOES NOT prevent items from being emitted past the proper point in the lifecycle
        LifecycleObservable.bindActivityLifecycle(lifecycle(), createObservable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        // no-op
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e != null) {
                            mTextView.setText(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(String s) {
                        mTextView.setText(s);
                    }
                });
    }

    //
    //
    //

    /**
     * Creates an Observable instance for loadDataRxJava and loadDataRxAndroid.
     */
    private Observable<String> createObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                HttpURLConnection con = null;
                Scanner scanner = null;
                try {
                    con = (HttpURLConnection) new URL(TEST_URL).openConnection();
                    scanner = new Scanner(con.getInputStream());
                    try{Thread.sleep(1000);}catch (Exception ex){}
                    subscriber.onNext(scanner.useDelimiter("\\A").next());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                } finally {
                    if (scanner != null) {
                        scanner.close();
                    }
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        });
    }
}
