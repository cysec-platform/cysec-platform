package eu.smesec.platform;

import eu.smesec.platform.threading.FileWatcherTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.platform.threading
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FileWatcherTest.class,
})
public class MainThreadingSuite {
    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(FileWatcherTest.class));
        return s;
    }
}