package pl.edu.wat;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sszwaczyk on 19.12.17.
 */
public class FlowAddSender {

    protected static boolean FLOWMOD_DEFAULT_MATCH_VLAN = true;
    protected static boolean FLOWMOD_DEFAULT_MATCH_MAC = true;
    protected static boolean FLOWMOD_DEFAULT_MATCH_IP_ADDR = true;
    protected static boolean FLOWMOD_DEFAULT_MATCH_TRANSPORT = true;
    protected static int FLOWMOD_DEFAULT_PRIORITY = 32768;

    private static Logger logger;
	//private static IOFSwitchService switchService;

    public FlowAddSender() {
        logger = LoggerFactory.getLogger(FlowAddSender.class.getSimpleName());
    }

	//IOFSwitch sw1 = switchService.getSwitch(DatapathId.of(1));
    
    public void simpleAdd(IOFSwitch sw, OFPacketIn pin, FloodlightContext cntx, OFPort outPort) {


    	
        //TODO: Create OFFlowMod.Builder
    	OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();

        //TODO: Create Match
    	Match.Builder mb = sw.getOFFactory().buildMatch();
    	mb.setExact(MatchField.IN_PORT, pin.getMatch().get(MatchField.IN_PORT));
    	Match m = mb.build();

        //TODO: Create Actions
    	OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
    	List<OFAction> actions = new ArrayList<OFAction>();
    	aob.setPort(outPort);
    	aob.setMaxLen(Integer.MAX_VALUE);
    	actions.add(aob.build());

        //TODO: Bind match and actions with OFFlowMod.Builder
    	fmb.setMatch(m).setBufferId(pin.getBufferId()).setOutPort(outPort).setPriority(FLOWMOD_DEFAULT_PRIORITY);
    	fmb.setActions(actions);

        //TODO: Send FlowMod message to switch
    	try {
    		sw.write(fmb.build());
    		logger.info("Flow from port {} forwarded to port {}); match: {}",
    				new Object [] {pin.getMatch().get(MatchField.IN_PORT).getPortNumber(), outPort.getPortNumber(), m.toString()});
    		} catch(Exception e) {
    			logger.error("error {}", e);
    		}
    }

    public void addBasedOnPacketIn(IOFSwitch sw, OFPacketIn pin, FloodlightContext cntx, OFPort outPort) {

        //TODO: Based on simpleAdd and using createMatchFromPacket method build FlowAdd message based on PacketIn.
        //TODO: Set Idle timeout of Flow to 10 seconds and Hard timeout to 120 seconds
    }

    public Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {
        // The packet in match will only contain the port number.
        // We need to add in specifics for the hosts we're routing between.
        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
        VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
        MacAddress srcMac = eth.getSourceMACAddress();
        MacAddress dstMac = eth.getDestinationMACAddress();

        Match.Builder mb = sw.getOFFactory().buildMatch();
        mb.setExact(MatchField.IN_PORT, inPort);

        if (FLOWMOD_DEFAULT_MATCH_MAC) {
            mb.setExact(MatchField.ETH_SRC, srcMac).setExact(MatchField.ETH_DST, dstMac);
        }

        if (FLOWMOD_DEFAULT_MATCH_VLAN) {
            if (!vlan.equals(VlanVid.ZERO)) {
                mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
            }
        }

        // TODO Detect switch type and match to create hardware-implemented flow
        if (eth.getEtherType() == EthType.IPv4) { /*
													 * shallow check for
													 * equality is okay for
													 * EthType
													 */
            IPv4 ip = (IPv4) eth.getPayload();
            IPv4Address srcIp = ip.getSourceAddress();
            IPv4Address dstIp = ip.getDestinationAddress();

            if (FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
                mb.setExact(MatchField.ETH_TYPE, EthType.IPv4).setExact(MatchField.IPV4_SRC, srcIp)
                        .setExact(MatchField.IPV4_DST, dstIp);
            }

            if (FLOWMOD_DEFAULT_MATCH_TRANSPORT) {
				/*
				 * Take care of the ethertype if not included earlier, since
				 * it's a prerequisite for transport ports.
				 */
                if (!FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
                    mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
                }

                if (ip.getProtocol().equals(IpProtocol.TCP)) {
                    TCP tcp = (TCP) ip.getPayload();
                    mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP).setExact(MatchField.TCP_SRC, tcp.getSourcePort())
                            .setExact(MatchField.TCP_DST, tcp.getDestinationPort());
                } else if (ip.getProtocol().equals(IpProtocol.UDP)) {
                    UDP udp = (UDP) ip.getPayload();
                    mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP).setExact(MatchField.UDP_SRC, udp.getSourcePort())
                            .setExact(MatchField.UDP_DST, udp.getDestinationPort());
                }
            }
        } else if (eth.getEtherType() == EthType.ARP) { /*
														 * shallow check for
														 * equality is okay for
														 * EthType
														 */
            mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
        }

        return mb.build();
    }
}
