/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.core.config.ConfigTest;
import eu.smesec.cysec.platform.core.endpoints.ResourcesTest;
import eu.smesec.cysec.platform.core.endpoints.SignUpTest;
import eu.smesec.cysec.platform.core.endpoints.UserTest;
import eu.smesec.cysec.platform.core.json.ClassFieldsExclusionStrategyTest;
import eu.smesec.cysec.platform.core.json.FieldsExclusionStrategyTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all classes related to eu.smesec.cysec.platform.core.
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

