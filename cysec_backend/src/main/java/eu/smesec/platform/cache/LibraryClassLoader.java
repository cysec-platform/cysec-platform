package eu.smesec.platform.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibraryClassLoader extends ClassLoader {
  //  private static Pattern regexWord = Pattern.compile("^assets/\\w+\\.\\w+$");
  // Hidden data members.

  // Map from class or resource name to contents.
  private Map<String, byte[]> contentMap = new HashMap<>();
  private ClassLoader chain;

  // Exported constructors.

  /**
   * Construct a new JAR class loader. The parent class loader is the system class loader.
   *
   * @param jar Byte array with the contents of the JAR file.
   * @throws NullPointerException (unchecked exception) Thrown if <code>jar</code> is null.
   * @throws IOException Thrown if the JAR file's contents could not be extracted from the
   *     <code>jar</code> byte array.
   */
  public LibraryClassLoader(byte[] jar) throws IOException {
    super();
    readJarContents(jar);
  }

  /**
   * Construct a new JAR class loader with the given parent class loader.
   *
   * @param parent Parent class loader.
   * @param jar Byte array with the contents of the JAR file.
   * @throws NullPointerException (unchecked exception) Thrown if <code>jar</code> is null.
   * @throws IOException Thrown if the JAR file's contents could not be extracted from the
   *     <code>jar</code> byte array.
   */
  public LibraryClassLoader(ClassLoader parent, byte[] jar) throws IOException {
    super(parent);
    this.chain = parent;
    readJarContents(jar);
  }

  private void readJarContents(byte[] jar) throws IOException {
    if (jar == null) {
      throw new NullPointerException("JarClassLoader(): jar is null");
    }
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
