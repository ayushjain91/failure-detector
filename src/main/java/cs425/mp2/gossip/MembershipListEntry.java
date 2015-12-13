package cs425.mp2.gossip;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MembershipListEntry {
    private final String name;
    private final String addr;
    private final int port;
    private int heartbeatCounter;
    private long heartbeatTime;
    private NodeStatus status;

    public MembershipListEntry (String name, String addr, int port, int heartbeatCounter){
        this.name = name;
        this.addr = addr;
        this.port = port;
        this.heartbeatCounter = heartbeatCounter;
        this.heartbeatTime = System.currentTimeMillis();
        this.status = NodeStatus.ALIVE;
    }

    public MembershipListEntry(String jsonStr) throws ParseException {
        JSONParser parser=new JSONParser();
        JSONObject obj= (JSONObject) parser.parse(jsonStr);
        this.name = (String) obj.get("name");
        this.addr = (String) obj.get("addr");
        this.port = ((Long) obj.get("port")).intValue();
        this.heartbeatCounter = ((Long) obj.get("heartbeatCounter")).intValue();
        this.heartbeatTime = (long) obj.get("heartbeatTime");
        this.status = NodeStatus.valueOf((String) obj.get("status"));
    }

    public String getName(){
        return this.name;
    }


    public String getAddr(){
        return this.addr;
    }

    public int getPort(){
        return this.port;
    }

    public int getHeartbeatCounter(){
        return this.heartbeatCounter;
    }

    @Override
    public String toString() {
        return this.getJSON().toString();
    }

    public long getHeartbeatTime(){
        return this.heartbeatTime;
    }

    public void setHeartbeat(int newHeartbeat){
        this.heartbeatCounter = newHeartbeat;
        this.heartbeatTime = System.currentTimeMillis();
        this.status = NodeStatus.ALIVE;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public JSONObject getJSON(){
        JSONObject obj=new JSONObject();
        obj.put("name", this.getName());
        obj.put("addr", this.getAddr());
        obj.put("port", this.getPort());
        obj.put("heartbeatCounter", this.getHeartbeatCounter());
        obj.put("heartbeatTime", this.getHeartbeatTime());
        obj.put("status",this.getStatus().name());
        return obj;
    }

    public static void main(String args[]){
        return;
    }

}