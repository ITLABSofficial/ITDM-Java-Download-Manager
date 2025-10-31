package com.itlabs.itdm.ui;

import com.itlabs.itdm.core.Download;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class DownloadTableModel extends AbstractTableModel {

    private final List<Download> items = new ArrayList<>();
    private final String[] cols = {"URL", "Dosya", "Durum", "İlerleme", "Hız (KB/s)"};

    @Override public int getRowCount() { return items.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    public Download getAt(int row) { return items.get(row); }

    public void add(Download d) {
        items.add(d);
        fireTableRowsInserted(items.size() - 1, items.size() - 1);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Download d = items.get(row);
        switch (col) {
            case 0: return d.getUri().toString();
            case 1: return d.getFilePath();
            case 2: return d.getStatus().name();
            case 3:
                long tot = d.getTotalBytes(), cur = d.getDownloadedBytes();
                return (tot <= 0 ? "-" : String.format("%.1f%%", (cur * 100.0) / tot));
            case 4:
                return String.format("%.1f", d.getSpeedBytesPerSec() / 1024.0);
            default: return "";
        }
    }

    public void refreshRow(Download d) {
        int idx = items.indexOf(d);
        if (idx >= 0) fireTableRowsUpdated(idx, idx);
    }
}
