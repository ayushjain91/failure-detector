package cs425.mp2.gossip;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MembershipListEntryTest{

    public static final String LOCALHOST = "localhost";
    public static final String ADDR = "127.0.0.0";
    public static final int PORT = 100;
    public static final int HEARTBEAT_COUNTER = 1;

    @Test
    public void testConstructor(){
        MembershipListEntry m = new MembershipListEntry(LOCALHOST, ADDR, PORT, HEARTBEAT_COUNTER);
        assertEquals(m.getAddr(), ADDR);
        assertEquals(m.getPort(), PORT);
        assertEquals(m.getName(), LOCALHOST);
        assertEquals(m.getHeartbeatCounter(), HEARTBEAT_COUNTER);
    }

    @Test
    public void testJSON() throws ParseException {
        MembershipListEntry m = new MembershipListEntry(LOCALHOST, ADDR, PORT, HEARTBEAT_COUNTER);
        MembershipListEntry m_copy = new MembershipListEntry(m.getJSON().toString());
        assertEquals(m.getAddr(), m_copy.getAddr());
        assertEquals(m.getPort(), m_copy.getPort());
        assertEquals(m.getName(), m_copy.getName());
        assertEquals(m.getHeartbeatCounter(), m_copy.getHeartbeatCounter());
    }

    @Test
    public void testStringConstructor(){
        String jsonStr = "{\"heartbeatCounter\":1,\"heartbeatTime\":1449848149572,\"port\":100,\"name\":\"localhost\",\"addr\":\"127.0.0.0\",\"status\":\"ALIVE\"}";
        MembershipListEntry m = null;
        try {
            m = new MembershipListEntry(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(m.getAddr(), ADDR);
        assertEquals(m.getPort(), PORT);
        assertEquals(m.getName(), LOCALHOST);
        assertEquals(m.getHeartbeatCounter(), HEARTBEAT_COUNTER);
    }

    @Test
    public void testSetHeartBeat() throws InterruptedException{
        MembershipListEntry m = new MembershipListEntry(LOCALHOST, ADDR, PORT, HEARTBEAT_COUNTER);
        long oldHeartbeatTime = m.getHeartbeatTime();
        TimeUnit.MILLISECONDS.sleep(2);
        m.setHeartbeat(2);
        assertEquals(m.getHeartbeatCounter(), 2);
        assertTrue(oldHeartbeatTime < m.getHeartbeatTime());
        assertEquals(m.getStatus(), NodeStatus.ALIVE);

    }
}