from mininet.topo import Topo

class MyTopo( Topo ):
    "Simple topology example."

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        leftHost = self.addHost( 'h1' )
        rightHost = self.addHost( 'h2' )
        oneSwitch = self.addSwitch( 's1' )
        twoSwitch = self.addSwitch( 's2' )
        thrSwitch = self.addSwitch( 's3' )

        # Add links
        self.addLink( leftHost, oneSwitch, bw=100 )
        self.addLink( oneSwitch, twoSwitch, bw=100 )
        self.addLink( oneSwitch, thrSwitch, bw=1 )
        self.addLink( twoSwitch, rightHost, bw=100 )
        self.addLink( thrSwitch, rightHost, bw=1 )


topos = { 'mytopo': ( lambda: MyTopo() ) }

