package com.barley.monty;

import com.barley.runtime.*;
import com.barley.utils.Arguments;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Monty {

    private static JFrame frame;
    private static LayoutManager layoutManager = new BorderLayout();
    private static int progressID = 0;
    private static ArrayList<JProgressBar> bars = new ArrayList<>();

    // Layout functions

    public static BarleyValue BorderLayout(BarleyValue... args) {
        layoutManager = new BorderLayout();
        frame.setLayout(layoutManager);
        return new BarleyReference(frame);
    }

    public static BarleyValue FlowLayout(BarleyValue... args) {
        layoutManager = new FlowLayout(args.length == 1 ? args[0].asInteger().intValue() : SwingConstants.LEADING);
        frame.setLayout(layoutManager);
        return new BarleyReference(frame);
    }

    public static BarleyValue GridLayout(BarleyValue... args) {
        Arguments.check(2,args.length);
        layoutManager = new GridLayout(args[0].asInteger().intValue(), args[1].asInteger().intValue());
        frame.setLayout(layoutManager);
        return new BarleyReference(frame);
    }

    public static BarleyValue Window(BarleyValue... args) {
        Arguments.checkOrOr(0, 1, args.length);
        String tittle = (args.length == 1) ? args[0].toString() : "";
        frame = new JFrame(tittle);
        frame.setLayout(null);
        ImageIcon icon = new ImageIcon("monty/monty_icon.jpg");
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return new BarleyReference(frame);
    }

    public static BarleyValue SetVisible(BarleyValue... args) {
        Arguments.check(1, args.length);
        frame.setVisible(args[0].toString().equals("true"));
        return new BarleyReference(frame);
    }

    public static BarleyValue Panel(BarleyValue... args) {
        Arguments.checkOrOr(0, 1, args.length);
        final JPanel panel = new JPanel();
        panel.setLayout(layoutManager);
        frame.add(panel);
        return new BarleyReference(frame);
    }

    public static BarleyValue Text(BarleyValue... args) {
        Arguments.check(6, args.length);
        final JLabel text = new JLabel(args[0].toString());
        text.setForeground((Color) ((BarleyReference) args[5]).getRef());
        text.setBounds(args[1].asInteger().intValue(), args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue());
        frame.add(text);
        return new BarleyReference(frame);
    }

    public static BarleyValue SetSize(BarleyValue... args) {
        Arguments.check(2, args.length);
        frame.setSize(new Dimension(args[0].asInteger().intValue(), args[1].asInteger().intValue()));
        return new BarleyReference(frame);
    }

    public static BarleyValue SetResizable(BarleyValue... args) {
        Arguments.check(1, args.length);
        frame.setResizable(args[0].toString().equals("true"));
        return new BarleyReference(frame);
    }

    public static BarleyValue Center(BarleyValue... args) {
        Arguments.check(0, args.length);
        frame.setLocationRelativeTo(null);
        return new BarleyReference(frame);
    }

    public static BarleyValue Button(BarleyValue... args) {
        Arguments.check(5, args.length);
        final JButton button = new JButton(args[0].toString());
        button.setBounds(args[1].asInteger().intValue(), args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue());
        frame.add(button);
        return new BarleyReference(frame);
    }

    public static BarleyValue ActionButton(BarleyValue... args) {
        Arguments.check(6, args.length);
        final JButton button = new JButton(args[0].toString());
        button.setBounds(args[1].asInteger().intValue(), args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue());
        button.addActionListener((e) -> {((BarleyFunction) args[5]).execute();});
        frame.add(button);
        return new BarleyReference(frame);
    }

    public static BarleyValue Slider(BarleyValue... args) {
        Arguments.check(8, args.length);
        final JSlider slider = new JSlider(args[0].asInteger().intValue(), args[1].asInteger().intValue(), args[2].asInteger().intValue());
        slider.setBounds(args[3].asInteger().intValue(), args[4].asInteger().intValue(), args[5].asInteger().intValue(), args[6].asInteger().intValue());
        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            ((BarleyFunction) args[7]).execute(new BarleyNumber(source.getValue()));
        });
        frame.add(slider);
        return new BarleyReference(frame);
    }

    public static BarleyValue Pack(BarleyValue... args) {
        frame.pack();
        return new BarleyReference(frame);
    }

    public static BarleyValue CheckBox(BarleyValue... args) {
        Arguments.check(7, args.length);
        final JCheckBox checkBox = new JCheckBox(args[0].toString(), args[1].toString().equals("true"));
        checkBox.setBounds(args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue(), args[5].asInteger().intValue());
        checkBox.addActionListener((l) -> {
            JCheckBox source = (JCheckBox) l.getSource();
            ((BarleyFunction) args[6]).execute(new BarleyAtom(String.valueOf(source.isSelected())), new BarleyString(source.getText()));
        });
        frame.add(checkBox);
        return new BarleyReference(frame);
    }

    public static BarleyValue ClearFrame(BarleyValue... args) {
        synchronized (new Object()) {
            frame.getContentPane().removeAll();
            Modules.get("barley").get("sleep").execute(new BarleyNumber(10));
        }
        return new BarleyReference(frame);
    }

    public static BarleyValue Image(BarleyValue... args) {
        ImageIcon icon = new ImageIcon(args[0].toString());
        Image img = icon.getImage();
        final JLabel labelPic = new JLabel("");
        labelPic.setBounds(args[1].asInteger().intValue(), args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue());
        labelPic.setIcon(icon);
        frame.add(labelPic);
        return new BarleyReference(frame);
    }

    public static BarleyValue ProgressBar(BarleyValue... args) {
        Arguments.check(6, args.length);
        final JProgressBar progressBar = new JProgressBar(args[0].asInteger().intValue(), args[1].asInteger().intValue());
        progressBar.setBounds(args[2].asInteger().intValue(), args[3].asInteger().intValue(), args[4].asInteger().intValue(), args[5].asInteger().intValue());
        progressID++;
        bars.add(progressBar);
        frame.add(progressBar);
        return new BarleyNumber(progressID - 1);
    }

    public static BarleyValue StepBar(BarleyValue... args) {
        Arguments.check(2, args.length);
        bars.get(args[0].asInteger().intValue()).setValue(bars.get(args[0].asInteger().intValue()).getValue() + args[1].asInteger().intValue());
        frame.repaint();
        frame.revalidate();
        return args[0];
    }
}
