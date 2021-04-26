package eu.smesec.platform.cache;

import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.Audits;
import eu.smesec.bridge.generated.UserAction;
import eu.smesec.platform.utils.FileUtils;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.GregorianCalendar;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MapperAuditTest {
  private Path source;
  private Path temp;
  private Path file;

  @Rule
  public TestName name = new TestName();

  public MapperAuditTest() {
    this.source = Paths.get("src/test/resources/data");
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/AuditCache/" + name.getMethodName());
    this.file = this.temp.resolve("xyz/audits.xml");
    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInit() {
    Path path = this.temp.resolve("xyz/audit-test.xml");
    try {
      Audits source = new Audits();
      Assert.assertFalse(Files.exists(path));
      Mapper<Audits> mapper = new Mapper<>(Audits.class);

      mapper.init(path, source);

      Assert.assertTrue(Files.exists(path));
      JAXBContext jc = JAXBContext.newInstance(Audits.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof Audits);
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
      Mapper<Audits> mapper = new Mapper<>(Audits.class);

      Audits audits = mapper.unmarshalWithInit(file, Audits.class);

      Assert.assertNotNull(audits);
      Assert.assertEquals(3, audits.getAudit().size());
      Assert.assertEquals("user1", audits.getAudit().get(0).getUser());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testMarshal() {
    try {
      Audits source = new Audits();
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar now =
            datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
      Audit audit = new Audit();
      audit.setUser("newUser");
      audit.setTime(now);
      audit.setAction(UserAction.CREATED);
      audit.setBefore("");
      audit.setAfter("example");
      source.getAudit().add(audit);
      Mapper<Audits> mapper = new Mapper<>(Audits.class);

      mapper.marshal(file, source);

      JAXBContext jc = JAXBContext.newInstance(Audits.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Audits audits = (Audits) object;
        Assert.assertEquals(1, audits.getAudit().size());
        Audit added = audits.getAudit().get(0);
        Assert.assertEquals(audit.getUser(), added.getUser());
        Assert.assertEquals(audit.getTime(), added.getTime());
        Assert.assertEquals(audit.getAction(), added.getAction());
        Assert.assertEquals(audit.getBefore(), added.getBefore());
        Assert.assertEquals(audit.getAfter(), added.getAfter());
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
