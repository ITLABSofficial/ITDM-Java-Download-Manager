package com.itlabs.itdm.core;

import javax.swing.SwingUtilities;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;

public class DownloadTask implements Runnable {

    public interface Listener {
        void onProgress(Download d);
        void onDone(Download d);
        void onError(Download d, Exception e);
    }

    private final HttpClient client;
    private final Download d;
    private final Listener listener;
    private volatile boolean paused = false;
    private volatile boolean canceled = false;

    public DownloadTask(Download d, Listener listener) {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.d = d;
        this.listener = listener;
    }

    public void pause()  { paused = true; d.setStatus(Download.Status.PAUSED); fireProgress(); }
    public void resume() { paused = false; }
    public void cancel() { canceled = true; }

    @Override
    public void run() {
        try {
            d.setStatus(Download.Status.QUEUED);

            HttpRequest head = HttpRequest.newBuilder(d.getUri())
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<Void> headResp = client.send(head, HttpResponse.BodyHandlers.discarding());
            var headers = headResp.headers();

            long contentLen = headers.firstValueAsLong("content-length").orElse(-1);
            boolean acceptsRange = headers.firstValue("accept-ranges")
                    .map(v -> v.toLowerCase().contains("bytes")).orElse(false);

            d.setTotalBytes(contentLen);
            d.setAcceptsRange(acceptsRange);

            long resumeFrom = d.getDownloadedBytes();
            if (!acceptsRange && resumeFrom > 0) resumeFrom = 0;

            d.setStatus(Download.Status.DOWNLOADING);
            fireProgress();

            try (RandomAccessFile raf = new RandomAccessFile(d.getFilePath(), "rw");
                 FileChannel ch = raf.getChannel()) {

                if (contentLen > 0) raf.setLength(contentLen);

                long written = resumeFrom;
                long lastTickBytes = written;
                long lastTickTime = System.nanoTime();

                while (!canceled && (contentLen < 0 || written < contentLen)) {
                    if (paused) { sleep(150); continue; }

                    String rangeHeader = acceptsRange
                            ? "bytes=" + written + "-" + (contentLen > 0 ? (contentLen - 1) : "")
                            : null;

                    HttpRequest.Builder rb = HttpRequest.newBuilder(d.getUri()).GET();
                    if (rangeHeader != null) rb.header("Range", rangeHeader);
                    HttpRequest req = rb.build();

                    HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                    int code = resp.statusCode();
                    if (code != 200 && code != 206) throw new RuntimeException("HTTP " + code);

                    try (InputStream in = resp.body()) {
                        byte[] buf = new byte[1 << 16]; // 64 KB
                        int n;
                        while (!paused && !canceled && (n = in.read(buf)) != -1) {
                            ByteBuffer bb = ByteBuffer.wrap(buf, 0, n);
                            ch.position(written);
                            while (bb.hasRemaining()) ch.write(bb);
                            written += n;
                            d.setDownloadedBytes(written);

                            long now = System.nanoTime();
                            if (now - lastTickTime >= 300_000_000L) { // ~300 ms
                                double secs = (now - lastTickTime) / 1_000_000_000.0;
                                double speed = (written - lastTickBytes) / secs;
                                d.setSpeedBytesPerSec(speed);
                                lastTickBytes = written; lastTickTime = now;
                                fireProgress();
                            }
                            if (contentLen > 0 && written >= contentLen) break;
                        }
                    }
                }
            }

            if (canceled) {
                d.setStatus(Download.Status.CANCELED);
                d.setSpeedBytesPerSec(0);
                fireProgress();
                listener.onDone(d);
                return;
            }

            d.setStatus(Download.Status.COMPLETED);
            d.setSpeedBytesPerSec(0);
            fireProgress();
            listener.onDone(d);

        } catch (Exception e) {
            d.setStatus(Download.Status.FAILED);
            d.setSpeedBytesPerSec(0);
            fireProgress();
            listener.onError(d, e);
        }
    }

    private void fireProgress() {
        SwingUtilities.invokeLater(() -> listener.onProgress(d));
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
