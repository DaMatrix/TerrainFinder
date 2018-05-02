package net.daporkchop.bedrock.gui;

import net.daporkchop.bedrock.Bedrock;
import net.daporkchop.bedrock.mode.bedrock.BedrockMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Locale;

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
    private JComboBox modeBox;
    private JPanel footer;
    private JPanel content;
    private BedrockMode mode;
    private TriStateCheckBox[][] boxes;

    {
        setupUI();
    }

    public BedrockDialog() {
        setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(actionButton);

        actionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAction();
            }
        });

        modeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                BedrockMode newMode = (BedrockMode) itemEvent.getItem();
                if (newMode != mode) {
                    mode = newMode;
                    System.out.println("Changed to " + newMode);
                    refreshTable();
                }
            }
        });
    }

    public static void main(String[] args) {
        BedrockDialog dialog = new BedrockDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onAction() {
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

        new Thread() {
            @Override
            public void run() {
                new Bedrock(pattern,
                        Runtime.getRuntime().availableProcessors(),
                        mode,
                        l -> scannedCount.setText(numberFormat.format(l) + " chunks scanned"),
                        500L,
                        (x, z, p) -> JOptionPane.showMessageDialog(null, "Found match at x=" + x + ", z=" + z));
            }
        }
                //;
                .start();
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
        modeBox = new JComboBox();
        for (BedrockMode mode : BedrockMode.values()) {
            modeBox.addItem(mode);
        }
        modeBox.setToolTipText("<html><strong>Full</strong>: Searches for a pattern in an entire chunk (fastest)<br>" +
                "<strong>Sub</strong>: Searches for an 8x8 subpattern in a single chunk (slower)<br>" +
                "<strong>Super</strong>: Searches for an 8x8 pattern that can overlap into neighboring chunks (slowest)</html>");
        footer.add(modeBox, BorderLayout.WEST);
        content = new JPanel();
        contentPane.add(content, BorderLayout.CENTER);
        mode = BedrockMode.FULL;
        this.refreshTable();
    }
}
