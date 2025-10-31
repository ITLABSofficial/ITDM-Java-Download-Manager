package com.itlabs.itdm.core;

import java.util.Map;
import java.util.concurrent.*;

public class DownloadManager implements DownloadTask.Listener {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final Map<String, DownloadTask> tasks = new ConcurrentHashMap<>();
    private final java.util.function.Consumer<Download> onUpdate;
    private final java.util.function.BiConsumer<Download, Exception> onError;

    public DownloadManager(java.util.function.Consumer<Download> onUpdate,
                           java.util.function.BiConsumer<Download, Exception> onError) {
        this.onUpdate = onUpdate;
        this.onError  = onError;
    }

    public void start(Download d) {
        DownloadTask task = new DownloadTask(d, this);
        tasks.put(d.getId(), task);
        pool.submit(task);
    }

    public void pause(Download d) {
        var t = tasks.get(d.getId());
        if (t != null) t.pause();
    }

    public void resume(Download d) {
        var t = tasks.get(d.getId());
        if (t != null) t.resume();
        else start(d);
    }

    public void cancel(Download d) {
        var t = tasks.get(d.getId());
        if (t != null) t.cancel();
    }

    @Override public void onProgress(Download d) { onUpdate.accept(d); }
    @Override public void onDone(Download d)     { onUpdate.accept(d); tasks.remove(d.getId()); }
    @Override public void onError(Download d, Exception e) { onError.accept(d, e); tasks.remove(d.getId()); }

    public void shutdown() { pool.shutdownNow(); }
}
