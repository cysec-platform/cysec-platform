package eu.smesec.core.cache;

import eu.smesec.bridge.Library;
import eu.smesec.core.utils.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryTest {
  private Path source;
  private Path temp;

  public LibraryTest() {
    this.source = Paths.get("src/test/resources/coaches");
  }

  @Rule
  public TestName name = new TestName();

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/CoachCache/" + name.getMethodName());

    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testLoadLibrary() {
    Path file = this.temp.resolve("eu.smesec.library.company.CompanyLib.txt");
    try {
      Assert.assertTrue(Files.exists(file));
      String libId = FileUtils.getNameExt(file.getFileName().toString())[0];
      String libEnc = Files.readAllLines(file).get(0);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      eu.smesec.bridge.generated.Library lib = new eu.smesec.bridge.generated.Library();
      lib.setId(libId);
      lib.setValue(libEnc);

      Library concreteLibrary = CacheFactory.loadLibrary(classLoader, lib);

      Assert.assertNotNull(concreteLibrary);
      Assert.assertEquals(libId, concreteLibrary.getClass().getName());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDir(temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
