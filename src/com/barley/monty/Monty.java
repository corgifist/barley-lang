package com.barley.monty;

import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyReference;
import com.barley.runtime.BarleyValue;
import com.barley.utils.Arguments;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Monty {

    private static JFrame frame;
    private static LayoutManager layoutManager = new BorderLayout();

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
        Arguments.check(5, args.length);
        final JLabel text = new JLabel(args[0].toString());
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

    public static BarleyValue Pack(BarleyValue... args) {
        frame.pack();
        return new BarleyReference(frame);
    }
}
