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
package eu.smesec.cysec.platform.core.cache;

import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;

public abstract class Cache {
  protected static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  protected Path path;
  protected ReentrantReadWriteLock.ReadLock readLock;
  protected ReentrantReadWriteLock.WriteLock writeLock;

  /**
   * Cache base class constructor.
   *
   * @param path Cache directory path
   */
  public Cache(Path path) {
    this.path = path;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
  }

  public Path getPath() {
    return path;
  }
}
