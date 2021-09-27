package eu.smesec.cysec.platform.core;

import eu.smesec.cysec.platform.core.cache.CoachCacheTest;
import eu.smesec.cysec.platform.core.cache.CompanyCacheTest;
import eu.smesec.cysec.platform.core.cache.DataCacheTest;
import eu.smesec.cysec.platform.core.cache.LibraryTest;
import eu.smesec.cysec.platform.core.cache.MapperAnswerTest;
import eu.smesec.cysec.platform.core.cache.MapperAuditTest;
import eu.smesec.cysec.platform.core.cache.MapperCoachTest;
import eu.smesec.cysec.platform.core.cache.MapperUserTest;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.cysec.platform.core.cache
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CoachCacheTest.class,
        CompanyCacheTest.class,
        DataCacheTest.class,
        LibraryTest.class,
        MapperAnswerTest.class,
        MapperAuditTest.class,
        MapperCoachTest.class,
        MapperUserTest.class,
})
public class MainCacheSuite {
    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(CoachCacheTest.class));
        s.addTest(new JUnit4TestAdapter(CompanyCacheTest.class));
        s.addTest(new JUnit4TestAdapter(DataCacheTest.class));
        s.addTest(new JUnit4TestAdapter(LibraryTest.class));
        s.addTest(new JUnit4TestAdapter(MapperAnswerTest.class));
        s.addTest(new JUnit4TestAdapter(MapperAuditTest.class));
        s.addTest(new JUnit4TestAdapter(MapperCoachTest.class));
        s.addTest(new JUnit4TestAdapter(MapperUserTest.class));
        return s;
    }
}

