/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

public class Version {
  private static final String BUILD_VERSION = Version.class.getPackage().getImplementationVersion();
  private static final String VERSION = Version.class.getPackage().getSpecificationVersion();

  public static String getVersion() {
    return VERSION;
  }

  public static String getBuildVersion() {
    return BUILD_VERSION;
  }

  public static void main(String[] args) {
    System.out.println("VERSION=" + getVersion());
    System.out.println("BUILD=" + getBuildVersion());
  }
}