package eu.smesec.platform.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import eu.smesec.bridge.ILibCal;
import eu.smesec.bridge.Library;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.generated.*;
import eu.smesec.platform.threading.ThreadFactory;
import eu.smesec.platform.utils.FileUtils;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheFactory.class, ThreadFactory.class})
public class CoachCacheTest {
  private Path source;
  private Path temp;
  private final ILibCal iLibCal;
  private final Mapper<Questionnaire> mapper;

  @Rule
  public TestName name = new TestName();

  @SuppressWarnings("unchecked")
  public CoachCacheTest() {
    this.source = Paths.get("src/test/resources/coaches");
    this.iLibCal = PowerMockito.mock(ILibCal.class);
    this.mapper = PowerMockito.mock(Mapper.class);

    try {
      PowerMockito.mockStatic(CacheFactory.class);

      PowerMockito.when(CacheFactory.createMapper(Questionnaire.class)).thenReturn(mapper);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/CoachCache/" + name.getMethodName());

    try {
      FileUtils.copyDir(source, temp);
      Mockito.reset(iLibCal, mapper);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  private Option createTestOption(String id, String text, String comment) {
    Option option = new Option();
    option.setId(id);
    option.setText(text);
    option.setComment(comment);
    return option;
  }

  private Option createTestOption(String id, String text) {
    return createTestOption(id, text, null);
  }

  private Question createTestQuestion(String id, String type, String text, String readMore, Option... options) {
    Question question = new Question();
    question.setId(id);
    question.setType(type);
    question.setText(text);
    question.setReadMore(readMore);
    Options options1 = new Options();
    options1.getOption().addAll(Arrays.asList(options));
    question.setOptions(options1);
    return question;
  }

  private Question createTestQuestion(String id, String type, String text, Option... options) {
    return createTestQuestion(id, type, text, null, options);
  }

  private eu.smesec.bridge.generated.Library createTestLib(String id) {
    eu.smesec.bridge.generated.Library lib = new eu.smesec.bridge.generated.Library();
    lib.setId(id);
    return lib;
  }

  private Questionnaire createTestCoach(String id, String parent, String name, String className, Locale locale,
                                        int order, eu.smesec.bridge.generated.Library lib, Question... questions) {
    Questionnaire coach = new Questionnaire();
    List<eu.smesec.bridge.generated.Library> libraries = coach.getLibrary();
    if (lib != null) {
      libraries.add(lib);
    }
    coach.setId(id);
    coach.setParent(parent);
    coach.setReadableName(name);
    coach.setReadableClass(className);
    coach.setOrder(order);
    coach.setLanguage(locale != null ? locale.getLanguage().toLowerCase() : null);
    Questions questions1 = new Questions();
    questions1.getQuestion().addAll(Arrays.asList(questions));
    coach.setQuestions(questions1);
    return coach;
  }

  private Questionnaire createTestCoach(String id, String parent, String name, String className,
                                        int order, eu.smesec.bridge.generated.Library lib, Question... questions) {
    return createTestCoach(id, parent, name, className, null, order, lib, questions);
  }

  @Test
  public void testInstall() {
    Path path = temp.resolve("test-coaches");
    try {
      Assert.assertFalse(Files.exists(path));
      new CoachCache(path, iLibCal);
      Assert.assertTrue(Files.exists(path));
      Assert.assertTrue(Files.isDirectory(path));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testExists() {
    String companyId = "lib-company";
    Path companyFile = Paths.get("lib-company/lib-company.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, null, companyFile);

      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testNotExists() {
    String otherId = "lib-other";
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);

      Assert.assertFalse(cache.existsCoach(otherId, null));
      Assert.assertFalse(cache.existsCoach(otherId, Locale.ENGLISH));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAddTranslation() {
    String companyId = "lib-company";
    Path companyFileEn = Paths.get("lib-company/lib-company.xml");
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, Locale.ENGLISH.getLanguage(), companyFileEn);
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);

      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.FRENCH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.GERMAN));

      cache.removeCoach(companyFileFr);

      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.GERMAN));

      cache.removeCoach(companyFileEn);

      Assert.assertFalse(cache.existsCoach(companyId, null));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.GERMAN));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAddTranslationFirst() {
    String companyId = "lib-company";
    Path companyFileEn = Paths.get("lib-company/lib-company.xml");
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);
      // add french language first
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);
      cache.addCoach(companyId, null, Locale.ENGLISH.getLanguage(), companyFileEn);

      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.FRENCH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.GERMAN));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAddTranslationOnly() {
    String companyId = "lib-company";
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);
      // add french language first
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);

      Assert.assertFalse(cache.existsCoach(companyId, null));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.FRENCH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.GERMAN));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAddSubCoach() {
    String companyId = "lib-company";
    String parentId = "lib-parent";
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, parentId, null, companyFileFr);

      Assert.assertFalse(cache.existsCoach(parentId, null));
      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAddCoachWeiredName() {
    String companyId = "lib-company";
    Path path = Paths.get("weired.xml");
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      CoachCache cache = new CoachCache(temp, iLibCal);

      cache.addCoach(companyId, null, null, path);

      Assert.assertTrue(cache.existsCoach(companyId, null));
      Assert.assertTrue(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));

      cache.removeCoach(path);

      Assert.assertFalse(cache.existsCoach(companyId, null));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.ENGLISH));
      Assert.assertFalse(cache.existsCoach(companyId, Locale.FRENCH));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadDefaultCoach() {
    String companyId = "lib-company";
    String libId = "first lib";
    eu.smesec.bridge.generated.Library libGen = createTestLib(libId);
    Path companyFileEn = Paths.get("lib-company/lib-company.xml");
    Questionnaire enCoach = createTestCoach("lib-company", null,
        "Company", "CySec", 1, libGen,
        createTestQuestion("company-q10", "A", "Question 1",
            createTestOption("company-q10o1", "Option 1"),
            createTestOption("company-q10o2", "Option 2")
        ), createTestQuestion("company-q20", "A", "Question 2",
            createTestOption("company-q20o1", "Option 1"),
            createTestOption("company-q20o2", "Option 2")
        ), createTestQuestion("company-q30", "A", "Question 3",
            createTestOption("company-q30o1", "Option 1"),
            createTestOption("company-q30o2", "Option 2")
        ));
    Questionnaire result = new Questionnaire();
    enCoach.copyTo(result);
    result.getLibrary().clear();
    try {
      Library library = Mockito.mock(Library.class);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.any(ClassLoader.class), Mockito.eq(libGen)))
          .thenReturn(library);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileEn))).thenReturn(enCoach);

      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, null, companyFileEn);
      cache.read(companyId, null, coachLib -> {
        Questionnaire coach = coachLib.getFirst();
        Assert.assertNotSame(result, coach);
        Assert.assertEquals(result, coach);
        Assert.assertSame(1, coachLib.getSecond().size());
        Assert.assertSame(library, coachLib.getSecond().get(0));
        Mockito.verify(library, Mockito.times(1))
            .init(Mockito.eq(libId), Mockito.eq(enCoach), Mockito.eq(iLibCal), Mockito.any());
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadFrenchCoachFullyConnected() {
    String companyId = "lib-company";
    String libId = "first lib";
    eu.smesec.bridge.generated.Library libGen = createTestLib(libId);
    Path companyFileEn = Paths.get("lib-company/lib-company.xml");
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    Questionnaire enCoach = createTestCoach(companyId, null,
        "Company", "CySec", 1, libGen,
        createTestQuestion("company-q10", "A", "English question 1",
            createTestOption("company-q10o1", "English option 1"),
            createTestOption("company-q10o2", "English option 2")
        ), createTestQuestion("company-q20", "A", "English question 2",
            createTestOption("company-q20o1", "English option 1"),
            createTestOption("company-q20o2", "English option 2")
        ), createTestQuestion("company-q30", "A", "English question 3",
            createTestOption("company-q30o1", "English option 1"),
            createTestOption("company-q30o2", "English option 2")
        ));
    Questionnaire frCoach = createTestCoach(companyId, null,
        "Entreprise", "CySec", Locale.FRENCH, 0, null,
        createTestQuestion("company-q10", null, "French question 1",
            createTestOption("company-q10o1", "French option 1"),
            createTestOption("company-q10o2", "French option 2")
        ), createTestQuestion("company-q20", null, "French question 2",
            createTestOption("company-q20o1", "French option 1"),
            createTestOption("company-q20o2", "French option 2")
        ), createTestQuestion("company-q30", null, "French question 3",
            createTestOption("company-q30o1", "French option 1"),
            createTestOption("company-q30o2", "French option 2")
        ));
    Questionnaire result = createTestCoach(companyId, null,
        "Entreprise", "CySec", 1, null,
        createTestQuestion("company-q10", "A", "French question 1",
            createTestOption("company-q10o1", "French option 1"),
            createTestOption("company-q10o2", "French option 2")
        ), createTestQuestion("company-q20", "A", "French question 2",
            createTestOption("company-q20o1", "French option 1"),
            createTestOption("company-q20o2", "French option 2")
        ), createTestQuestion("company-q30", "A", "French question 3",
            createTestOption("company-q30o1", "French option 1"),
            createTestOption("company-q30o2", "French option 2")
        ));
    try {
      Library library = Mockito.mock(Library.class);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.any(ClassLoader.class), Mockito.eq(libGen)))
          .thenReturn(library);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileEn))).thenReturn(enCoach);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileFr))).thenReturn(frCoach);
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, Locale.ENGLISH.getLanguage(), companyFileEn);
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);
      cache.read(companyId, Locale.FRENCH, coachLib -> {
        Questionnaire coach = coachLib.getFirst();
        Assert.assertEquals(result, coach);
        Assert.assertSame(1, coachLib.getSecond().size());
        Assert.assertSame(library, coachLib.getSecond().get(0));
        Mockito.verify(library, Mockito.times(1))
            .init(Mockito.eq(libId), Mockito.eq(enCoach), Mockito.eq(iLibCal), Mockito.any());
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadFrenchCoachDifferentIds() {
    String companyId = "lib-company";
    String libId = "first lib";
    eu.smesec.bridge.generated.Library libGen = createTestLib(libId);
    Path companyFileEn = Paths.get("lib-company/lib-company.xml");
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    Questionnaire enCoach = createTestCoach(companyId, null,
        "Company", "CySec", 1, libGen,
        createTestQuestion("company-q10", "A", "English question 1",
            createTestOption("company-q10o1", "English option 1"),
            createTestOption("company-q10o2", "English option 2")
        ), createTestQuestion("company-q20", "A", "English question 2",
            createTestOption("company-q20o1", "English option 1"),
            createTestOption("company-q20o2", "English option 2"),
            createTestOption("company-q20o3", "English option 3")
        ), createTestQuestion("company-q40", "A", "English question 4",
            createTestOption("company-q40o1", "English option 1"),
            createTestOption("company-q40o2", "English option 2")
        ));
    Questionnaire frCoach = createTestCoach(companyId, null,
        "Entreprise", "CySec", Locale.FRENCH, 0, null,
        createTestQuestion("company-q10", null, "French question 1",
            createTestOption("company-q10o1", "French option 1"),
            createTestOption("company-q10o2", "French option 2")
        ), createTestQuestion("company-q20", null, "French question 2",
            createTestOption("company-q20o1", "French option 1"),
            createTestOption("company-q20o2", "French option 2"),
            createTestOption("company-q20o4", "French option 4")
        ), createTestQuestion("company-q30", null, "French question 3",
            createTestOption("company-q30o1", "French option 1"),
            createTestOption("company-q30o2", "French option 2")
        ));
    Questionnaire result = createTestCoach(companyId, null,
        "Entreprise", "CySec", 1, null,
        createTestQuestion("company-q10", "A", "French question 1",
            createTestOption("company-q10o1", "French option 1"),
            createTestOption("company-q10o2", "French option 2")
        ), createTestQuestion("company-q20", "A", "French question 2",
            createTestOption("company-q20o1", "French option 1"),
            createTestOption("company-q20o2", "French option 2"),
            createTestOption("company-q20o3", "English option 3")
        ), createTestQuestion("company-q40", "A", "English question 4",
            createTestOption("company-q40o1", "English option 1"),
            createTestOption("company-q40o2", "English option 2")
        ));
    try {
      Library library = Mockito.mock(Library.class);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.any(ClassLoader.class), Mockito.eq(libGen)))
          .thenReturn(library);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileEn))).thenReturn(enCoach);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileFr))).thenReturn(frCoach);
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, Locale.ENGLISH.getLanguage(), companyFileEn);
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);
      cache.read(companyId, Locale.FRENCH, coachLib -> {
        Questionnaire coach = coachLib.getFirst();
        Assert.assertEquals(result, coach);
        Assert.assertSame(1, coachLib.getSecond().size());
        Assert.assertSame(library, coachLib.getSecond().get(0));
        Mockito.verify(library, Mockito.times(1))
            .init(Mockito.eq(libId), Mockito.eq(enCoach), Mockito.eq(iLibCal), Mockito.any());
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadFrenchCoachNonDefault() {
    String companyId = "lib-company";
    Path companyFileFr = Paths.get("lib-company/lib-company_fr.xml");
    Questionnaire frCoach = createTestCoach(companyId, null,
        "Entreprise", "CySec", Locale.FRENCH, 0, null,
        createTestQuestion("company-q10", null, "French question 1",
            createTestOption("company-q10o1", "French option 1"),
            createTestOption("company-q10o2", "French option 2")));
    try {
      Mockito.when(mapper.unmarshal(temp.resolve(companyFileFr))).thenReturn(frCoach);
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, Locale.FRENCH.getLanguage(), companyFileFr);
      cache.read(companyId, Locale.FRENCH, coachLib -> {
        Assert.fail();
        return null;
      });
    } catch (CacheNotFoundException nfe) {
      Assert.assertEquals("Default coach lib-company was not detected.", nfe.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadDefaultSubCoach() {
    String companyId = "lib-company";
    String subId = "lib-demo-sub";
    String libId = "first lib";
    String libSubId = "second lib";
    eu.smesec.bridge.generated.Library libGen = createTestLib(libId);
    eu.smesec.bridge.generated.Library libSubGen = createTestLib(libSubId);
    Path companyFile = Paths.get("lib-company/lib-company.xml");
    Path subFile = Paths.get("lib-company/lib-demo-sub/lib-demo-sub.xml");
    Questionnaire coach = createTestCoach(companyId, null,
        "Company", "CySec", 1, libGen);
    Questionnaire subCoach = createTestCoach(subId, companyId,
        "Demo", "CySec", 1, libSubGen,
        createTestQuestion("company-q10", "A", "English question 1",
            createTestOption("company-q10o1", "English option 1"),
            createTestOption("company-q10o2", "English option 2")
        ), createTestQuestion("company-q20", "A", "English question 2",
            createTestOption("company-q20o1", "English option 1"),
            createTestOption("company-q20o2", "English option 2"),
            createTestOption("company-q20o3", "English option 3")
        ), createTestQuestion("company-q40", "A", "English question 4",
            createTestOption("company-q40o1", "English option 1"),
            createTestOption("company-q40o2", "English option 2")
        ));
    Questionnaire result = new Questionnaire();
    subCoach.copyTo(result);
    result.getLibrary().clear();
    try {
      Library library = Mockito.mock(Library.class);
      Library subLibrary = Mockito.mock(Library.class);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.any(ClassLoader.class), Mockito.eq(libGen)))
          .thenReturn(library);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.eq(library.getClass().getClassLoader()),
          Mockito.eq(libSubGen))).thenReturn(subLibrary);
      Mockito.when(mapper.unmarshal(temp.resolve(companyFile))).thenReturn(coach);
      Mockito.when(mapper.unmarshal(temp.resolve(subFile))).thenReturn(subCoach);
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(companyId, null, null, companyFile);
      cache.addCoach(subId, companyId, null, subFile);
      cache.read(subId, Locale.ENGLISH, coachLib -> {
        Questionnaire coach1 = coachLib.getFirst();
        Assert.assertEquals(result, coach1);
        Assert.assertSame(1, coachLib.getSecond().size());
        Assert.assertSame(subLibrary, coachLib.getSecond().get(0));
        Mockito.verify(library, Mockito.times(1))
            .init(Mockito.eq(libId), Mockito.eq(coach), Mockito.eq(iLibCal), Mockito.any());
        Mockito.verify(subLibrary, Mockito.times(1))
            .init(Mockito.eq(libSubId), Mockito.eq(subCoach), Mockito.eq(iLibCal), Mockito.any());
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testReadSubCoachNoParent() {
    String companyId = "lib-company";
    String subId = "lib-demo-sub";
    String libSubId = "second lib";
    eu.smesec.bridge.generated.Library libSubGen = createTestLib(libSubId);
    Path subFile = Paths.get("lib-company/lib-demo-sub/lib-demo-sub.xml");
    Questionnaire subCoach = createTestCoach(subId, companyId,
        "Demo", "CySec", 1, libSubGen,
        createTestQuestion("company-q10", "A", "English question 1",
            createTestOption("company-q10o1", "English option 1"),
            createTestOption("company-q10o2", "English option 2")
        ), createTestQuestion("company-q20", "A", "English question 2",
            createTestOption("company-q20o1", "English option 1"),
            createTestOption("company-q20o2", "English option 2"),
            createTestOption("company-q20o3", "English option 3")
        ), createTestQuestion("company-q40", "A", "English question 4",
            createTestOption("company-q40o1", "English option 1"),
            createTestOption("company-q40o2", "English option 2")
        ));
    try {
      Library subLibrary = Mockito.mock(Library.class);
      PowerMockito.when(CacheFactory.loadLibrary(Mockito.any(ClassLoader.class),
          Mockito.eq(libSubGen))).thenReturn(subLibrary);
      Mockito.when(mapper.unmarshal(temp.resolve(subFile))).thenReturn(subCoach);
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));

      CoachCache cache = new CoachCache(temp, iLibCal);
      cache.addCoach(subId, companyId, null, subFile);
      cache.read(subId, Locale.ENGLISH, coachLib -> {
        Assert.fail();
        return null;
      });
    } catch (CacheNotFoundException nfe) {
      Assert.assertEquals("Default coach lib-company was not detected.", nfe.getMessage());
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
