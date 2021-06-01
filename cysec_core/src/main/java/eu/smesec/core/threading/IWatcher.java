package eu.smesec.core.threading;

import java.io.IOException;
import java.nio.file.Path;

public interface IWatcher {
  void invoke(Path path) throws IOException;
}
