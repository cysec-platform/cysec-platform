package eu.smesec.platform.threading;

import java.io.IOException;
import java.nio.file.Path;

public interface IWatcher {
  void invoke(Path path) throws IOException;
}
