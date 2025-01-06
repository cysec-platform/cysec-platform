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
package eu.smesec.cysec.platform.core.threading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;

/**
 * <p>Writing thread. Writes all cached file changes to the files system.</p>
 *
 * @author Claudio Seitz
 * @version 1.0
 */
public class Timer implements IExecutable {
  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private final String name;
  private final long interval;
  private final AtomicBoolean running;
  private final List<Runnable> runnable;

  /**
   * <p>Constructor.
   * Initializes the writing thread.</p>
   * @param name The name of this timer.
   * @param interval The interval of this timer in milliseconds.
   */
  Timer(String name, long interval) {
    this.name = name;
    this.interval = interval;
    this.running = new AtomicBoolean();
    this.runnable = Collections.synchronizedList(new ArrayList<>());
  }

  /**
   * <p>Register a task to execute after each interval.</p>
   *
   * @param task The task to execute.
   */
  public void register(Runnable task) {
    this.runnable.add(task);
  }

  /**
   * <p>Starts the timer thread.</p>
   */
  @Override
  public void start() {
    running.set(true);
    ThreadManager.getInstance().register(() -> {
      try {
        logger.log(Level.INFO, "Started timer: " + name);
        while (!Thread.currentThread().isInterrupted() && running.get()) {
          Thread.sleep(interval);
          for (Runnable runnable : runnable) {
            runnable.run();
          }
        }
        logger.log(Level.INFO, "Stopped timer: " + name);
      } catch (InterruptedException e) {
        logger.log(Level.WARNING, "Interrupted timer: " + name);
      }
    });
  }

  /**
   * <p>Cancels the timer thread.</p>
   */
  @Override
  public void stop() {
    running.set(false);
  }
}
