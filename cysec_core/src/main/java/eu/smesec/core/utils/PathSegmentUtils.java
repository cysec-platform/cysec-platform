package eu.smesec.core.utils;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.PathSegment;

public final class PathSegmentUtils {
  private PathSegmentUtils() {}

  /**
   * Combines the path segments as a single string using '/'.
   *
   * @param segments path segments
   * @return combined string
   */
  public static String combine(List<PathSegment> segments) {
    return segments.stream()
          .map(PathSegment::getPath)
          .collect(Collectors.joining("/"));
  }
}
