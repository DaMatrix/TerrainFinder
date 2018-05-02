package net.daporkchop.bedrock.gui;

import net.daporkchop.bedrock.mode.bedrock.BedrockAlg;
import net.daporkchop.bedrock.mode.bedrock.BedrockMode;
import net.daporkchop.bedrock.util.AsyncTask;
import net.daporkchop.bedrock.util.RotationMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class BedrockDialog extends JFrame {
    public static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel contentPane;
    private JLabel scannedCount;
    private JButton actionButton;
    private JPanel footer;
    private JPanel content;
    protected BedrockMode mode;
    protected RotationMode rotationMode;
    protected int threads = Runtime.getRuntime().availableProcessors();
    private TriStateCheckBox[][] boxes;
    private BedrockAlg alg;

    public BedrockDialog() {
        setupUI();
        setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(actionButton);

        actionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClick();
            }
        });
        this.pack();
        this.setVisible(true);
    }

    public static void main() {
        new BedrockDialog();
    }

    private synchronized void onClick() {
        actionButton.setEnabled(false);
        if (alg == null || !alg.isRunning()) {
            actionButton.setText("Starting...");
            System.out.println("Starting search for mode " + mode);
            int size = mode.size;
            byte[] pattern = new byte[size * size];
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    int state = boxes[x][z].getSelectionState();
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

            final AtomicLong processed = new AtomicLong(0);
            alg = mode.constructor.newInstance(
                    processed,
                    pattern,
                    (x, z, p) -> {
                        JOptionPane.showMessageDialog(null, "Found match at x=" + x + ", z=" + z);
                    },
                    this.threads,
                    this.rotationMode);

            alg.start(false);

            new AsyncTask("GUI updater worker",
                    () -> {
                        actionButton.setEnabled(true);
                        while (alg.isRunning()) {
                            actionButton.setText("Stop");
                            try {
                                Thread.sleep(100L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            scannedCount.setText(numberFormat.format(processed.get()) + " chunks scanned");
                        }
                        actionButton.setText("Start");
                    });
        } else {
            actionButton.setText("Stopping...");
            new AsyncTask("Search stop thread",
                    () -> {
                        alg.stop(true);
                        actionButton.setText("Start");
                        actionButton.setEnabled(true);
                    });
        }
    }

    public void refreshTable() {
        content.removeAll();
        content.setLayout(new GridLayout(mode.size, mode.size));
        boxes = new TriStateCheckBox[mode.size][mode.size];

        for (int x = 0; x < mode.size; x++) {
            for (int z = 0; z < mode.size; z++) {
                content.add(boxes[x][z] = new TriStateCheckBox());
            }
        }
        revalidate();
        repaint();
    }

    private void setupUI() {
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        footer = new JPanel();
        footer.setLayout(new BorderLayout(0, 0));
        contentPane.add(footer, BorderLayout.SOUTH);
        scannedCount = new JLabel();
        scannedCount.setText("0 chunks scanned");
        footer.add(scannedCount, BorderLayout.CENTER);
        actionButton = new JButton();
        actionButton.setText("Start");
        footer.add(actionButton, BorderLayout.EAST);
        JButton optionsButton = new JButton();
        optionsButton.setText("Options");
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new BedrockOptionsDialog(BedrockDialog.this);
            }
        });
        footer.add(optionsButton, BorderLayout.WEST);
        content = new JPanel();
        contentPane.add(content, BorderLayout.CENTER);
        mode = BedrockMode.FULL;
        rotationMode = RotationMode.NORTH;
        this.refreshTable();
    }
}
