# failure-detector
A gossip-style failure detection protocol for distributed systems (see http://dl.acm.org/citation.cfm?id=1659238).

## Parameters ##
The parameters for this failure detector are defined in src/main/resources/config.properties

1. **T_GOSSIP**: The time interval (in milliseconds) at which every node sends heartbeats to the group
2. **T_SUSPECT**: A node is marked as *SUSPECT* if no hearbeat is received for *T_SUSPECT* milliseconds 
3. **T_FAIL**: A node is marked as *FAILED* if no hearbeat is received for *T_FAIL* milliseconds 
4. **port**: The port to be used on all machines for this protocol
5. **introducers**: The addresses of the nodes that act as introducers - evry new node that joins the group contacts these nodes
6. **K**: The number of nodes to whom a heartbeat is sent every *T_GOSSIP* milliseconds

##Running the code##
1. Compile the java files using ```$ mvn compile```
2. Run the Gossip class using ```$ mvn exec:java -Dexec.mainClass="cs425.mp2.gossip.Gossip"```
