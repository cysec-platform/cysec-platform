package eu.smesec.cysec.platform.core;

import eu.smesec.cysec.platform.core.auth.PasswordStorageTest;
import eu.smesec.cysec.platform.core.auth.strategies.BasicAuthStrategyTest;
import eu.smesec.cysec.platform.core.auth.strategies.ReplicaAuthStrategyTest;

import eu.smesec.cysec.platform.core.auth.strategies.HeaderAuthStrategyTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.cysec.platform.core.auth.
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
