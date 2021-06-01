package eu.smesec.core.cache;

import eu.smesec.bridge.Library;
import eu.smesec.bridge.execptions.LibraryException;
import eu.smesec.bridge.generated.Answers;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.generated.QuestionnaireReference;

import java.nio.file.Path;
import java.util.Base64;

public final class CacheFactory {
  private CacheFactory() {}

  /**
   * Creates a mapper for a generated jaxb2 class.
   *
   * @param classOfT generated jaxb2 class
   * @param <T> class of generated class
   * @return Mapper object
   */
  public static <T> Mapper<T> createMapper(Class<T> classOfT) {
    return new Mapper<>(classOfT);
  }

  /**
   * Creates a company coach.
   *
   * @param path company directory
   * @return company cache object
   */
  public static CompanyCache createCompanyCache(Path path) {
    return new CompanyCache(path);
  }

  /**
   * Creates an answers list with a reference to the coach.
   *
   * @param coach coach object
   * @return answers object
   */
  public static Answers createAnswersFromCoach(Questionnaire coach) {
    Answers answers = new Answers();
    QuestionnaireReference reference = new QuestionnaireReference();
    reference.setQuestionnaireId(coach.getId());
    reference.setFilename(coach.getFilename());
    answers.setQuestionnaireReference(reference);
    return answers;
  }

  /**
   * Loads an encoded library.
   *
   * @param parent parent ClassLoader
   * @param lib encoded library
   * @return concrete library
   * @throws LibraryException if an error occurs during loading or instantiating the library
   */
  static Library loadLibrary(ClassLoader parent, eu.smesec.bridge.generated.Library lib)
      throws LibraryException {
    try {
      byte[] decoded = Base64.getDecoder().decode(lib.getValue());
      LibraryClassLoader loader = new LibraryClassLoader(parent, decoded);
      // The id of the library must be the FQCN of the class
      Class<?> libraryClass = loader.findClass(lib.getId());
      // Instantiate the class and give it a reference to the questionnaire
      return (Library) libraryClass.newInstance();
    } catch (Exception e) {
      throw new LibraryException(lib, e.getMessage());
    }
  }
}
