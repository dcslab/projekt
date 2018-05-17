package pl.edu.wat;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
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

        /*Packet Out -- section
        PacketOutSender packetOutsender = new PacketOutSender();
        packetOutsender.sendPacketOutMessage(sw);
*/
        
        //Flow Add -- section
        OFPacketIn pin = (OFPacketIn) msg;
        OFPort outPort = OFPort.of(0);
        
        boolean flaga = false;
        
       // if (sw == (DatapathId.of(1))) {
            if (flaga == true) { 
            	if (pin.getMatch().get(MatchField.IN_PORT) == OFPort.of(1)) 
            		outPort = OFPort.of(2);
            	else
            		outPort = OFPort.of(1);
            } else {
            	if (pin.getMatch().get(MatchField.IN_PORT) == OFPort.of(1))
            		outPort = OFPort.of(3);
            	else if (pin.getMatch().get(MatchField.IN_PORT) == OFPort.of(3))
            		outPort = OFPort.of(1);
    
            } 
            
      /*  } else if (sw == (DatapathId.of(2))) {
        	if (pin.getMatch().get(MatchField.IN_PORT) == OFPort.of(1)) 
        		outPort = OFPort.of(2);
        	else 
        		outPort = OFPort.of(1);
        } else if (sw == (DatapathId.of(3))) {
        	if (pin.getMatch().get(MatchField.IN_PORT) == OFPort.of(1)) 
        		outPort = OFPort.of(2);
        	else 
        		outPort = OFPort.of(1);
        }
        */

        
        FlowAddSender flowAddSender = new FlowAddSender();
        

        
        //Simple Flow Add -- section
        flowAddSender.simpleAdd(sw, pin, cntx, outPort);
        

        /*
        //Packet In Flow Add -- section
        flowAddSender.addBasedOnPacketIn(sw, pin, cntx, outPort);
        */
        return Command.CONTINUE;
    }
}
