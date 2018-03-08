package edu.usma.etherchat;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public class MessageReceiver implements Runnable {

    private String device;
    private final BlockingQueue<Map.Entry<String, String>> incoming = new LinkedBlockingQueue<>();

    void open(String device) {
        this.device = device;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(device);

            try (PcapHandle handle = nif.openLive(EtherChat.SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, EtherChat.READ_TIMEOUT);) {
                handle.setFilter("ether dst FF:FF:FF:FF:FF:FF", BpfProgram.BpfCompileMode.OPTIMIZE);

                handle.stream()
                        .map((packet) -> new AbstractMap.SimpleEntry("packet", packet.toHexString()))
                        .forEach((message) -> incoming.offer(message));

            }
        } catch (PcapNativeException | NotOpenException ex) {
            Window.alert(ex.getMessage());
        }
    }

    void getMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
