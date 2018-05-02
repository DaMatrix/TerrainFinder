package net.daporkchop.bedrock.gui;

import lombok.NonNull;
import net.daporkchop.bedrock.mode.bedrock.BedrockMode;
import net.daporkchop.bedrock.util.RotationMode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Locale;

public class BedrockOptionsDialog extends JFrame {
    public static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel contentPane;
    private BedrockDialog dialog;

    public BedrockOptionsDialog(@NonNull BedrockDialog dialog) {
        this.dialog = dialog;
        setupUI();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setContentPane(contentPane);
        this.pack();
        this.setVisible(true);
    }

    private void setupUI() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(3, 2));

        {
            contentPane.add(new JLabel("Mode"));
            JComboBox modeBox = new JComboBox();
            {
                String s = "<html>";
                for (BedrockMode mode : BedrockMode.values()) {
                    modeBox.addItem(mode);
                    s += "<strong>" + mode.name() + "</strong>: " + mode.desc + "<br>";
                }
                modeBox.setSelectedItem(dialog.mode);
                modeBox.setToolTipText(s + "</html>");

                modeBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent itemEvent) {
                        BedrockMode newMode = (BedrockMode) itemEvent.getItem();
                        if (newMode != dialog.mode) {
                            dialog.mode = newMode;
                            System.out.println("Changed to " + newMode);
                            dialog.refreshTable();
                        }
                    }
                });
            }
            contentPane.add(modeBox);
        }

        {
            contentPane.add(new JLabel("Rotation"));
            JComboBox rotBox = new JComboBox();
            {
                for (RotationMode mode : RotationMode.values()) {
                    rotBox.addItem(mode);
                }
                rotBox.setSelectedItem(dialog.rotationMode);
                rotBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent itemEvent) {
                        RotationMode newMode = (RotationMode) itemEvent.getItem();
                        if (newMode != dialog.rotationMode) {
                            dialog.rotationMode = newMode;
                            System.out.println("Changed to " + newMode);
                        }
                    }
                });
            }
            contentPane.add(rotBox);
        }

        {
            contentPane.add(new JLabel("Threads"));

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(0, 0));

            {
                JLabel label = new JLabel(String.valueOf(dialog.threads));
                JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 1, Runtime.getRuntime().availableProcessors(), this.dialog.threads);
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        label.setText(String.valueOf(dialog.threads = slider.getValue()));
                    }
                });

                panel.add(label, BorderLayout.EAST);
                panel.add(slider, BorderLayout.CENTER);
            }
            contentPane.add(panel);
        }
    }
}
