package juicebox.mapcolorui;

import juicebox.MainWindow;
import org.lwjgl.Sys;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class MultiColorPickerDialog extends JDialog{
    private static final long serialVersionUID = -678567876;

    JButton[] bColor = new JButton[24];
    JButton[] bChoose = new JButton[24];
    JButton[] bDelete = new JButton[24];
    JColorChooser chooser = new JColorChooser();

    JPanel preview = new JPanel();
    JPanel prvPanel1 = new JPanel();
    JPanel prvPanel2 = new JPanel();
    JPanel prvPanel3 = new JPanel();

    JPanel chooserPanel = new JPanel();
    JButton bOk = new JButton("OK");
    JButton bCancel = new JButton("Cancel");

    public MultiColorPickerDialog() {
        super();
        setResizable(false);
        setLayout(new BoxLayout(getContentPane(), 1));
        final Color defaultColor = getBackground();

        MainWindow.getInstance();

        chooser.setSize(new Dimension(690, 270));
        chooserPanel.setMaximumSize(new Dimension(690, 270));

        AbstractColorChooserPanel[] accp = chooser.getChooserPanels();
        chooser.removeChooserPanel(accp[0]);
        chooser.removeChooserPanel(accp[1]);
        chooser.removeChooserPanel(accp[2]);
        chooser.removeChooserPanel(accp[4]);

        chooser.setPreviewPanel(new JPanel());

        chooserPanel.add(chooser);

        prvPanel1.add(new JLabel("RGB"));
        prvPanel2.add(new JLabel("Pick"));
        prvPanel3.add(new JLabel("Clear"));

        for (int idx = 0; idx < 24; idx++) {
            final int x = idx;
            bColor[x] = new JButton();
            bColor[x].setBackground(defaultColor);
            bColor[x].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            bColor[x].setOpaque(true);
            bChoose[x] = new JButton("+");
            bDelete[x] = new JButton("-");

            bColor[x].setPreferredSize(new Dimension(15, 15));
            prvPanel1.add(bColor[x]);

            bChoose[x].setPreferredSize(new Dimension(15, 15));
            bDelete[x].setPreferredSize(new Dimension(15, 15));

            bColor[x].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chooser.setColor(bColor[x].getBackground());
                }
            });
            bChoose[x].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bColor[x].setBackground(chooser.getColor());
                }
            });
            bDelete[x].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bColor[x].setBackground(defaultColor);
                }
            });

            prvPanel2.add(bChoose[x]);

            prvPanel3.add(bDelete[x]);
        }

        prvPanel1.setPreferredSize(new Dimension(600, 30));
        prvPanel2.setPreferredSize(new Dimension(600, 30));
        prvPanel3.setPreferredSize(new Dimension(600, 30));

        getContentPane().add(chooserPanel);

        preview.add(prvPanel1);
        preview.add(prvPanel2);
        preview.add(prvPanel3);
        add(preview);

        JPanel okCancel = new JPanel();

        okCancel.add(bOk);
        bOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                MainWindow.preDefMapColorGradient.clear();
                MainWindow.preDefMapColorFractions.clear();

                //todo - make the bColor add/remove behavior instead.
                for (JButton aBColor : bColor) {
                    if (aBColor.isVisible())
                        MainWindow.preDefMapColorGradient.add(aBColor.getBackground());
                }

                float tmpfraction = 0.0f;
                int tmpSize = MainWindow.preDefMapColorGradient.size();
                float tmpGap = 0;
                if (tmpSize > 0) {
                    tmpGap = (1.0f / MainWindow.preDefMapColorGradient.size());
                }

                for (int i = 0; i < tmpSize; i++) {
                    MainWindow.preDefMapColorFractions.add(tmpfraction);
                    tmpfraction += tmpGap;
                }
                dispose();
            }
        });

        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okCancel.add(bCancel);
        add(okCancel);

        setSize(new Dimension(690, 500));
        setVisible(true);

        setLocationRelativeTo(getOwner());
    }

    public void initValue(Color[] colorArray){
        for (int cIdx=0; cIdx < colorArray.length && cIdx < bColor.length;cIdx++)
        {
            bColor[cIdx].setBackground(colorArray[cIdx]);
        }
    }
}
