package eu.smesec.platform;

import eu.smesec.platform.config.ConfigTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.platform.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConfigTest.class,
})
public class MainPlatformSuite {
    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));
        return s;
    }
}

