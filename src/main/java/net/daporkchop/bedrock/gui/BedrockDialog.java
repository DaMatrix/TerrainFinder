package net.daporkchop.bedrock.gui;

import net.daporkchop.bedrock.Search;
import net.daporkchop.bedrock.mode.SearchMode;
import net.daporkchop.bedrock.util.RotationMode;
import net.daporkchop.lib.common.system.OperatingSystem;
import net.daporkchop.lib.common.system.PlatformInfo;
import net.daporkchop.lib.common.util.PorkUtil;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;

public class BedrockDialog extends JFrame {
    public static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

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
    protected RotationMode rotationMode;
    protected int threads = Runtime.getRuntime().availableProcessors();
    private TriStateCheckBox[][] boxes;
    private Search search;

    public BedrockDialog() {
        this.setupUI();
        this.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        this.setContentPane(this.contentPane);
        this.getRootPane().setDefaultButton(this.actionButton);

        this.actionButton.addActionListener(e -> this.onClick());
        this.pack();
        this.setVisible(true);
    }

    public static void main() {
        new BedrockDialog();
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
                    this.mode.create(pattern, this.rotationMode),
                    (x, z) -> {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Found match at x=" + x + ", z=" + z + "(chunk: x=" + (x >> 4) + ", z=" + (z >> 4) + ")"));
                        return true;
                    });
            this.search.start(this.threads);

            new Thread(() -> {
                this.actionButton.setEnabled(true);
                while (!this.search.completedFuture().isDone()) {
                    this.actionButton.setText("Stop");
                    this.scannedCount.setText(numberFormat.format(this.search.processed()) + " chunks scanned");
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
        optionsButton.addActionListener(e -> new BedrockOptionsDialog(BedrockDialog.this));
        this.footer.add(optionsButton, BorderLayout.WEST);
        this.content = new JPanel();
        this.contentPane.add(this.content, BorderLayout.CENTER);
        this.mode = SearchMode.FULL;
        this.rotationMode = RotationMode.NORTH;
        this.refreshTable();
    }
}
