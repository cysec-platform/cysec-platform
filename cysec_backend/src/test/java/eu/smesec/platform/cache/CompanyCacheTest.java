package eu.smesec.platform.cache;

import eu.smesec.bridge.execptions.CacheReadOnlyException;
import eu.smesec.bridge.generated.*;
import eu.smesec.platform.utils.FileResponse;
import eu.smesec.platform.utils.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheFactory.class, FileResponse.class})
public class CompanyCacheTest {
  private Path source;
  private Path temp;
  private Path compDir;
  private Path compDirRO;

  private Mapper<Company> companyMapper;
  private Mapper<Audits> auditsMapper;
  private Mapper<Answers> answersMapper;

  @Rule
  public TestName name = new TestName();

  @SuppressWarnings("unchecked")
  public CompanyCacheTest() {
    this.source = Paths.get("src/test/resources/data");
    this.companyMapper = PowerMockito.mock(Mapper.class);
    this.auditsMapper = PowerMockito.mock(Mapper.class);
    this.answersMapper = PowerMockito.mock(Mapper.class);

    try {
      PowerMockito.mockStatic(CacheFactory.class);
      PowerMockito.when(CacheFactory.createMapper(Answers.class)).thenReturn(answersMapper);
      PowerMockito.when(CacheFactory.createMapper(Company.class)).thenReturn(companyMapper);
      PowerMockito.when(CacheFactory.createMapper(Audits.class)).thenReturn(auditsMapper);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/CompanyCache/" + name.getMethodName());
    this.compDir = this.temp.resolve("xyz");
    this.compDirRO = this.temp.resolve("ro");

    try {
      FileUtils.copyDir(source, temp);
      Mockito.reset(companyMapper, auditsMapper, answersMapper);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInstall() {
    Path path = temp.resolve("test-company");
    Path pathUsers = path.resolve(CompanyCache.USER_XML);
    Path pathAudits = path.resolve(CompanyCache.AUDITS_XML);
    try {
      Audits audits = new Audits();
      Company company = new Company();
      Assert.assertFalse(Files.exists(path));
      CompanyCache companyCache = new CompanyCache(path);
      String replicaToken = "5TGJO9UJKMOP09IUJMHNZTTGB";

      companyCache.install(company, audits, replicaToken);

      Assert.assertTrue(Files.exists(path));
      Assert.assertTrue(Files.isDirectory(path));
      Mockito.verify(companyMapper, Mockito.times(1))
            .init(pathUsers, company);
      Mockito.verify(auditsMapper, Mockito.times(1))
            .init(pathAudits, audits);
      Assert.assertEquals(replicaToken,
            Files.readAllLines(path.resolve(CompanyCache.REPLICA_TOKEN_FILE)).get(0));
      Assert.assertFalse(Files.exists(path.resolve("readonly")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testLoad() {
    Company company = new Company();
    User user1 = new User();
    User user2 = new User();
    User user3 = new User();
    user1.setId(1001L);
    user2.setId(1002L);
    user3.setId(1003L);
    company.getUser().add(user1);
    company.getUser().add(user2);
    company.getUser().add(user3);
    try {
      PowerMockito.when(companyMapper.unmarshal(compDir.resolve(CompanyCache.USER_XML)))
          .thenReturn(company);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache companyCache = new CompanyCache(compDir);

      companyCache.load();

      Assert.assertEquals("37V0P9GZMC4PC70P394G83P8G", companyCache.getReplicaToken());
      Assert.assertFalse(companyCache.isReadOnly());
      Assert.assertEquals(1004L, companyCache.nextUserId());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testLoadReadonly() {
    try {
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache companyCache = new CompanyCache(compDirRO);

      companyCache.load();

      Assert.assertEquals("37V0P9GZMC4PC70P394G83P8G", companyCache.getReplicaToken());
      Assert.assertTrue(companyCache.isReadOnly());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnAnswers() {
    Path relative = Paths.get("lib-coach/default.xml");
    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative)))
            .thenReturn(answers);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache companyCache = new CompanyCache(compDir);

      companyCache.load();
      companyCache.readOnAnswers(relative, answers1 -> {
        Assert.assertEquals(answers, answers1);
        Assert.assertNotSame(answers, answers1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnAnswersReadonly() {
    Path relative = Paths.get("lib-coach/default.xml");
    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDirRO.resolve(relative)))
            .thenReturn(answers);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.readOnAnswers(relative, answers1 -> {
        Assert.assertEquals(answers, answers1);
        Assert.assertNotSame(answers, answers1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnAllAnswers() {
    Path path1 = Paths.get("lib-coach/default.xml");
    Path path2 = Paths.get("lib-coach/lib-coach-sub/A.xml");
    Path path3 = Paths.get("lib-coach/lib-coach-sub/B.xml");
    try {
      Answers answers1 = new Answers();
      Answers answers2 = new Answers();
      Answers answers3 = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(path1)))
            .thenReturn(answers1);
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(path2)))
            .thenReturn(answers2);
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(path3)))
            .thenReturn(answers3);

      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache companyCache = new CompanyCache(compDir);
      companyCache.load();
      companyCache.readOnAllAnswers(map -> {
        Answers ans1 = map.get(path1);
        Answers ans2 = map.get(path2);
        Answers ans3 = map.get(path3);

        Assert.assertEquals(answers1, ans1);
        Assert.assertNotSame(answers1, ans1);
        Assert.assertEquals(answers2, ans2);
        Assert.assertNotSame(answers2, ans2);
        Assert.assertEquals(answers3, ans3);
        Assert.assertNotSame(answers3, ans3);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnUsers() {
    try {
      Company company = new Company();
      PowerMockito.when(companyMapper.unmarshal(compDir.resolve(CompanyCache.USER_XML)))
            .thenReturn(company);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache cache = new CompanyCache(compDir);

      cache.load();
      cache.readOnUsers(company1 -> {
        Assert.assertEquals(company, company1);
        Assert.assertNotSame(company, company1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnUsersReadonly() {
    try {
      Company company = new Company();
      PowerMockito.when(companyMapper.unmarshal(compDirRO.resolve(CompanyCache.USER_XML)))
            .thenReturn(company);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.readOnUsers(company1 -> {
        Assert.assertEquals(company, company1);
        Assert.assertNotSame(company, company1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnAudits() {
    try {
      Audits audits = new Audits();
      PowerMockito.when(auditsMapper.unmarshal(compDir.resolve(CompanyCache.AUDITS_XML)))
            .thenReturn(audits);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache cache = new CompanyCache(compDir);

      cache.load();
      cache.readOnAudits(audits1 -> {
        Assert.assertEquals(audits, audits1);
        Assert.assertNotSame(audits, audits1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadOnAuditsReadonly() {
    try {
      Audits audits = new Audits();
      PowerMockito.when(auditsMapper.unmarshal(compDirRO.resolve(CompanyCache.AUDITS_XML)))
            .thenReturn(audits);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.readOnAudits(audits1 -> {
        Assert.assertEquals(audits, audits1);
        Assert.assertNotSame(audits, audits1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnAnswers() {
    Path relative = Paths.get("lib-coach/default.xml");

    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative)))
            .thenReturn(answers);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache cache = new CompanyCache(compDir);

      cache.load();
      cache.writeOnAnswers(relative, answers1 -> {
        Assert.assertSame(answers, answers1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnAnswersReadonly() {
    Path relative = Paths.get("lib-coach/default.xml");

    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDirRO.resolve(relative)))
            .thenReturn(answers);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.writeOnAnswers(relative, answers1 -> {
        Assert.fail();
        return null;
      });
    } catch (CacheReadOnlyException ignored) {
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnUsers() {
    try {
      Company company = new Company();
      PowerMockito.when(companyMapper.unmarshal(compDir.resolve(CompanyCache.USER_XML)))
            .thenReturn(company);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache cache = new CompanyCache(compDir);

      cache.load();
      cache.writeOnUsers(company1 -> {
        Assert.assertSame(company, company1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnUsersReadonly() {
    try {
      Company company = new Company();
      PowerMockito.when(companyMapper.unmarshal(compDirRO.resolve(CompanyCache.USER_XML)))
            .thenReturn(company);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.writeOnUsers(company1 -> {
        Assert.fail();
        return null;
      });
    } catch (CacheReadOnlyException ignore) {
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnAudits() {
    try {
      Audits audits = new Audits();
      PowerMockito.when(auditsMapper.unmarshal(compDir.resolve(CompanyCache.AUDITS_XML)))
            .thenReturn(audits);
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));
      CompanyCache cache = new CompanyCache(compDir);

      cache.load();
      cache.writeOnAudits(audits1 -> {
        Assert.assertSame(audits, audits1);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testWriteOnAuditsReadonly() {
    try {
      Audits audits = new Audits();
      PowerMockito.when(auditsMapper.unmarshal(compDirRO.resolve(CompanyCache.AUDITS_XML)))
            .thenReturn(audits);
      Assert.assertTrue(Files.exists(compDirRO));
      Assert.assertTrue(Files.isDirectory(compDirRO));
      CompanyCache cache = new CompanyCache(compDirRO);

      cache.load();
      cache.writeOnAudits(audits1 -> {
        Assert.fail();
        return null;
      });
    } catch (CacheReadOnlyException ignore) {
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSaveObject() {
    Path relative = Paths.get("lib-coach/default.xml");
    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative)))
            .thenReturn(answers);
      CompanyCache cache = new CompanyCache(compDir);
      cache.load();
      // load into cache
      cache.readOnAnswers(relative, a -> null);
      cache.saveCachedObject(relative);

      Mockito.verify(answersMapper, Mockito.times(1))
            .marshal(compDir.resolve(relative), answers);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSaveInvalidObjects() {
    Path relative = Paths.get("lib-coach/default.xml");
    try {
      Answers answers = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative)))
            .thenReturn(answers);
      CompanyCache cache = new CompanyCache(compDir);
      cache.load();

      cache.saveCachedObject(relative);
      Mockito.verify(answersMapper, Mockito.times(0))
            .marshal(compDir.resolve(relative), answers);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSaveAllObjects() {
    Path relative1 = Paths.get("lib-coach/default.xml");
    Path relative2 = Paths.get("lib-coach/lib-coach-sub/A.xml");
    Path relative3 = Paths.get("lib-coach/lib-coach-sub/B.xml");
    try {
      Answers answers1 = new Answers();
      Answers answers2 = new Answers();
      Answers answers3 = new Answers();
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative1)))
            .thenReturn(answers1);
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative2)))
            .thenReturn(answers2);
      PowerMockito.when(answersMapper.unmarshal(compDir.resolve(relative3)))
            .thenReturn(answers3);
      CompanyCache cache = new CompanyCache(compDir);
      cache.load();

      // load into cache
      cache.readOnAnswers(relative1, a -> null);
      cache.readOnAnswers(relative2, a -> null);
      cache.readOnAnswers(relative3, a -> null);
      cache.saveCachedObjects();

      Mockito.verify(answersMapper, Mockito.times(1))
            .marshal(compDir.resolve(relative1), answers1);
      Mockito.verify(answersMapper, Mockito.times(1))
            .marshal(compDir.resolve(relative2), answers2);
      Mockito.verify(answersMapper, Mockito.times(1))
            .marshal(compDir.resolve(relative3), answers3);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testZip() {
    Path path = temp.resolve("company.zip");
    Set<String> entries = new HashSet<>(Arrays.asList(
          "audit.xml",
          "users.xml",
          "lib-coach/default.xml",
          "lib-coach/lib-coach-sub/A.xml",
          "lib-coach/lib-coach-sub/B.xml"
    ));
    try {
      Assert.assertTrue(Files.exists(compDir));
      Assert.assertTrue(Files.isDirectory(compDir));

      CompanyCache cache = new CompanyCache(compDir);
      cache.zip(path);

      // verify zip file
      Assert.assertTrue(Files.exists(path));
      try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(path))) {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          String entryName = zipEntry.getName().replace("\\", "/");
          Assert.assertTrue(entries.remove(entryName));
          zipEntry = zis.getNextEntry();
        }
      }
      Assert.assertTrue(entries.isEmpty());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testCreateFileResponse() {
    Path relative = Paths.get(CompanyCache.USER_XML);
    try {
      CompanyCache cache = new CompanyCache(compDir);
      Assert.assertNotNull(cache.createFileResponse(relative));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInvalidFileResponse() {
    Path relative = Paths.get("test.txt");
    try {
      CompanyCache cache = new CompanyCache(compDir);
      Assert.assertNull(cache.createFileResponse(relative));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSyncFile() {
    Path newFile = Paths.get("src/test/resources/test.txt");
    Path relative = Paths.get("test.txt");
    try {
      CompanyCache cache = new CompanyCache(compDir);
      cache.load();
      try (InputStream is = Files.newInputStream(newFile)) {
        cache.syncFile(relative, is, true);
      }
      Assert.assertEquals("content", Files.readAllLines(compDir.resolve(relative)).get(0));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSyncFileReadonly() {
    Path newFile = Paths.get("src/test/resources/test.txt");
    Path relative = Paths.get("test.txt");
    try {
      CompanyCache cache = new CompanyCache(compDirRO);
      cache.load();
      try (InputStream is = Files.newInputStream(newFile)) {
        cache.syncFile(relative, is, true);
      }
      Assert.assertEquals("content", Files.readAllLines(compDirRO.resolve(relative)).get(0));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInstantiatedCoach() {
    String coachId = "lib-coach-new";
    Answers answers = PowerMockito.mock(Answers.class);
    Questionnaire coach = PowerMockito.mock(Questionnaire.class);
    Mockito.when(coach.getId()).thenReturn(coachId);
    PowerMockito.when(CacheFactory.createAnswersFromCoach(coach)).thenReturn(answers);
    try {
      Path file = Paths.get(coachId, CompanyCache.DEFAULT_ANSWERS_XML);
      CompanyCache cache = new CompanyCache(compDir);

      cache.instantiateCoach(null, coach, null);

      Mockito.verify(answersMapper, Mockito.times(1)).init(compDir.resolve(file), answers);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInstantiateSubCoach() {
    String parentId = "lib-coach";
    String coachId = "lib-coach-new";
    Path parentCoach = Paths.get(parentId);
    Path newCoach = parentCoach.resolve(coachId);
    Set<String> names = new HashSet<>(Arrays.asList("A", "B"));

    try {
      Answers answers = Mockito.mock(Answers.class);
      Questionnaire coach = Mockito.mock(Questionnaire.class);
      Mockito.when(coach.getId()).thenReturn(coachId);
      PowerMockito.when(CacheFactory.createAnswersFromCoach(coach)).thenReturn(answers);

      CompanyCache cache = new CompanyCache(compDir);
      cache.instantiateCoach(parentCoach, coach, names);

      Mockito.verify(answersMapper, Mockito.times(1))
            .init(compDir.resolve(newCoach).resolve("A.xml"), answers);
      Mockito.verify(answersMapper, Mockito.times(1))
            .init(compDir.resolve(newCoach).resolve("B.xml"), answers);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testIsCoachInstantiated() {
    Path file = Paths.get("lib-coach/default.xml");
    CompanyCache cache = new CompanyCache(compDir);
    Assert.assertTrue(cache.isCoachInstantiated(file));
  }

  @Test
  public void testIsCoachInstantiatedInvalid() {
    Path file = Paths.get("lib-coach/A.xml");
    CompanyCache cache = new CompanyCache(compDir);
    Assert.assertFalse(cache.isCoachInstantiated(file));
  }

  @Test
  public void testListFiles() {
    List<String> names = Arrays.asList(
          "lib-coach/default.xml",
          "lib-coach/lib-coach-sub/A.xml",
          "lib-coach/lib-coach-sub/B.xml"
    );
    CompanyCache cache = new CompanyCache(compDir);
    try {
      List<Path> paths = cache.listInstantiatedCoaches();

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
