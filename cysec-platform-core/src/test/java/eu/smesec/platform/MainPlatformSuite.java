package eu.smesec.platform;

import eu.smesec.platform.config.ConfigTest;
import eu.smesec.platform.endpoints.ResourcesTest;
import eu.smesec.platform.endpoints.SignUpTest;
import eu.smesec.platform.endpoints.UserTest;
import eu.smesec.platform.json.ClassFieldsExclusionStrategyTest;
import eu.smesec.platform.json.FieldsExclusionStrategyTest;
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
        FieldsExclusionStrategyTest.class,
        ClassFieldsExclusionStrategyTest.class,
        ResourcesTest.class,
        SignUpTest.class,
        UserTest.class,
})
public class MainPlatformSuite {
    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));
        s.addTest(new JUnit4TestAdapter(FieldsExclusionStrategyTest.class));
        s.addTest(new JUnit4TestAdapter(ClassFieldsExclusionStrategyTest.class));
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));
        s.addTest(new JUnit4TestAdapter(ResourcesTest.class));
        s.addTest(new JUnit4TestAdapter(SignUpTest.class));
        s.addTest(new JUnit4TestAdapter(UserTest.class));
        return s;
    }
}

