package ut.com.tapdevelopment.jira;

import org.junit.Test;
import com.tapdevelopment.jira.api.MyPluginComponent;
import com.tapdevelopment.jira.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}