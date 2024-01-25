/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Global executor service as singleton. */
public class ThreadManager {
  private static final ThreadManager tm = new ThreadManager();

  /**
   * Returns the singleton instance.
   *
   * @return instance
   */
  public static ThreadManager getInstance() {
    return tm;
  }

  private final ExecutorService service;

  private ThreadManager() {
    service = Executors.newCachedThreadPool();
  }

  /**
   * Submits a new task to the thread manager.
   *
   * @param task The task to execute
   */
  public void register(Runnable task) {
    service.submit(task);
  }

  /** Cancels each task and shuts down the thread manager. */
  public void shutdown() {
    service.shutdownNow();
  }
}
