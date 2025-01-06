/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

