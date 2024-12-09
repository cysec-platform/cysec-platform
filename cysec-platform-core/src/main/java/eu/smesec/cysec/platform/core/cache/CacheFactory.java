/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.execptions.LibraryException;
import eu.smesec.cysec.platform.bridge.generated.Answers;
import eu.smesec.cysec.platform.bridge.generated.FlaggedQuestions;
import eu.smesec.cysec.platform.bridge.generated.Library;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.generated.QuestionnaireReference;

import java.nio.file.Path;
import java.util.Base64;

public final class CacheFactory {
  private CacheFactory() {}

  private static LibraryClassLoader classLoader = new LibraryClassLoader(Thread.currentThread().getContextClassLoader());

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
   * Creates a flagged questions list with a reference to the coach.
   *
   * @param coach coach object
   * @return flagged questions object
   */
  public static FlaggedQuestions createFlaggedQuestionsFromCoach(Questionnaire coach) {
    FlaggedQuestions flaggedQuestions = new FlaggedQuestions();
    QuestionnaireReference reference = new QuestionnaireReference();
    reference.setQuestionnaireId(coach.getId());
    reference.setFilename(coach.getFilename());
    flaggedQuestions.setQuestionnaireReference(reference);
    return flaggedQuestions;
  }

  /**
   * Loads an encoded library.
   *
   * @param parent parent ClassLoader
   * @param lib encoded library
   * @return concrete library
   * @throws LibraryException if an error occurs during loading or instantiating the library
   */
  static CoachLibrary loadLibrary(ClassLoader parent, Library lib)
      throws LibraryException {
    try {
      byte[] decoded = Base64.getDecoder().decode(lib.getValue());
      classLoader.loadJar(decoded);
      // The id of the library must be the FQCN of the class
      Class<?> libraryClass = classLoader.findClass(lib.getId());
      // Instantiate the class and give it a reference to the questionnaire
      return (CoachLibrary) libraryClass.newInstance();
    } catch (Exception e) {
      throw new LibraryException(lib, e.getMessage());
    }
  }
}
