package eu.smesec.core.cache;

import eu.smesec.bridge.generated.Company;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.utils.FileUtils;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MapperUserTest {
  private Path source;
  private Path temp;
  private Path file;

  @Rule
  public TestName name = new TestName();

  public MapperUserTest() {
    this.source = Paths.get("src/test/resources/data");
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/UserCache/" + name.getMethodName());
    this.file = this.temp.resolve("xyz/users.xml");

    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInstall() {
    Path path = this.temp.resolve("xyz/users-test.xml");
    String companyId = "test";
    String companyName = "testname";
    try {
      Assert.assertFalse(Files.exists(path));
      Company source = new Company();
      source.setId(companyId);
      source.setCompanyname(companyName);
      Mapper<Company> mapper = new Mapper<>(Company.class);

      mapper.init(path, source);

      Assert.assertTrue(Files.exists(path));
      JAXBContext jc = JAXBContext.newInstance(Company.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof Company);
        Company company = (Company) object;
        Assert.assertEquals(companyId, company.getId());
        Assert.assertEquals(companyName, company.getCompanyname());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testUnmarshal() {
    try {
      Assert.assertTrue(Files.exists(file));
      Mapper<Company> mapper = new Mapper<>(Company.class);

      Company company = mapper.unmarshal(file);

      Assert.assertNotNull(company);
      Assert.assertEquals("fhnw", company.getId());
      Assert.assertEquals("FHNW", company.getCompanyname());
      Assert.assertEquals(3, company.getUser().size());
      Assert.assertEquals("admin", company.getUser().get(0).getUsername());
      Assert.assertEquals(1, company.getUser().get(0).getRole().size());
      Assert.assertEquals("Admin", company.getUser().get(0).getRole().get(0));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testMarshal() {
    try {
      Company source = new Company();
      User user = new User();
      user.setUsername("test");
      user.setPassword("==test");
      user.setFirstname("john");
      user.setSurname("test");
      user.setLock(Locks.PENDING);
      source.getUser().add(user);
      Mapper<Company> mapper = new Mapper<>(Company.class);

      mapper.marshal(file, source);

      JAXBContext jc = JAXBContext.newInstance(Company.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Company company = (Company) object;
        Assert.assertEquals(1, company.getUser().size());
        User added = company.getUser().get(0);
        Assert.assertEquals(user.getUsername(), added.getUsername());
        Assert.assertEquals(user.getPassword(), added.getPassword());
        Assert.assertEquals(user.getFirstname(), added.getFirstname());
        Assert.assertEquals(user.getSurname(), added.getSurname());
      }
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
