package com.itlabs.itdm.core;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public class Download {
    public enum Status { NEW, QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELED }

    private final String id = UUID.randomUUID().toString();
    private final URI uri;
    private final String filePath;

    private volatile Status status = Status.NEW;
    private volatile long totalBytes = -1;
    private volatile long downloadedBytes = 0;
    private volatile boolean acceptsRange = false;
    private volatile double speedBytesPerSec = 0.0;

    private final Instant createdAt = Instant.now();

    public Download(URI uri, String filePath) {
        this.uri = uri;
        this.filePath = filePath;
    }

    public String getId() { return id; }
    public URI getUri() { return uri; }
    public String getFilePath() { return filePath; }
    public Status getStatus() { return status; }
    public long getTotalBytes() { return totalBytes; }
    public long getDownloadedBytes() { return downloadedBytes; }
    public boolean isAcceptsRange() { return acceptsRange; }
    public double getSpeedBytesPerSec() { return speedBytesPerSec; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(Status s) { this.status = s; }
    public void setTotalBytes(long t) { this.totalBytes = t; }
    public void setDownloadedBytes(long d) { this.downloadedBytes = d; }
    public void setAcceptsRange(boolean b) { this.acceptsRange = b; }
    public void setSpeedBytesPerSec(double v) { this.speedBytesPerSec = v; }

    @Override
    public String toString() {
        return "Download[" + uri + ", " + status + "]";
    }
}
