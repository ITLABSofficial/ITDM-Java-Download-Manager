package com.itlabs.itdm.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;

public class AddDownloadDialog extends JDialog {
    private final JTextField urlField  = new JTextField();
    private final JTextField pathField = new JTextField();

    public AddDownloadDialog(Frame owner) {
        super(owner, "Yeni İndirme", true);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(0,1,6,6));
        form.add(new JLabel("URL:"));
        form.add(urlField);
        form.add(new JLabel("Kaydetme Yolu:"));

        JPanel pathRow = new JPanel(new BorderLayout(6,6));
        pathRow.add(pathField, BorderLayout.CENTER);
        JButton browse = new JButton("Gözat...");
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("indirilen-dosya.bin"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        pathRow.add(browse, BorderLayout.EAST);
        form.add(pathRow);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Ekle");
        JButton cancel = new JButton("İptal");
        ok.addActionListener(e -> dispose());
        cancel.addActionListener(e -> { urlField.setText(""); pathField.setText(""); dispose(); });

        btns.add(cancel); btns.add(ok);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setSize(520, 220);
        setLocationRelativeTo(owner);
    }

    public URI getUri() {
        try { return new URI(urlField.getText().trim()); }
        catch (Exception e){ return null; }
    }
    public String getPath() { return pathField.getText().trim(); }
}
