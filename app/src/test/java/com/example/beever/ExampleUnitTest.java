package com.example.beever;

import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.example.beever.database.TodoEntry;
import com.example.beever.database.UserEntry;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class ExampleUnitTest {
    @Test
    public void testUser() {
        UserEntry user = new UserEntry("username","name","email",new ArrayList<Object>(), new ArrayList<Object>(), new HashMap<String,Object>(), new HashMap<String,Object>());
        user.assignDashboardGrp(0,"0");
        user.assignDashboardGrp(1,"1");
        user.addGroupId("1");
        user.addGroupId("0");

        assertEquals(Arrays.asList((Object)"0",(Object)"1",null,null,null,null),user.getDashboard_grps());
        assertEquals(Arrays.asList((Object)"1",(Object)"0"),user.getGroups());

        user.removeGroupId("2");
        assertEquals(Arrays.asList((Object)"1",(Object)"0"),user.getGroups());
        user.removeGroupId("1");
        assertEquals(Arrays.asList((Object)"0"),user.getGroups());
        Timestamp x = new Timestamp(20,20);
        EventEntry a = new EventEntry("a","a",x,x);
        EventEntry b = new EventEntry("b","b",x,x);
        EventEntry c = new EventEntry("c","c",x,x);
        user.modifyEventOrTodo(true,true,true,a);
        user.modifyEventOrTodo(true,false,true,b);
        Map<String,Object> testMap1 = new HashMap<String,Object>();
        List<Object> l1 = new ArrayList<Object>();
        l1.add(a.getRepresentation());
        testMap1.put("current",l1);
        testMap1.put("past",Arrays.asList((Object)b.getRepresentation()));
        assertEquals(user.getUser_events(),testMap1);
        user.modifyEventOrTodo(true,true,false,c);
        user.modifyEventOrTodo(true,false,false,c);
        assertEquals(user.getUser_events(),testMap1);
        l1.remove(a.getRepresentation());
        user.modifyEventOrTodo(true,true,false,a);
        assertEquals(user.getUser_events(),testMap1);
        TodoEntry a1 = new TodoEntry("a","b","c",x);
        TodoEntry a2 = new TodoEntry("b","c","d",x);
        TodoEntry a3 = new TodoEntry("c","d","e",x);
        user.modifyEventOrTodo(false,true,false,a1);
        user.modifyEventOrTodo(false,true,true,a1);
        user.modifyEventOrTodo(false,false,true,a2);
        Map<String,Object> testMap2 = new HashMap<String,Object>();
        testMap2.put("current",Arrays.asList((Object)a1.getRepresentation()));
        List<Object> l2 = new ArrayList<Object>();
        l2.add(a2.getRepresentation());
        testMap2.put("past",l2);
        assertEquals(user.getTodo_list(),testMap2);
        user.modifyEventOrTodo(false,true,false,a3);
        user.modifyEventOrTodo(false,false,false,a3);
        assertEquals(user.getTodo_list(),testMap2);
        l2.remove(a2.getRepresentation());
        user.modifyEventOrTodo(false,false,false,a2);
        assertEquals(user.getTodo_list(),testMap2);
    }

    @Test
    public void testGroup(){
        Map<String,Object> events = new HashMap<String,Object>();
        Map<String,Object> todo = new HashMap<String,Object>();
        List<Object> e1 = new ArrayList<Object>();
        List<Object> e2 = new ArrayList<Object>();
        List<Object> t1 = new ArrayList<Object>();
        List<Object> t2 = new ArrayList<Object>();

        events.put("current",e1);
        events.put("past",e2);
        todo.put("current",t1);
        todo.put("past",t2);
        GroupEntry group = new GroupEntry((long)0,"yeet",new ArrayList<Object>(),events,todo,new ArrayList<Object>());
        group.addUserId("0");
        group.addUserId("1");
        assertEquals(Arrays.asList((Object)"0",(Object)"1"),group.getMember_list());
        group.removeUserId("2");
        assertEquals(Arrays.asList((Object)"0",(Object)"1"),group.getMember_list());
        group.removeUserId("1");
        assertEquals(Arrays.asList((Object)"0"),group.getMember_list());
        Timestamp x = new Timestamp(20,20);
        EventEntry a = new EventEntry("a","a",x,x);
        EventEntry b = new EventEntry("b","b",x,x);
        EventEntry c = new EventEntry("c","c",x,x);
        group.modifyEventOrTodo(true,true,true,a);
        group.modifyEventOrTodo(true,false,true,b);
        Map<String,Object> testMap1 = new HashMap<String,Object>();
        List<Object> l1 = new ArrayList<Object>();
        l1.add(a.getRepresentation());
        testMap1.put("current",l1);
        testMap1.put("past",Arrays.asList((Object)b.getRepresentation()));
        assertEquals(group.getGroup_events(),testMap1);
        group.modifyEventOrTodo(true,true,false,c);
        group.modifyEventOrTodo(true,false,false,c);
        assertEquals(group.getGroup_events(),testMap1);
        l1.remove(a.getRepresentation());
        group.modifyEventOrTodo(true,true,false,a);
        assertEquals(group.getGroup_events(),testMap1);
        TodoEntry a1 = new TodoEntry("a","b","c",x);
        TodoEntry a2 = new TodoEntry("b","c","d",x);
        TodoEntry a3 = new TodoEntry("c","d","e",x);
        group.modifyEventOrTodo(false,true,false,a1);
        group.modifyEventOrTodo(false,true,true,a1);
        group.modifyEventOrTodo(false,false,true,a2);
        Map<String,Object> testMap2 = new HashMap<String,Object>();
        testMap2.put("current",Arrays.asList((Object)a1.getRepresentation()));
        List<Object> l2 = new ArrayList<Object>();
        l2.add(a2.getRepresentation());
        testMap2.put("past",l2);
        assertEquals(group.getTodo_list(),testMap2);
        group.modifyEventOrTodo(false,true,false,a3);
        group.modifyEventOrTodo(false,false,false,a3);
        assertEquals(group.getTodo_list(),testMap2);
        l2.remove(a2.getRepresentation());
        group.modifyEventOrTodo(false,false,false,a2);
        assertEquals(group.getTodo_list(),testMap2);
    }

}