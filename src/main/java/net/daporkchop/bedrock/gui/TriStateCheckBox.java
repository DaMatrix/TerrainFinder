package net.daporkchop.bedrock.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author s1w_
 */
public class TriStateCheckBox extends JCheckBox implements Icon, ActionListener {
    final static boolean MIDasSELECTED = true;  //consider mid-state as selected ?
    final static Icon icon = UIManager.getIcon("CheckBox.icon");

    public TriStateCheckBox() {
        this("");
    }

    public TriStateCheckBox(String text) {
        super(text);
        putClientProperty("SelectionState", 0);
        setIcon(this);
        addActionListener(this);
    }

    public TriStateCheckBox(String text, int sel) {
        /*
        tri-state checkbox has 3 selection states:
         * 0 unselected
         * 1 mid-state selection
         * 2 fully selected
         */
        super(text, sel > 1);

        switch (sel) {
            case 2:
                setSelected(true);
            case 1:
            case 0:
                putClientProperty("SelectionState", sel);
                break;
            default:
                throw new IllegalArgumentException();
        }
        addActionListener(this);
        setIcon(this);
    }

    @Override
    public boolean isSelected() {
        if (MIDasSELECTED && (getSelectionState() > 0)) return true;
        else return super.isSelected();
    }

    public int getSelectionState() {
        return (getClientProperty("SelectionState") != null ? (int) getClientProperty("SelectionState") :
                super.isSelected() ? 2 :
                        0);
    }

    public void setSelectionState(int sel) {
        switch (sel) {
            case 2:
                setSelected(true);
                break;
            case 1:
            case 0:
                setSelected(false);
                break;
            default:
                throw new IllegalArgumentException();
        }
        putClientProperty("SelectionState", sel);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        if (getSelectionState() != 1) return;

        int w = getIconWidth();
        int h = getIconHeight();
        g.setColor(c.isEnabled() ? new Color(51, 51, 51) : new Color(122, 138, 153));
        g.fillRect(x + 4, y + 4, w - 8, h - 8);

        if (!c.isEnabled()) return;
        g.setColor(new Color(81, 81, 81));
        g.drawRect(x + 4, y + 4, w - 9, h - 9);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }

    public void actionPerformed(ActionEvent e) {
        TriStateCheckBox tcb = (TriStateCheckBox) e.getSource();
        if (tcb.getSelectionState() == 0)
            tcb.setSelected(false);

        tcb.putClientProperty("SelectionState", tcb.getSelectionState() == 2 ? 0 :
                tcb.getSelectionState() + 1);

        // test
        System.out.println(">>>>IS SELECTED: " + tcb.isSelected());
        System.out.println(">>>>IN MID STATE: " + (tcb.getSelectionState() == 1));
    }
}
