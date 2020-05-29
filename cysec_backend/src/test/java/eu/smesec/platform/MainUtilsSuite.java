package eu.smesec.platform;

import eu.smesec.platform.utils.FileUtilsZipTest;
import eu.smesec.platform.utils.LocaleTest;
import eu.smesec.platform.utils.ValidatorTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.platform.threading
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FileUtilsZipTest.class,
        LocaleTest.class,
        ValidatorTest.class,
})
public class MainUtilsSuite {
    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(FileUtilsZipTest.class));
        s.addTest(new JUnit4TestAdapter(LocaleTest.class));
        s.addTest(new JUnit4TestAdapter(ValidatorTest.class));
        return s;
    }
}
