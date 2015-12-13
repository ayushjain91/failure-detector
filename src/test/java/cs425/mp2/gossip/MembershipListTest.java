package cs425.mp2.gossip;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ayush on 12/11/15.
 */
public class MembershipListTest {

    public static final String ADDR = "127.0.0.0";
    public static final int PORT = 100;

    @Test
    public void testConstructor(){
        MembershipList list = new MembershipList(ADDR, PORT);
        list.setHeartbeatFor("http://www.google.com", 100, 23);
        list.setHeartbeatFor("http://gmail.com", 110, 24);
        assertEquals(list.size(), 3);
       for (Iterator<Map.Entry<String, MembershipListEntry>> membersIter = list.iterator(); membersIter.hasNext();) {
           Map.Entry<String, MembershipListEntry> e = membersIter.next();
           MembershipListEntry node = e.getValue();
           if (node.getAddr() == ADDR) {
               assertEquals(node.getPort(), PORT);
           } else if (node.getAddr() == "http://www.google.com") {
               assertEquals(node.getPort(), 100);
               assertEquals(node.getHeartbeatCounter(), 23);
           } else if (node.getAddr() == "http://gmail.com") {
               assertEquals(node.getPort(), 110);
               assertEquals(node.getHeartbeatCounter(), 24);
           } else {
               assertTrue("Unexpected entry in MembershipList", false);
           }
       }
    }

    @Test
    public void testStringConstructor(){
        String jsonStr = "{\"membershipGroup\":{\"http:\\/\\/www.google.com:100\":{\"heartbeatCounter\":23,\"heartbeatTime\":1449851486476,\"port\":100,\"name\":\"http:\\/\\/www.google.com:100\",\"addr\":\"http:\\/\\/www.google.com\",\"status\":\"ALIVE\"},\"http:\\/\\/gmail.com:110\":{\"heartbeatCounter\":24,\"heartbeatTime\":1449851486476,\"port\":110,\"name\":\"http:\\/\\/gmail.com:110\",\"addr\":\"http:\\/\\/gmail.com\",\"status\":\"ALIVE\"},\"127.0.0.0:100\":{\"heartbeatCounter\":1,\"heartbeatTime\":1449851486474,\"port\":100,\"name\":\"127.0.0.0:100\",\"addr\":\"127.0.0.0\",\"status\":\"ALIVE\"}}}";
        MembershipList list = null;
        try {
            list = new MembershipList(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(list.size(), 3);
        for (Iterator<Map.Entry<String, MembershipListEntry>> membersIter = list.iterator(); membersIter.hasNext();){
            Map.Entry<String, MembershipListEntry> e = membersIter.next();
            MembershipListEntry node = e.getValue();
            if (Objects.equals(node.getAddr(), ADDR)){
                assertEquals(node.getPort(), PORT);
            }
            else if (Objects.equals(node.getAddr(), "http://www.google.com")){
                assertEquals(node.getPort(), 100);
                assertEquals(node.getHeartbeatCounter(), 23);
            }
            else if (Objects.equals(node.getAddr(), "http://gmail.com")){
                assertEquals(node.getPort(), 110);
                assertEquals(node.getHeartbeatCounter(), 24);
            }
            else {
                assertTrue("Unexpected entry in MembershipList", false);
            }
        }
    }

    @Test
    public void testJSON(){
        MembershipList list = new MembershipList(ADDR, PORT);
        list.setHeartbeatFor("http://www.google.com", 100, 23);
        list.setHeartbeatFor("http://gmail.com", 110, 24);

        MembershipList list_copy = null;
        try {
            list_copy = new MembershipList(list.getJSON().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(list_copy.size(), 3);
        for (Iterator<Map.Entry<String, MembershipListEntry>> membersIter = list_copy.iterator(); membersIter.hasNext();){
            Map.Entry<String, MembershipListEntry> e = membersIter.next();
            MembershipListEntry node = e.getValue();
            if (Objects.equals(node.getAddr(), ADDR)){
                assertEquals(node.getPort(), PORT);
            }
            else if (Objects.equals(node.getAddr(), "http://www.google.com")){
                assertEquals(node.getPort(), 100);
                assertEquals(node.getHeartbeatCounter(), 23);
            }
            else if (Objects.equals(node.getAddr(), "http://gmail.com")){
                assertEquals(node.getPort(), 110);
                assertEquals(node.getHeartbeatCounter(), 24);
            }
            else {
                assertTrue("Unexpected entry in MembershipList", false);
            }
        }
    }

    @Test
    public void testMerge(){
        MembershipList list = new MembershipList(ADDR, PORT);
        list.setHeartbeatFor("http://www.google.com", 100, 23);
        list.setHeartbeatFor("http://gmail.com", 110, 24);
        int old_heartbeat_google = list.getHeartbeatFor("http://www.google.com", 100);
        long old_heartbeatTime_google = list.getHeartbeatTime("http://www.google.com", 100);
        int old_heartbeat_gmail = list.getHeartbeatFor("http://gmail.com", 110);
        long old_heartbeatTime_gmail = list.getHeartbeatTime("http://gmail.com", 110);

        MembershipList incoming_list = new MembershipList("128.174.24.55", 3128);
        incoming_list.setHeartbeatFor("http://www.google.com", 100, 25);
        incoming_list.setHeartbeatFor("http://gmail.com", 110, 20);
        incoming_list.setHeartbeatFor("http://hotmail.com", 200, 40);
        int new_heartbeat_google = incoming_list.getHeartbeatFor("http://www.google.com", 100);
        long new_heartbeatTime_google = incoming_list.getHeartbeatTime("http://www.google.com", 100);
        int new_heartbeat_gmail = incoming_list.getHeartbeatFor("http://gmail.com", 110);
        long new_heartbeatTime_gmail = incoming_list.getHeartbeatTime("http://gmail.com", 110);

        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list.merge(incoming_list.toString());
        assertEquals("Heartbeat not updated", list.getHeartbeatFor("http://www.google.com", 100), 25);
        assertTrue("Heartbeat time not updated", old_heartbeatTime_google < list.getHeartbeatTime("http://www.google.com", 100));
        assertEquals("Heartbeat updated when incoming heartbeat is smaller", list.getHeartbeatFor("http://gmail.com", 110), old_heartbeat_gmail);
        assertTrue("Heartbeat time updated when incoming heartbeat is smaller", old_heartbeatTime_gmail == list.getHeartbeatTime("http://gmail.com", 110));
        assertTrue("Incoming new node not added", list.hasNode("http://hotmail.com", 200));
    }
}
