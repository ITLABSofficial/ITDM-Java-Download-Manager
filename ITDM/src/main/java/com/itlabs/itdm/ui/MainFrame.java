package com.itlabs.itdm.ui;

import com.itlabs.itdm.core.Download;
import com.itlabs.itdm.core.DownloadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;

public class MainFrame extends JFrame {
    private final DownloadTableModel model = new DownloadTableModel();
    private final JTable table = new JTable(model);
    private final DownloadManager manager;

    public MainFrame() {
        super("ITDM – itLabs");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        manager = new DownloadManager(
                d -> model.refreshRow(d),
                (d, e) -> {
                    model.refreshRow(d);
                    JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(),
                            "İndirme Hatası", JOptionPane.ERROR_MESSAGE);
                });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn    = new JButton("Yeni");
        JButton startBtn  = new JButton("Başlat");
        JButton pauseBtn  = new JButton("Duraklat");
        JButton resumeBtn = new JButton("Devam");
        JButton cancelBtn = new JButton("İptal");
        JButton githubBtn = new JButton("GitHub Project");

        addBtn.addActionListener(e -> onAdd());
        startBtn.addActionListener(e -> withSelected(manager::start));
        pauseBtn.addActionListener(e -> withSelected(manager::pause));
        resumeBtn.addActionListener(e -> withSelected(manager::resume));
        cancelBtn.addActionListener(e -> withSelected(manager::cancel));
        githubBtn.addActionListener(e -> openGitHub());

        top.add(addBtn);
        top.add(startBtn);
        top.add(pauseBtn);
        top.add(resumeBtn);
        top.add(cancelBtn);
        top.add(githubBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void onAdd() {
        AddDownloadDialog dlg = new AddDownloadDialog(this);
        dlg.setVisible(true);
        URI uri = dlg.getUri();
        String path = dlg.getPath();
        if (uri != null && path != null && !path.isBlank()) {
            Download d = new Download(uri, path);
            model.add(d);
        }
    }

    private void withSelected(java.util.function.Consumer<Download> fn) {
        int row = table.getSelectedRow();
        if (row >= 0) fn.accept(model.getAt(row));
    }

    private void openGitHub() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/ITLABSofficial"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "GitHub sayfası açılamadı:\n" + ex.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
