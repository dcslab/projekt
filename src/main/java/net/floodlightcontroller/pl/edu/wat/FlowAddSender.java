package net.floodlightcontroller.pl.edu.wat;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
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

    private static Logger logger;

    public FlowAddSender() {
        logger = LoggerFactory.getLogger(FlowAddSender.class.getSimpleName());
    }

    public void simpleAdd(IOFSwitch sw, OFPacketIn pin, FloodlightContext cntx, OFPort outPort) {

    }

    public void addBasedOnPacketIn(IOFSwitch sw, OFPacketIn pin, FloodlightContext cntx, OFPort outPort) {

    }

    private Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {
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
