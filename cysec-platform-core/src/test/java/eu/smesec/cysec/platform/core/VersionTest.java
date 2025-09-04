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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class VersionTest {

    @Test
    void getVersion_shouldReturnVersion() {
        String version = Version.getVersion();
        // Version might be null if not running from a JAR
        assertThat(version).isEqualTo(Version.class.getPackage().getSpecificationVersion());
    }

    @Test
    void getBuildVersion_shouldReturnBuildVersion() {
        String buildVersion = Version.getBuildVersion();
        // Build version might be null if not running from a JAR
        assertThat(buildVersion).isEqualTo(Version.class.getPackage().getImplementationVersion());
    }

    @Test
    void main_shouldPrintVersionInfo() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        
        try {
            System.setOut(new PrintStream(outContent));
            Version.main(new String[]{});
            
            String output = outContent.toString();
            assertThat(output).contains("VERSION=");
            assertThat(output).contains("BUILD=");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void version_shouldBeStaticClass() {
        // Version class should only have static methods
        assertThat(Version.class.getDeclaredConstructors()).hasSize(1);
        assertThat(Version.class.getDeclaredConstructors()[0].getParameterCount()).isEqualTo(0);
    }
}
