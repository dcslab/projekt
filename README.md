Projekt bazuje na projekcie utworzonym podczas laboratoriów (floodlight-lab4). 

Poszerzony jest jedynie o bazę statystyk (https://github.com/dcslab/projekt/tree/master/src/main/java/net/floodlightcontroller/statistics) na podstawie tutorialu https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/21856267/How+to+Collect+Switch+Statistics+and+Compute+Bandwidth+Utilization. W rezultacie zwracane są ilości bajtów (rx, tx) wszystkich portów switcha w z góry wcześniej określonym przedziale czasowym (10s). Przedział ten można zmniejszyć, ale wpływa to wtedy na dokładność pomiaru.

Utworzono topologię sieci: topologia.py (z rys. niżej nr 3)
Załadowanie topologii: sudo mn --custom topologia.py --topo mytopo --link=tc controller=remote,ip=127.0.0.1,port=6653

Routing statyczny reaktywny oparty jest na utworzeniu przepływów na podstawie flowmod add (https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343547/How+to+use+OpenFlowJ-Loxigen#HowtouseOpenFlowJ-Loxigen-FlowMods). 

Ustawiając na hostach 1, 2 xtermy, uruchomione zostaly wiresharki. 

Utworzono 3 przypadki topologii tak jak tutaj: https://imgur.com/a/9YhyonT można zaobserwować:
1) Przepływ od hostu 1 do hostu 2 i powrót górą (łącze o większej przepustowości), ale podczas przepływu od hostu 1 do 2 dołem (łącze o mniejszej przepustowości) jedynie pojawiają się pakiety na hoscie drugim, natomiast nie wracają do 1.
2) jw
3) Przepływ od hostu 1 do hostu 2 i od hostu 1 do hostu 3 w obie strony.

Przepływ od hostu 1 do 2 i powrót "górą". Następnie przepływ od hostu 1 do 2, ale brak powrotu (inne porty) z punktu widzenia wireshark: https://imgur.com/a/y2yJXQ6

PS Sprawdzałem id portów poprzez mininet>ports

Nie wiem czemu w przypadku 3 to działa nie zmieniając nic w poniższej funkcji:

Fragment kodu odpowiadający za przepływy: https://github.com/dcslab/projekt/blob/master/src/main/java/pl/edu/wat/SdnLabModule.java

------------------------------------
 @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

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
    
 }

----------------------------------

Bazuje on na przepływie w jedną stronę, zamianie portów i przepływie w drugą stronę.
Flaga miała za zadanie wymuszenie drogi przepływu (górą - link o większej przepustowości lub dołem - link o mniejszej przepustowości). Prawidłowo powinien tam znalezc się warunek odnośnie zajętości pasma (wykorzystanie statystyk), jednakże jeśli routing nie działa to nie umieszczałem tego tam.


