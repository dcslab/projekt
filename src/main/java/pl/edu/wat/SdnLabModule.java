package pl.edu.wat;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by sszwaczyk on 19.12.17.
 */
public class SdnLabModule implements IFloodlightModule, IOFMessageListener {

    private IFloodlightProviderService floodlightProvider;
    private static Logger logger;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        logger = LoggerFactory.getLogger(SdnLabModule.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public String getName() {
        return SdnLabModule.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

        //Packet Out
        PacketOutSender packetOutsender = new PacketOutSender();
        packetOutsender.sendPacketOutMessage(sw);

        /*
        //Flow Add
        OFPacketIn pin = (OFPacketIn) msg;
        OFPort outPort = OFPort.of(0);
        if (pin.getInPort() == OFPort.of(1)) {
            outPort = OFPort.of(2);
        } else {
            outPort = OFPort.of(1);
        }
        FlowAddSender flowAddSender = new FlowAddSender();
        flowAddSender.simpleAdd(sw, pin, cntx, outPort);
        flowAddSender.addBasedOnPacketIn(sw, pin, cntx, outPort);
        */
        return Command.CONTINUE;
    }
}
