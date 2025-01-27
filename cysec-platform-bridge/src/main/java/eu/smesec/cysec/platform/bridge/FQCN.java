/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>Full qualified coach name.
 * Points to an answer *.xml file
 * and an accompanying *-flags.xml file</p>
 */
public class FQCN {
  private static Pattern regex = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*$");

  private final String[] segments;

  private FQCN(String[] segments) {
    this.segments = segments;
  }

  public int size() {
    return segments.length;
  }

  public boolean isTopLevel() {
    return segments.length == 2;
  }

  public Stream<String> coacheIds() {
    return Arrays.stream(segments)
          .limit(segments.length - 1);
  }

  public String getRootCoachId() {
    return segments[0];
  }

  public FQCN getRoot() {
    String[] segments = new String[2];
    segments[0] = this.segments[0];
    segments[1] = "default";
    return new FQCN(segments);
  }

  public String getParentCoachId() {
    if (this.segments.length > 2) {
      return this.segments[this.segments.length - 3];
    }
    return null;
  }

  public FQCN getParent() {
    int size = this.segments.length;
    if (this.segments.length > 2) {
      String[] segments = new String[size - 1];
      System.arraycopy(this.segments, 0, segments, 0, size - 2);
      segments[size - 2] = "default";
      return new FQCN(segments);
    }
    return null;
  }

  public String getCoachId() {
    return segments[segments.length - 2];
  }

  public FQCN resolveDefault(String... coachId) {
    return resolve("default", coachId);
  }

  public FQCN resolve(String instance, String... coachId) {
    int size = this.segments.length;
    String[] segments = new String[size + coachId.length];
    System.arraycopy(this.segments, 0, segments, 0, size - 1);
    System.arraycopy(coachId, 0, segments, size - 1, coachId.length);
    segments[size + coachId.length - 1] = instance;
    return new FQCN(segments);
  }

  public String getName() {
    return segments[segments.length - 1];
  }

  public Path toPath() {
    return Paths.get(String.join("/", segments) + ".xml");
  }

  public Path toFlagsPath() {
    return Paths.get(String.join("/", segments) + "-flags.xml");
  }

  @Override
  public String toString() {
    return String.join(".", segments);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FQCN) {
      return toString().equals(obj.toString());
    }
    return false;
  }

  /**
   * creates a new full qualified name from a given string
   *
   * @param fqcn input string
   * @return full qualified name
   */
  public static FQCN fromString(String fqcn) {
    if (fqcn == null || !regex.matcher(fqcn).matches()) {
      throw new IllegalArgumentException();
    }
    if (fqcn.contains(".")) {
      return new FQCN(fqcn.split("\\."));
    }
    return new FQCN(new String[]{
          fqcn, "default"
    });
  }

  /**
   * creates a new full qualified name from a given string
   *
   * @param path input path
   * @return full qualified name
   */
  public static FQCN fromPath(Path path) {
    if (path == null) {
      throw new IllegalArgumentException();
    }
    String[] segments = StreamSupport.stream(path.spliterator(), false)
          .map(Path::toString)
          .toArray(String[]::new);
    if (segments.length > 1) {
      String name =  segments[segments.length - 1];
      int i = name.lastIndexOf('.');
      if (i > 0) {
        String nameWithoutExt = name.substring(0, name.lastIndexOf('.'));
        segments[segments.length - 1] = nameWithoutExt;
      }
      return new FQCN(segments);
    }
    return new FQCN(new String[] {
          segments[0], "default"
    });
  }

  /**
   * Constructs a FQCN of a subcoach given the root coach ID, the subcoach ID and the instance Name of the subcoach
   * @param rootCoachId The coach ID of the root (parent) coach
   * @param subcoachId The coach ID of the subcoach
   * @param instanceName The instance name of the coach
   * @return a new FQCN containing the passed information
   */
  public static FQCN from(String rootCoachId, String subcoachId, String instanceName) {
    return new FQCN(new String[] { rootCoachId, subcoachId, instanceName });
  }
}
