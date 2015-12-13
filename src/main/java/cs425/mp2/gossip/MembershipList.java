package cs425.mp2.gossip;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * Created by ayush on 12/11/15.
 */
public class MembershipList {
    private HashMap<String, MembershipListEntry> membershipGroup;

    public MembershipList(String myIP, int myPort){
        this.membershipGroup = new HashMap<String, MembershipListEntry>();
        String key = getKey(myIP, myPort);
        this.membershipGroup.put(key, new MembershipListEntry(key, myIP, myPort, 1));
    }

    public MembershipList(){
        this.membershipGroup = new HashMap<String, MembershipListEntry>();
    }

    public MembershipList(String jsonStr) throws ParseException {
        this.membershipGroup = new HashMap<String, MembershipListEntry>();
        JSONParser parser=new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(jsonStr);
        JSONObject memlist = (JSONObject) parser.parse(obj.get("membershipGroup").toString());
        for(Iterator iterator =memlist.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            this.membershipGroup.put(key, new MembershipListEntry(memlist.get(key).toString()));
        }
    }

    public int getHeartBeatFor(String key){
        return this.membershipGroup.get(key).getHeartbeatCounter();
    }

    public int getHeartbeatFor(String addr, int port){
        String key = getKey(addr, port);
        return getHeartBeatFor(key);
    }

    public MembershipListEntry get(String key){
        return this.membershipGroup.get(key);
    }

    public MembershipListEntry get(String addr, int port){
        String key = getKey(addr, port);
        return get(key);
    }

    public Set<String> getProcesses(){
        return this.membershipGroup.keySet();
    }

    public long getHeartbeatTime(String key){
        return this.membershipGroup.get(key).getHeartbeatTime();
    }

    public long getHeartbeatTime(String addr, int port){
        String key = getKey(addr, port);
        return getHeartbeatTime(key);
    }

    public boolean hasNode(String addr, int port){
        String key = getKey(addr, port);
        return this.hasNode(key);
    }

    public boolean hasNode(String key){
        return this.membershipGroup.containsKey(key);
    }

    public Iterator<Map.Entry<String, MembershipListEntry>> iterator() {
        return this.membershipGroup.entrySet().iterator();
    }


    public void setHeartbeatFor(String nodeIP, int nodePort, int heartBeat){
        String key = getKey(nodeIP, nodePort);
        if (this.membershipGroup.containsKey(key)){
            this.membershipGroup.get(key).setHeartbeat(heartBeat);
        }
        else {
            this.membershipGroup.put(key, new MembershipListEntry(key, nodeIP, nodePort, heartBeat));
        }
    }

    private String getKey(String addr, int port){
        return addr + ":" + Integer.toString(port);
    }

    public void merge(String listJSON){
        MembershipList other = null;
        try {
            other = new MembershipList(listJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (Iterator<Map.Entry<String, MembershipListEntry>> membersIter = other.iterator(); membersIter.hasNext();) {
            Map.Entry<String, MembershipListEntry> e = membersIter.next();
            MembershipListEntry otherEntry = e.getValue();
            if (!this.membershipGroup.containsKey(e.getKey())) {
                this.setHeartbeatFor(otherEntry.getAddr(), otherEntry.getPort(), otherEntry.getHeartbeatCounter());
            }
            MembershipListEntry myEntry = this.membershipGroup.get(e.getKey());
            if (myEntry.getHeartbeatCounter() < otherEntry.getHeartbeatCounter()) {
                this.setHeartbeatFor(otherEntry.getAddr(), otherEntry.getPort(), otherEntry.getHeartbeatCounter());
            }
        }
    }

    public JSONObject getJSON(){
        return getJSON(true);
    }

    public JSONObject getJSON(boolean filter){
        JSONObject obj = new JSONObject();
        JSONObject group = new JSONObject();
        for (Map.Entry<String, MembershipListEntry> entry: this.membershipGroup.entrySet()){
            MembershipListEntry e = entry.getValue();
            if (!filter || (e.getStatus() != NodeStatus.SUSPECT || e.getStatus() != NodeStatus.FAILED)) {
                group.put(entry.getKey(), e.getJSON());
            }
        }
        obj.put("membershipGroup", group);
        return obj;
    }

    @Override
    public String toString() {
        return this.getJSON().toString();
    }

    public void remove(String key){
        this.membershipGroup.remove(key);
    }

    public void remove(String addr, int port){
        String key = getKey(addr, port);
        remove(key);
    }

    public int size(){
        return membershipGroup.size();
    }

}
