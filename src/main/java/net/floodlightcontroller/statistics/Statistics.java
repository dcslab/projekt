package net.floodlightcontroller.statistics;

import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.types.NodePortTuple;

public class Statistics implements IStatisticsService {

	@Override
	public SwitchPortBandwidth getBandwidthConsumption(DatapathId dpid, OFPort p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<NodePortTuple, SwitchPortBandwidth> getBandwidthConsumption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void collectStatistics(boolean collect) {
		// TODO Auto-generated method stub

	}

}
