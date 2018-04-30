package net.daporkchop.bedrock.gui;

import net.daporkchop.bedrock.Bedrock;
import net.daporkchop.bedrock.mode.Modes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

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
    private JTable table;
    private Modes mode;

    {
        $$$setupUI$$$();
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
                Modes newMode = (Modes) itemEvent.getItem();
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
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        System.out.println("Starting search for mode " + mode);
        int size = mode.size;
        byte[] pattern = new byte[size * size];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                pattern[x * size + z] = (boolean) model.getValueAt(x, z) ? (byte) 1 : 0;
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
                        (x, z) -> JOptionPane.showMessageDialog(null, "Found match at x=" + x + ", z=" + z));
            }
        }
                //;
                .start();
    }

    public void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setColumnCount(0);
        model.setColumnCount(mode.size);
        model.setNumRows(mode.size);

        for (int x = 0; x < mode.size; x++) {
            for (int z = 0; z < mode.size; z++) {
                model.setValueAt(Boolean.FALSE, x, z);
            }
        }
    }

    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        footer = new JPanel();
        footer.setLayout(new BorderLayout(0, 0));
        contentPane.add(footer, BorderLayout.SOUTH);
        scannedCount = new JLabel();
        scannedCount.setText("0 chunks scanned");
        footer.add(scannedCount, BorderLayout.CENTER);
        actionButton = new JButton();
        actionButton.setText("Button");
        footer.add(actionButton, BorderLayout.EAST);
        modeBox = new JComboBox();
        for (Modes mode : Modes.values()) {
            modeBox.addItem(mode);
        }
        footer.add(modeBox, BorderLayout.WEST);
        content = new JPanel();
        content.setLayout(new BorderLayout(0, 0));
        contentPane.add(content, BorderLayout.CENTER);
        table = new JTable(new CheckBoxModel());
        mode = Modes.FULL;
        this.refreshTable();
        content.add(table, BorderLayout.CENTER);
    }

    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public class CheckBoxModel extends DefaultTableModel {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            Vector rowData = (Vector) getDataVector().get(row);
            rowData.set(column, aValue);
            fireTableCellUpdated(row, column);
        }
    }
}
