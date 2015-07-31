package com.ysy.ysywb.support.asyncdrawable;

import com.ysy.ysywb.support.database.DownloadPicturesDBTask;
import com.ysy.ysywb.support.file.FileDownloaderHttpHelper;
import com.ysy.ysywb.support.file.FileLocationMethod;
import com.ysy.ysywb.support.file.FileManager;
import com.ysy.ysywb.support.imageutility.ImageUtility;
import com.ysy.ysywb.support.utils.Utility;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ggec5486 on 2015/7/30.
 */
public class TaskCache {

    private static final ThreadFactory sDownloadThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadFutureTask Download #" + mCount.getAndIncrement());
        }
    };

    private static ConcurrentHashMap<String, DownloadFutureTask> downloadTasks
            = new ConcurrentHashMap<String, DownloadFutureTask>();

    public static boolean isThisUrlTaskFinished(String url) {
        return !downloadTasks.containsKey(url);
    }

    public static final Object backgroundWifiDownloadPicturesWorkLock = new Object();

    public static void removeDownloadTask(String url, DownloadFutureTask downloadWorker) {
        synchronized (TaskCache.backgroundWifiDownloadPicturesWorkLock) {
            downloadTasks.remove(url, downloadWorker);
            if (TaskCache.isDownloadTaskFinished()) {
                TaskCache.backgroundWifiDownloadPicturesWorkLock.notifyAll();
            }
        }
    }

    public static boolean isDownloadTaskFinished() {
        return TaskCache.downloadTasks.isEmpty();
    }

    private static final Executor DOWNLOAD_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(4, 4, 1,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(15) {
        @Override
        public boolean offer(Runnable runnable) {
            return super.offerFirst(runnable);
        }
    }, sDownloadThreadFactory,
            new ThreadPoolExecutor.DiscardOldestPolicy() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    if (!e.isShutdown()) {
                        LinkedBlockingDeque<Runnable> deque = (LinkedBlockingDeque) e.getQueue();
                        Runnable runnable = deque.pollLast();
                        if (runnable instanceof DownloadFutureTask) {
                            DownloadFutureTask futureTask = (DownloadFutureTask) runnable;
                            futureTask.cancel(true);
                            String url = futureTask.getUrl();
                            removeDownloadTask(url, futureTask);
                        }
                        e.execute(r);
                    }
                }
            }
    );

    public static boolean waitForPictureDownload(String url,
                                                 FileDownloaderHttpHelper.DownloadListener downloadListener, String savedPath,
                                                 FileLocationMethod method) {
        while (true) {
            DownloadFutureTask downloadFutureTask = TaskCache.downloadTasks.get(url);

            if (downloadFutureTask == null) {

                DownloadFutureTask newDownloadFutureTask = DownloadFutureTask
                        .newInstance(url, method);
                synchronized (backgroundWifiDownloadPicturesWorkLock) {
                    downloadFutureTask = TaskCache.downloadTasks
                            .putIfAbsent(url, newDownloadFutureTask);
                }
                if (downloadFutureTask == null) {
                    downloadFutureTask = newDownloadFutureTask;
                    DOWNLOAD_THREAD_POOL_EXECUTOR.execute(downloadFutureTask);
                }
            }

            downloadFutureTask.addDownloadListener(downloadListener);

            try {
                return downloadFutureTask.get();
            } catch (InterruptedException e) {
                Utility.printStackTrace(e);
                //for better listview scroll performance
                downloadFutureTask.cancel(true);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                Utility.printStackTrace(e);
                return false;
            } catch (CancellationException e) {
                removeDownloadTask(url, downloadFutureTask);
            }
        }
    }

    private JobCallable callable;

    public void addDownloadListener(FileDownloaderHttpHelper.DownloadListener listener) {
        callable.addDownloadListener(listener);
    }

    private static class JobCallable implements Callable<Boolean> {

        private DownloadFutureTask futureTask;

        private CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener> downloadListenerList
                = new CopyOnWriteArrayList<FileDownloaderHttpHelper.DownloadListener>();

        private String url;

        private FileLocationMethod method;

        private int progress;
        private int max;

        public void addDownloadListener(FileDownloaderHttpHelper.DownloadListener listener) {
            if (listener == null) {
                return;
            }
            downloadListenerList.addIfAbsent(listener);
            if (progress > 0 && max > 0) {
                listener.pushProgress(progress, max);
            }
        }

        private JobCallable(String url, FileLocationMethod method) {
            this.url = url;
            this.method = method;
        }

        @Override
        public Boolean call() throws Exception {
            synchronized (TimeLineBitmapDownloader.pauseDownloadWorkLock) {
                while (TimeLineBitmapDownloader.pauseDownloadWork && !Thread.currentThread()
                        .isInterrupted()) {
                    try {
                        TimeLineBitmapDownloader.pauseDownloadWorkLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            String filePath = FileManager.generateDownloadFileName(url);

            String actualDownloadUrl = url;

            switch (method) {
                case picture_thumbnail:
                    actualDownloadUrl = url.replace("thumbnail", "webp180");
                    break;
                case picture_bmiddle:
                    actualDownloadUrl = url.replace("bmiddle", "webp720");
                    break;
                case picture_large:
                    actualDownloadUrl = url.replace("large", "woriginal");
                    break;
            }

            boolean result = ImageUtility.getBitmapFromNetWork(actualDownloadUrl, filePath,
                    new FileDownloaderHttpHelper.DownloadListener() {
                        @Override
                        public void pushProgress(int progress, int max) {
                            JobCallable.this.progress = progress;
                            JobCallable.this.max = max;
                            for (FileDownloaderHttpHelper.DownloadListener downloadListener : downloadListenerList) {
                                if (downloadListener != null) {
                                    downloadListener.pushProgress(progress, max);
                                }
                            }
                        }
                    });

            if (result) {
                DownloadPicturesDBTask.add(this.url,
                        FileManager.generateDownloadFileName(this.url),
                        this.method);
            }

            TaskCache.removeDownloadTask(url, futureTask);
            return result;
        }
    }
}
