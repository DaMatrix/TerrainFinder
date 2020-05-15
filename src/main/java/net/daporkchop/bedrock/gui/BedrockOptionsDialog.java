/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.bedrock.gui;

import lombok.NonNull;
import net.daporkchop.bedrock.mode.SearchMode;
import net.daporkchop.bedrock.util.Rotation;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class BedrockOptionsDialog extends JDialog {
    private JPanel contentPane;
    private BedrockFrame dialog;

    public BedrockOptionsDialog(@NonNull BedrockFrame dialog) {
        super(dialog);
        this.dialog = dialog;
        this.setupUI();
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setContentPane(this.contentPane);
        this.pack();
        this.setVisible(true);
    }

    private void setupUI() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new GridLayout(3, 2));

        this.contentPane.add(new JLabel("Mode"));
        JComboBox<SearchMode> modeBox = new JComboBox<>(SearchMode.values());
        modeBox.setSelectedItem(this.dialog.mode);
        modeBox.setToolTipText(Arrays.stream(SearchMode.values())
                .map(mode -> String.format("<strong>%s</strong>: %s", mode.name(), mode.description()))
                .collect(Collectors.joining("<br>", "<html>", "</html>")));
        modeBox.addItemListener(e -> {
            SearchMode newMode = (SearchMode) e.getItem();
            if (newMode != this.dialog.mode) {
                this.dialog.mode = newMode;
                System.out.println("Changed to " + newMode);
                this.dialog.refreshTable();
            }
        });
        this.contentPane.add(modeBox);

        this.contentPane.add(new JLabel("Rotation"));
        JComboBox<Rotation> rotBox = new JComboBox<>(Rotation.values());
        rotBox.setSelectedItem(this.dialog.rotation);
        rotBox.addItemListener(e -> {
            Rotation newMode = (Rotation) e.getItem();
            if (newMode != this.dialog.rotation) {
                this.dialog.rotation = newMode;
                System.out.println("Changed to " + newMode);
            }
        });
        this.contentPane.add(rotBox);

        this.contentPane.add(new JLabel("Threads"));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));
        JLabel label = new JLabel(String.valueOf(this.dialog.threads));
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 1, Runtime.getRuntime().availableProcessors(), this.dialog.threads);
        slider.addChangeListener(e -> label.setText(String.valueOf(this.dialog.threads = slider.getValue())));
        panel.add(label, BorderLayout.EAST);
        panel.add(slider, BorderLayout.CENTER);
        this.contentPane.add(panel);
    }
}
