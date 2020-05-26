package eu.smesec.platform.utils;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.PathSegment;

public final class PathSegmentUtils {
  private PathSegmentUtils() {}

  public static String combine(List<PathSegment> segments) {
    return segments.stream()
          .map(PathSegment::getPath)
          .collect(Collectors.joining("/"));
  }
}
