package edu.usma.etherchat;

import javax.swing.SwingUtilities;
import org.pcap4j.packet.namednumber.EtherType;

public class EtherChat {

    public static final int SNAPLEN = 65536;
    public static final int READ_TIMEOUT = 10;
    public static final EtherType ETHERTYPE = new EtherType((short) 0x0806, "EtherChat");

    public static void main(String[] args) throws InterruptedException {
        MessageSender sender = new MessageSender();
        MessageReceiver receiver = new MessageReceiver();
        SwingUtilities.invokeLater(new Window(sender, receiver));
    }
}
