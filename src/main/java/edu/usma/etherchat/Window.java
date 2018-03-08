package edu.usma.etherchat;

import edu.usma.etherchat.MessageReceiver.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Window implements Runnable {

    private final List<String> devices;
    private final MessageSender sender;
    private final MessageReceiver receiver;

    public Window(List<String> devices, MessageSender sender, MessageReceiver receiver) {
        this.devices = devices;
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        devices.add(0, "Select a network device ...");
        JComboBox deviceComboBox = new JComboBox(devices.toArray());

        deviceComboBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                deviceComboBox.setEnabled(false);
                sender.open(e.getItem().toString());
                receiver.open(e.getItem().toString());
            }
        });

        JTextArea messages = new JTextArea();
        messages.setLineWrap(true);
        JScrollPane jp = new JScrollPane(messages);

        new Timer(500, (e) -> {
            Message message = receiver.getMessage();

            if (message != null) {
                messages.append(message.getText());
                messages.append(System.getProperty("line.separator"));
            }
        }).start();

        JTextField input = new JTextField("Say something!");

        // this seems like a lot of work for a placeholder ...
        input.setForeground(Color.GRAY);

        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getForeground() == Color.GRAY) {
                    input.setText("");
                    input.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty()) {
                    input.setForeground(Color.GRAY);
                    input.setText("Say something!");
                }
            }
        });

        input.addActionListener((ActionEvent e) -> {
            if (!input.getText().isEmpty()) {
                sender.send(input.getText());
                input.setText("");
            }
        });

        JFrame frame = new JFrame("EtherChat");

        frame.add(deviceComboBox, BorderLayout.NORTH);
        frame.add(jp, BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);
        frame.pack();

        frame.setSize(1000, 500);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void alert(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
