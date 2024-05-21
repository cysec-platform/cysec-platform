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
package eu.smesec.cysec.platform.core.threading;

public final class ThreadFactory {
  private ThreadFactory() {}

  /**
   * <p>Creates a new timer.</p>
   *
   * @param name The name of the timer.
   * @param interval The interval of the timer in milliseconds.
   * @return <code>Timer</code>
   */
  public static Timer createTimer(String name, long interval) {
    return new Timer(name, interval);
  }

  /**
   * <p>Creates a new FileWatcher.</p>
   *
   * @return <code>FileWatcher</code>
   */
  public static FileWatcher createFileWatcher() {
    return new FileWatcher();
  }
}
