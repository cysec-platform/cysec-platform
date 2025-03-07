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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class LibraryClassLoader extends ClassLoader {
  // Map from class or resource name to contents.
  private Map<String, byte[]> contentMap = new HashMap<>();
  private ClassLoader chain;

  // Exported constructors.

  /**
   * Construct a new JAR class loader with the given parent class loader.
   *
   * @param parent Parent class loader.
   * @throws NullPointerException (unchecked exception) Thrown if <code>jar</code> is null.
   */
  public LibraryClassLoader(ClassLoader parent) {
    super(parent);
    this.chain = parent;
  }

  public void loadJar(byte[] jar) throws IOException {
    if (jar == null) {
      throw new NullPointerException("JarClassLoader(): jar is null");
    }
    contentMap = new HashMap<>();
    JarInputStream in = new JarInputStream(new ByteArrayInputStream(jar));
    JarEntry jarEntry;
    String name;
    ByteArrayOutputStream out;
    byte[] buf = new byte[1024];
    int n;
    while ((jarEntry = in.getNextJarEntry()) != null) {
      name = jarEntry.getName();
      out = new ByteArrayOutputStream();
      while ((n = in.read(buf, 0, 1024)) != -1) {
        out.write(buf, 0, n);
      }
      contentMap.put(name, out.toByteArray());
    }
  }

  // Hidden operations.

  /**
   * Find the class with the given name.
   *
   * @param name Class name.
   * @return Class object.
   * @throws ClassNotFoundException Thrown if the class could not be found.
   */
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String contentName = name.replace('.', '/') + ".class";
    byte[] content = contentMap.get(contentName);
    if (content == null) {
      return this.chain.loadClass(name);
    }

    return defineClass(name, content, 0, content.length);
  }

  /**
   * Get an input stream for reading the given resource.
   *
   * @param name Resource name.
   * @return Input stream, or null if the resource could not be found.
   */
  public InputStream getResourceAsStream(String name) {
    byte[] content = contentMap.get(name);
    return content == null ? null : new ByteArrayInputStream(content);
  }

  /**
   * Iterates over each resource.
   *
   * @param function consumer function
   */
  public void forEachResource(BiConsumer<String, InputStream> function) {
    for (Map.Entry<String, byte[]> entry : contentMap.entrySet()) {
      function.accept(entry.getKey(), new ByteArrayInputStream(entry.getValue()));
    }
  }
}
