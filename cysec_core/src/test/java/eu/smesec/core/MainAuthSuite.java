package eu.smesec.core;

import eu.smesec.core.auth.PasswordStorageTest;

import eu.smesec.core.auth.strategies.BasicAuthStrategyTest;
import eu.smesec.core.auth.strategies.HeaderAuthStrategyTest;
import eu.smesec.core.auth.strategies.ReplicaAuthStrategyTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.platform.auth.
 * <p>
 * Created by martin.gwerder on 18.01.2018.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        PasswordStorageTest.class,
        BasicAuthStrategyTest.class,
        HeaderAuthStrategyTest.class,
        ReplicaAuthStrategyTest.class
})
public class MainAuthSuite {
  public static junit.framework.Test suite() {
    final TestSuite s = new TestSuite();
    s.addTest(new JUnit4TestAdapter(PasswordStorageTest.class));
    s.addTest(new JUnit4TestAdapter(BasicAuthStrategyTest.class));
    s.addTest(new JUnit4TestAdapter(HeaderAuthStrategyTest.class));
    s.addTest(new JUnit4TestAdapter(ReplicaAuthStrategyTest.class));
    return s;
  }
}
