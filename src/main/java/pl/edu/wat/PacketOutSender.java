package net.floodlightcontroller.pl.edu.wat;

import net.floodlightcontroller.core.IOFSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sszwaczyk on 19.12.17.
 */
public class PacketOutSender {

    private static Logger logger;

    public PacketOutSender() {
        logger = LoggerFactory.getLogger(PacketOutSender.class.getSimpleName());
    }

    public void sendPacketOutMessage(IOFSwitch sw) {

        //TODO: Data-link layer

        //TODO: Internet layer

        //TODO: Transport layer

        //TODO: Application layer (data)

        //TODO: Set payloads

        //TODO: Serialize all

        //TODO: Create PACKET_OUT and send it to switch

        logger.info("Packet Out sent!");
    }
}
