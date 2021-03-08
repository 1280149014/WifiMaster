package com.longquan.common.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by shengjieli on 17-10-31.<br>
 * Email address: shengjieli@ecarx.com.cn<br>
 * todo 将所有设置相关的子线程读取数据的问题都综合处理到这里
 * 可以添加各种使用方式
 */

public class SyncUtil {

    public static final String TAG = SyncUtil.class.getSimpleName();

    public static final ExecutorService CACHE_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    //为了方便继承，使用了RxAndroid-wiki页面的RxLifecycle组件，需要继承相应的类
//    public static <T> void startRead(RxFragment rxFragment,
//                                     Callable<T> doInBackground, Consumer<Timed<T>> onPostExecute) {
//
//        Observable.fromCallable(doInBackground)
//                .timestamp()
//                .subscribeOn(Schedulers.from(CACHE_EXECUTOR))
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(rxFragment.<Timed<T>>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                .subscribe(onPostExecute);
//    }

    //设置中的很多页面，其实都是读取数据的时候直接更新数据
    //所以后台运行的Callable.call方法其实并不需要返回更新之后的数据
    //目前以ReadBean来暂做触发Consumer.accept使用
    public static class ReadBean {

    }

}
