package edu.usma.etherchat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Window implements Runnable {

    private final List<String> deviceNames;
    private final BlockingQueue<Message> messageQueue;
    private Consumer<String> deviceChangeConsumer;

    Window(BlockingQueue<Message> messageQueue) {
        this.messageQueue = messageQueue;
        this.deviceNames = new ArrayList();
    }

    public static void alert(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    void addDevice(String name) {
        deviceNames.add(name);
    }

    void onDeviceChange(Consumer<String> consumer) {
        this.deviceChangeConsumer = consumer;
    }

    @Override
    public void run() {
        deviceNames.add(0, "Select a network device ...");
        JComboBox deviceComboBox = new JComboBox(deviceNames.toArray());

        deviceComboBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                deviceComboBox.setEnabled(false);
                deviceChangeConsumer.accept(e.getItem().toString());
            }
        });

        JTextArea messages = new JTextArea();
        messages.setLineWrap(true);
        JScrollPane jp = new JScrollPane(messages);

        // monitor the queue for incoming messages
        new Timer(500, (e) -> {
            if (!messageQueue.isEmpty()) {
                try {
                    messages.append(messageQueue.take().getText());
                } catch (InterruptedException ignore) {
                }
            }
        }).start();

        JTextField input = new JTextField("Say something!");

        // this seems like a lot of work for a placeholder ...
        input.setForeground(Color.GRAY);

        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getBackground() == Color.GRAY) {
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
            input.setText("");
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
}
