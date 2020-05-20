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

import net.daporkchop.bedrock.Search;
import net.daporkchop.bedrock.mode.SearchMode;
import net.daporkchop.bedrock.util.Constants;
import net.daporkchop.bedrock.util.Rotation;
import net.daporkchop.lib.common.system.OperatingSystem;
import net.daporkchop.lib.common.system.PlatformInfo;
import net.daporkchop.lib.common.util.PArrays;
import net.daporkchop.lib.common.util.PorkUtil;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import static net.daporkchop.bedrock.util.Constants.*;

public class BedrockFrame extends JFrame {
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {
        try {
            UIManager.setLookAndFeel(PlatformInfo.OPERATING_SYSTEM == OperatingSystem.Windows ? UIManager.getSystemLookAndFeelClassName() : UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel contentPane;
    private JLabel scannedCount;
    private JButton actionButton;
    private JPanel footer;
    private JPanel content;
    protected SearchMode mode;
    protected Rotation rotation;
    protected int threads = Runtime.getRuntime().availableProcessors();
    private TriStateCheckBox[][] boxes;
    private Search search;

    public BedrockFrame() {
        this.setupUI();
        this.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        this.setContentPane(this.contentPane);
        this.getRootPane().setDefaultButton(this.actionButton);

        this.actionButton.addActionListener(e -> this.onClick());
        this.pack();
        this.setVisible(true);
    }

    private synchronized void onClick() {
        this.actionButton.setEnabled(false);
        if (this.search == null || this.search.completedFuture().isDone()) {
            this.actionButton.setText("Starting...");
            System.out.println("Starting search for mode " + this.mode);
            int size = this.mode.size();
            byte[] pattern = new byte[size * size];
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int state = this.boxes[x][z].getSelectionState();
                    int a = state;
                    switch (a) {
                        case 1:
                            state = 2;
                            break;
                        case 2:
                            state = 1;
                            break;
                    }
                    pattern[x * size + z] = (byte) state;
                }
            }

            this.search = new Search(
                    null,
                    this.mode.create(pattern, this.rotation),
                    (x, z) -> {
                        synchronized (this.search) {
                            return JOptionPane.showOptionDialog(
                                    this,
                                    "Found match at x=" + (x << 4) + ", z=" + (z << 4) + "(chunk: x=" + x + ", z=" + z + ")",
                                    "Found match",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    new Object[]{"Continue search", "Stop"},
                                    null) == JOptionPane.OK_OPTION;
                        }
                    });
            this.search.start(this.threads);

            new Thread(() -> {
                this.actionButton.setEnabled(true);

                long iterations = 0L;
                double[] speeds = new double[150]; //15 seconds
                long lastTime = System.nanoTime();
                long lastProcessed = 0L;

                while (!this.search.completedFuture().isDone()) {
                    long now = System.nanoTime();
                    long processedNow = this.search.processed() * TILE_SIZE * TILE_SIZE;
                    long processed = processedNow - lastProcessed;
                    lastProcessed = processedNow;
                    long timeDelta = now - lastTime;
                    lastTime = now;
                    System.arraycopy(speeds, 0, speeds, 1, speeds.length - 1);
                    if (iterations++ > 10L) {
                        speeds[0] = (double) processed * TimeUnit.SECONDS.toNanos(1L) / (double) timeDelta;
                    }

                    this.actionButton.setText("Stop");
                    this.scannedCount.setText(String.format(
                            "%s chunks (%s/s) - %s blocks from spawn",
                            NUMBER_FORMAT.format(processedNow),
                            NUMBER_FORMAT.format(DoubleStream.of(speeds).sum() / (double) speeds.length),
                            NUMBER_FORMAT.format(Math.max(extractX(processedNow), extractZ(processedNow)))));
                    PorkUtil.sleep(100L);
                }

                this.actionButton.setText("Start");
            }, "GUI updater worker").start();
        } else {
            this.search.completedFuture().cancel(true);
            this.actionButton.setText("Start");
            this.actionButton.setEnabled(true);
        }
    }

    public void refreshTable() {
        this.content.removeAll();
        this.content.setLayout(new GridLayout(this.mode.size(), this.mode.size()));
        this.boxes = new TriStateCheckBox[this.mode.size()][this.mode.size()];

        for (int x = 0; x < this.mode.size(); x++) {
            for (int z = 0; z < this.mode.size(); z++) {
                this.content.add(this.boxes[x][z] = new TriStateCheckBox());
            }
        }

        if (true)   {
            switch (this.mode)  {
                case FULL: {
                    long state = seedBedrock(123, -456);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            this.boxes[x][z].setSelectionState(flagBedrock(state) << 1);
                            state = Constants.updateBedrock(state);
                        }
                    }
                }
                break;
                case SUB: {
                    int[] grid = {0,0,1,0,0,0,1,0,
                            0,0,0,0,0,0,0,0,
                            0,0,1,1,0,0,0,0,
                            0,1,1,0,0,0,0,0,
                            0,0,0,1,0,0,0,0,
                            0,0,0,0,0,1,0,1,
                            1,0,0,0,1,0,0,0,
                            0,0,1,0,0,1,0,1};
                    for (int x = 0, i = 0; x < 8; x++) {
                        for (int z = 0; z < 8; z++) {
                            this.boxes[x][z].setSelectionState(grid[i++] << 1);
                        }
                    }
                }
                break;
                /*case ANY: {
                    int[] grid = {0,0,0,0,0,0,0,0,
                            0,0,0,1,0,0,0,0,
                            0,1,0,0,0,0,0,0,
                            0,0,0,1,0,0,0,0,
                            0,1,1,0,0,0,0,0,
                            0,0,0,1,1,0,0,0,
                            0,0,0,0,0,0,0,0,
                            1,0,1,0,0,1,0,0};
                    for (int x = 0, i = 0; x < 8; x++) {
                        for (int z = 0; z < 8; z++) {
                            this.boxes[x][z].setSelectionState(grid[i++] << 1);
                        }
                    }
                }
                break;*/
            }
        }

        this.revalidate();
        this.repaint();
    }

    private void setupUI() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new BorderLayout(0, 0));
        this.footer = new JPanel();
        this.footer.setLayout(new BorderLayout(0, 0));
        this.contentPane.add(this.footer, BorderLayout.SOUTH);
        this.scannedCount = new JLabel();
        this.scannedCount.setText("0 chunks scanned");
        this.footer.add(this.scannedCount, BorderLayout.CENTER);
        this.actionButton = new JButton();
        this.actionButton.setText("Start");
        this.footer.add(this.actionButton, BorderLayout.EAST);
        JButton optionsButton = new JButton();
        optionsButton.setText("Options");
        optionsButton.addActionListener(e -> new BedrockOptionsDialog(BedrockFrame.this));
        this.footer.add(optionsButton, BorderLayout.WEST);
        this.content = new JPanel();
        this.contentPane.add(this.content, BorderLayout.CENTER);
        this.mode = SearchMode.FULL;
        this.rotation = Rotation.NORTH;
        this.refreshTable();
    }
}
