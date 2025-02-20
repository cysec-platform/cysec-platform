/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.execptions.CacheAlreadyExistsException;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.CacheNotFoundException;
import eu.smesec.cysec.platform.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.cysec.platform.bridge.execptions.ElementNotFoundException;
import eu.smesec.cysec.platform.bridge.execptions.TokenExpiredException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Answers;
import eu.smesec.cysec.platform.bridge.generated.Audit;
import eu.smesec.cysec.platform.bridge.generated.Audits;
import eu.smesec.cysec.platform.bridge.generated.Company;
import eu.smesec.cysec.platform.bridge.generated.FlaggedQuestion;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.generated.Token;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.md.State;
import eu.smesec.cysec.platform.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.helpers.subcoach.SubcoachHelper;
import eu.smesec.cysec.platform.core.utils.FileResponse;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.xml.datatype.XMLGregorianCalendar;

/** Cache abstraction layer. Provides cache API. */
@Singleton
public class CacheAbstractionLayer {
  private DataCache data;
  private CoachCache coaches;

  /**
   * CAL constructor.
   *
   * @param context servlet context
   * @param config app config
   */
  public CacheAbstractionLayer(
      @Context ServletContext context, ResourceManager resManager, Config config) {
    try {
      String contextName = context.getContextPath().substring(1);
      String basePath = config.getStringValue(contextName, "cysec_base_path");
      String dataPath = config.getStringValue(contextName, "cysec_data_path");
      String coachPath = config.getStringValue(contextName, "cysec_coach_path");
      this.data =
          new DataCache(
              dataPath.startsWith("/")
                  ? Paths.get(dataPath)
                  : Paths.get(basePath, dataPath, context.getContextPath()));
      this.coaches =
          new CoachCache(
              coachPath.startsWith("/") ? Paths.get(coachPath) : Paths.get(basePath, coachPath),
              new LibCal(this, context, resManager));
      CoachManager coachManager = new CoachManager(this.coaches);
      coachManager.init(); // check call on webapp start and not in constructor
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // -------------------
  // ----- COMPANY -----
  // -------------------

  /**
   * Returns all loaded company ids.
   *
   * @return Set of all companyIds.
   */
  public Collection<String> getCompanyIds() {
    return data.getCompanyIds();
  }

  /**
   * Returns a list of loaded companies.
   *
   * @return company list
   * @throws CacheException If an error occurs during accessing the companies
   */
  public List<Company> getCompanies() throws CacheException {
    return data.executeOnAllCompanies(
        entries -> {
          List<Company> companies = new ArrayList<>();
          for (Map.Entry<String, CompanyCache> entry : entries) {
            entry.getValue().readOnUsers(companies::add);
          }
          return companies;
        });
  }

  /**
   * Checks if a company is loaded.
   *
   * @param companyId The id of the company.
   * @return <code>true</code> if the company is loaded, or <code>false</code> otherwise.
   */
  public boolean existsCompany(String companyId) {
    return data.existsCompany(companyId.toLowerCase());
  }

  /**
   * Find the company for which the email is registered.
   *
   * @param email The email address of the user.
   * @return An optional of Company that has to be handled by the caller of the method.
   * @throws CacheException on any cache error
   */
  public String getCompanyByEmail(String email) throws CacheException {
    return data.executeOnAllCompanies(
        entries -> {
          List<String> result = new ArrayList<>();
          for (Map.Entry<String, CompanyCache> entry : entries) {
            if (entry
                .getValue()
                .readOnUsers(
                    company ->
                        company.getUser().stream()
                            .anyMatch(user -> user.getEmail().equals(email)))) {
              result.add(entry.getKey());
            }
          }
          return result.size() > 0 ? result.get(0) : null;
        });
  }

  /**
   * Creates a new company.
   *
   * @param companyId The id of the new company.
   * @param companyName The name of the new company.
   * @param admin The companies first user who should be admin.
   * @throws CacheException If the a general error occurs.
   * @throws ElementNotFoundException If the coach company was not found.
   * @throws CacheAlreadyExistsException If the company already exists.
   * @throws CacheException on any cache error
   */
  public void createCompany(String companyId, String companyName, User admin)
      throws CacheException {
    Questionnaire companyCoach = getCoach("lib-company");
    if (companyCoach == null) {
      throw new ElementNotFoundException("coach \"lib-company\" was not found");
    }
    admin.setId(1000L);
    data.addCompany(companyId.toLowerCase(), companyName, admin, companyCoach);
  }

  //    /**
  //     * <p>Removes an existing company.</p>
  //     *
  //     * @param companyId The id of the company to remove.
  //     * @throws CacheException         If the a general error occurs.
  //     * @throws CacheNotFoundException If the company was not found.
  //     */
  //    public void removeCompany(String companyId) throws CacheException {
  //        data.deleteCompany(companyId.toLowerCase());
  //    }

  // ----------------
  // ----- USER -----
  // ----------------

  private <R> R readOnUsers(String companyId, ICommand<Company, R> command) throws CacheException {
    return data.executeOnCompany(companyId, cache -> cache.readOnUsers(command));
  }

  private void writeOnUser(String companyId, ICommand<Company, Void> command)
      throws CacheException {
    data.executeOnCompany(
        companyId,
        cache -> {
          cache.writeOnUsers(command);
          return null;
        });
  }

  /**
   * <p>Gets the company.</p>
   *
   * @param companyId the company id to be looked up
   * @return the company object
   * @throws CacheException on any cache error
   */
  public Company getCompany(String companyId) throws CacheException {
    return readOnUsers(companyId, company -> company);
  }

  public String getCompanyName(String companyId) throws CacheException {
    return readOnUsers(companyId, Company::getCompanyname);
  }

  public String getCompanyReplicaToken(String companyId) throws CacheException {
    return data.executeOnCompany(companyId, CompanyCache::getReplicaToken);
  }

  /**
   * Creates a new user.
   *
   * @param companyId The id of the company.
   * @param user The new user.
   * @throws CacheNotFoundException If the company was not found.
   * @throws ElementAlreadyExistsException If the username or email are already in use.
   */
  public void createUser(String companyId, User user) throws CacheException {
    data.executeOnCompany(
        companyId,
        companyCache -> {
          companyCache.writeOnUsers(
              company -> {
                String username = user.getUsername();
                String email = user.getEmail();
                if (company.getUser().stream()
                    .anyMatch(
                        user1 ->
                            user1.getUsername().equals(username)
                                || user1.getEmail().equals(email))) {
                  throw new ElementAlreadyExistsException(
                      "User with username "
                          + username
                          + ", or email "
                          + email
                          + " already exists in company "
                          + companyId);
                }
                user.setId(companyCache.nextUserId());
                company.getUser().add((User) user.clone());
                return null;
              });
          return null;
        });
  }

  /**
   * Returns an user by its id.
   *
   * @param companyId The id of the company.
   * @param userId The id of the user.
   * @return <code>User</code> if an user was found, <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   */
  public User getUser(String companyId, long userId) throws CacheException {
    return readOnUsers(
        companyId,
        company -> {
          ListIterator<User> it = company.getUser().listIterator();
          while (it.hasNext()) {
            User user = it.next();
            if (user.getId() == userId) {
              return user;
            }
          }
          return null;
        });
  }

  /**
   * Returns an user by its name.
   *
   * @param companyId The id of the company.
   * @param username The name of the user.
   * @return <code>User</code> if an user was found, <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   */
  public User getUserByName(String companyId, String username) throws CacheException {
    return readOnUsers(
        companyId,
        company -> {
          ListIterator<User> it = company.getUser().listIterator();
          while (it.hasNext()) {
            User user = it.next();
            if (user.getUsername().equals(username)) {
              return user;
            }
          }
          return null;
        });
  }

  /**
   * Returns an user by its email.
   *
   * @param companyId The id of the company.
   * @param email The email of the user.
   * @return <code>User</code> if an user was found, <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   */
  public User getUserByEmail(String companyId, String email) throws CacheException {
    return readOnUsers(
        companyId,
        company -> {
          ListIterator<User> it = company.getUser().listIterator();
          while (it.hasNext()) {
            User user = it.next();
            if (user.getEmail().equals(email)) {
              return user;
            }
          }
          return null;
        });
  }

  /**
   * Returns an user by its email.
   *
   * @param companyId The id of the company.
   * @param token The reset token.
   * @return <code>User</code> if an user was found, <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws TokenExpiredException If the token is expired.
   */
  public User getUserByToken(String companyId, String token) throws CacheException {
    return readOnUsers(
        companyId,
        company -> {
          ListIterator<User> itUser = company.getUser().listIterator();
          while (itUser.hasNext()) {
            User user = itUser.next();
            ListIterator<Token> itToken = user.getToken().listIterator();
            while (itToken.hasNext()) {
              Token token1 = itToken.next();
              if (token1.getId().equals("reset") && token1.getValue().equals(token)) {
                XMLGregorianCalendar tokenExpiry = token1.getExpiry();
                LocalDate expiry =
                    LocalDate.of(
                        tokenExpiry.getYear(), tokenExpiry.getMonth(), tokenExpiry.getDay());
                if (LocalDate.now().isAfter(expiry)) {
                  throw new TokenExpiredException(token1);
                }
                return user;
              }
            }
          }
          return null;
        });
  }

  /**
   * Returns all users of a company.
   *
   * @param companyId The id of the company.
   * @return List of all users.
   * @throws CacheNotFoundException If the company was not found.
   */
  public List<User> getAllUsers(String companyId) throws CacheException {
    return readOnUsers(companyId, Company::getUser);
  }

  /**
   * Returns a list of all users within the given company that bear the Admin role.
   *
   * @param companyId The company to query.
   * @return A list of Users.
   * @throws CacheNotFoundException If the company was not found.
   */
  public List<User> getAllAdminUsers(String companyId) throws CacheException {
    return readOnUsers(
        companyId,
        company ->
            company.getUser().stream()
                .filter(user -> user.getRole().contains("Admin"))
                .collect(Collectors.toList()));
  }

  /**
   * Updates an user. The id of this user must be unmodified.
   *
   * @param companyId The id of the company.
   * @param user The modified user.
   * @throws CacheNotFoundException If the company was not found.
   * @throws ElementAlreadyExistsException If the username or email are already in use.
   * @throws ElementNotFoundException If the user was not found.
   */
  public void updateUser(String companyId, User user) throws CacheException {
    writeOnUser(
        companyId,
        company -> {
          ListIterator<User> it = company.getUser().listIterator();
          int i = -1;
          while (it.hasNext()) {
            User user1 = it.next();
            if (user1.getId().equals(user.getId())) {
              i = it.nextIndex();
              if (user.getPassword() == null) {
                user.setPassword(user1.getPassword());
              }
            } else if (user1.getUsername().equals(user.getUsername())
                || user1.getEmail().equals(user.getEmail())) {
              throw new ElementAlreadyExistsException(
                  "User with username "
                      + user.getUsername()
                      + " or email "
                      + user.getEmail()
                      + " already exists in company "
                      + companyId);
            }
          }
          if (i >= 0) {
            // -1 since list is 0-based
            company.getUser().set(i - 1, (User) user.clone());
            return null;
          }
          throw new ElementNotFoundException(
              "User " + user.getId() + " not found in company " + companyId);
        });
  }

  /**
   * Removes an user.
   *
   * @param companyId The id of the company.
   * @param userId The id of the user.
   * @throws CacheNotFoundException If the company was not found.
   * @throws ElementNotFoundException If the user was not found.
   */
  public void removeUser(String companyId, long userId) throws CacheException {
    writeOnUser(
        companyId,
        company -> {
          if (!company.getUser().removeIf(user -> user.getId() == userId)) {
            throw new ElementNotFoundException(
                "User " + userId + " not found in company " + companyId);
          }
          return null;
        });
  }

  // ------------------
  // ----- ANSWER -----
  // ------------------

  private <R> R readOnAllAnswers(String companyId, ICommand<Map<Path, Answers>, R> command)
      throws CacheException {
    return data.executeOnCompany(companyId, company -> company.readOnAllAnswers(command));
  }

  private <R> R readOnAnswers(String companyId, FQCN fqcn, ICommand<Answers, R> command)
      throws CacheException {
    return data.executeOnCompany(
        companyId, company -> company.readOnAnswers(fqcn.toPath(), command));
  }

  private void writeOnAnswers(String companyId, FQCN fqcn, ICommand<Answers, Void> command)
      throws CacheException {
    data.executeOnCompany(
        companyId,
        company -> {
          company.writeOnAnswers(fqcn.toPath(), command);
          return null;
        });
  }

  /**
   * Appends an answer object.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param answer The new answer object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementAlreadyExistsException If answer qid is already in use.
   */
  public void createAnswer(String companyId, FQCN fqcn, Answer answer) throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          if (answers.getAnswer().stream()
              .anyMatch(answer1 -> answer1.getQid().equals(answer.getQid()))) {
            throw new ElementAlreadyExistsException(
                "Answer "
                    + answer.getQid().toString()
                    + " already exists for "
                    + fqcn
                    + " in company "
                    + companyId);
          }
          answers.getAnswer().add((Answer) answer.clone());
          return null;
        });
  }

  /**
   * Old answer check, this method will be removed in a later release.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name.
   * @param qid The id of an answer.
   * @return <code>true</code> if the company exists, or <code>false</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  @Deprecated
  public boolean existsAnswer(String companyId, FQCN fqcn, Object qid) throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> answers.getAnswer().stream().anyMatch(answer -> answer.getQid().equals(qid)));
  }

  /**
   * Returns an answer object.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name.
   * @param qid The id of an answer.
   * @return Answer object if the answer was found, or <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public Answer getAnswer(String companyId, FQCN fqcn, Object qid) throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Answer> it = answers.getAnswer().listIterator();
          while (it.hasNext()) {
            Answer answer = it.next();
            if (answer.getQid().equals(qid)) {
              return answer;
            }
          }
          return null;
        });
  }

  /**
   * Returns all answer objects.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @return A list of all answer objects.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public List<Answer> getAllAnswers(String companyId, FQCN fqcn) throws CacheException {
    return readOnAnswers(companyId, fqcn, Answers::getAnswer);
  }

  /**
   * Updates an existing answer object. The qid of the answer must be the same.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param answer The new answer object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the answer was not found.
   */
  public void updateAnswer(String companyId, FQCN fqcn, Answer answer) throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Answer> it = answers.getAnswer().listIterator();
          while (it.hasNext()) {
            Answer answer1 = it.next();
            if (answer1.getQid().equals(answer.getQid())) {
              it.set((Answer) answer.clone());
              return null;
            }
          }
          throw new ElementNotFoundException(
              "Answer "
                  + answer.getQid().toString()
                  + " not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  /**
   * Removes an existing answer object.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param questionId The id of an answer.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the answer was not found.
   */
  public void removeAnswer(String companyId, FQCN fqcn, Object questionId) throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          if (!answers.getAnswer().removeIf(answer -> answer.getQid().equals(questionId))) {
            throw new ElementNotFoundException(
                "Answer "
                    + questionId.toString()
                    + " not found for instance "
                    + fqcn
                    + " in company "
                    + companyId);
          }
          return null;
        });
  }

  // -----------------
  // ----- Flags -----
  // -----------------

  /**
   * Removes the flag for a specific question.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param questionId The id of a question.
   * @param flagged Whether the question should be flagged or unflagged.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public void flagQuestion(String companyId, FQCN fqcn, String questionId, boolean flagged) throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<FlaggedQuestion> it = answers.getFlaggedQuestion().listIterator();
          while (it.hasNext()) {
            FlaggedQuestion q = it.next();
            if (q.getQid().equals(questionId)) {
              if(!flagged) {
                it.remove();
              }
              return null;
            }
          }
          if(flagged) {
            FlaggedQuestion newFlag = new FlaggedQuestion();
            newFlag.setQid(questionId);
            answers.getFlaggedQuestion().add(newFlag);
          }
          return null;
        });
  }

  /**
   * Returns whether a question is flagged.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name.
   * @param questionId The id of the question.
   * @return <code>true</code> if the question was flagged, <code>false</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public boolean isQuestionFlagged(String companyId, FQCN fqcn, Object questionId) throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<FlaggedQuestion> it = answers.getFlaggedQuestion().listIterator();
          while (it.hasNext()) {
            FlaggedQuestion q = it.next();
            if (q.getQid().equals(questionId)) {
              return true;
            }
          }
          return false;
        });
  }


  // ---------------------
  // ----- Metadata  -----
  // ---------------------

  /**
   * Appends a new metadata object. Metadata object should contain some mvalues.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadata The new metadata object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementAlreadyExistsException If metadata key is already in use.
   */
  @Deprecated
  public void createMetadataOnAnswers(String companyId, FQCN fqcn, Metadata metadata)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> it = answers.getMetadata().listIterator();
          while (it.hasNext()) {
            Metadata md = it.next();
            if (md.getKey().equals(metadata.getKey())) {
              throw new ElementAlreadyExistsException(
                  "Metadata key "
                      + metadata.getKey()
                      + " already exists for instance "
                      + fqcn
                      + " in company "
                      + companyId);
            }
          }
          answers.getMetadata().add((Metadata) metadata.clone());
          return null;
        });
  }

  /**
   * Checks if a metadata key exists.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @return <code>true</code> if the metadata exists, or <code>false</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  @Deprecated
  public boolean existsMetadataOnAnswer(String companyId, FQCN fqcn, String metadataKey)
      throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> answers.getMetadata().stream().anyMatch(md -> md.getKey().equals(metadataKey)));
  }

  /**
   * Returns a metadata.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @return the metadata if the key exists, or <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public Metadata getMetadataOnAnswer(String companyId, FQCN fqcn, String metadataKey)
      throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> it = answers.getMetadata().listIterator();
          while (it.hasNext()) {
            Metadata md = it.next();
            if (md.getKey().equals(metadataKey)) {
              return md;
            }
          }
          return null;
        });
  }

  /**
   * Returns all Metadata.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @return A list of all metadata.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public List<Metadata> getAllMetadataOnAnswer(String companyId, FQCN fqcn) throws CacheException {
    return readOnAnswers(companyId, fqcn, Answers::getMetadata);
  }

  /**
   * Returns all Metadata that start with the specified prefix.
   *
   * @param companyId The id of the company.
   * @param fqcn The instance of the coach.
   * @param prefix The prefix that the metadata have to match
   * @return A list of all metadata.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public List<Metadata> getAllMetadataOnAnswer(String companyId, FQCN fqcn, String prefix)
      throws CacheException {
    if (prefix == null) {
      return getAllMetadataOnAnswer(companyId, fqcn);
    }
    return readOnAnswers(
        companyId,
        fqcn,
        answers ->
            answers.getMetadata().stream()
                .filter(md -> md.getKey().startsWith(prefix))
                .collect(Collectors.toList()));
  }

  /**
   * Updates an existing metadata object. The key of the metadata must be the same.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadata The new metadata object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   */
  @Deprecated
  public void updateMetadataOnAnswer(String companyId, FQCN fqcn, Metadata metadata)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> it = answers.getMetadata().listIterator();
          while (it.hasNext()) {
            Metadata md = it.next();
            if (md.getKey().equals(metadata.getKey())) {
              it.set((Metadata) metadata.clone());
            }
          }
          throw new ElementNotFoundException(
              "Metadata "
                  + metadata.getKey()
                  + " was not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  /**
   * Deletes a metadata object.
   *
   * @param companyId The id of the company.
   * @param fqcn The instance of the coach.
   * @param metadataKey The key of the metadata.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   */
  public void deleteMetadataOnAnswers(String companyId, FQCN fqcn, String metadataKey)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          if (!answers.getMetadata().removeIf(metadata -> metadata.getKey().equals(metadataKey))) {
            throw new ElementNotFoundException(
                "Metadata "
                    + metadataKey
                    + " was not found for instance "
                    + fqcn
                    + " in company "
                    + companyId);
          }
          return null;
        });
  }

  // -------------------
  // ----- Mvalue  -----
  // -------------------

  /**
   * Sets all mvalues in the metadata. If the metadata does not exists, it will be created. If a
   * mvalue exists, it will be updated, or added otherwise. If a mvalue key exists more than once in
   * the list, then the last one will be used.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadata The metadata object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public void setMetadataOnAnswers(String companyId, FQCN fqcn, Metadata metadata)
      throws CacheException {
    Map<String, Mvalue> mvs = new HashMap<>(metadata.getMvalue().size());
    for (Mvalue mv : metadata.getMvalue()) {
      mvs.put(mv.getKey(), (Mvalue) mv.clone());
    }

    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> it = answers.getMetadata().listIterator();
          while (it.hasNext()) {
            Metadata md = it.next();
            if (md.getKey().equals(metadata.getKey())) {
              // update Metadata
              ListIterator<Mvalue> it2 = md.getMvalue().listIterator();
              while (it2.hasNext()) {
                Mvalue mv = it2.next();
                if (mvs.containsKey(mv.getKey())) {
                  it2.set(mvs.remove(mv.getKey()));
                }
              }
              md.getMvalue().addAll(mvs.values());
              return null;
            }
          }
          // create new Metadata
          Metadata md = new Metadata();
          md.setKey(metadata.getKey());
          md.getMvalue().addAll(mvs.values());
          answers.getMetadata().add(md);
          return null;
        });
  }

  /**
   * Removes all mvalues in the metadata object. If the mvalue is not present it will be ignored. If
   * the metadata will be empty, it will be removed.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @param mvalueKeys The keys of the mvalues.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public void removeMvaluesFromAnswer(
      String companyId, FQCN fqcn, String metadataKey, Set<String> mvalueKeys)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> it = answers.getMetadata().listIterator();
          while (it.hasNext()) {
            Metadata md = it.next();
            if (md.getKey().equals(metadataKey)) {
              // update Metadata
              md.getMvalue().removeIf(mv -> mvalueKeys.contains(mv.getKey()));
              if (md.getMvalue().isEmpty()) {
                it.remove();
              }
              return null;
            }
          }
          return null;
        });
  }

  /**
   * Appends a new mvalue to the answer.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @param mvalue The new mvalue object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   * @throws ElementAlreadyExistsException If the mvalue key is already in use.
   */
  @Deprecated
  public void createMvalueOnAnswers(String companyId, FQCN fqcn, String metadataKey, Mvalue mvalue)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> mdit = answers.getMetadata().listIterator();
          while (mdit.hasNext()) {
            Metadata md = mdit.next();
            if (md.getKey().equals(metadataKey)) {
              ListIterator<Mvalue> mvit = md.getMvalue().listIterator();
              while (mvit.hasNext()) {
                Mvalue mv = mvit.next();
                if (mv.getKey().equals(mvalue.getKey())) {
                  throw new ElementAlreadyExistsException(
                      "Mvalue key "
                          + mv.getKey()
                          + " already exists in metadata "
                          + metadataKey
                          + " for instance "
                          + fqcn
                          + " in company "
                          + companyId);
                }
              }
              md.getMvalue().add((Mvalue) mvalue.clone());
              return null;
            }
          }
          throw new ElementNotFoundException(
              "Metadata key "
                  + metadataKey
                  + " not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  /**
   * Checks if a metadata key exists.
   *
   * @param companyId The id of the company.
   * @param fqcn The instance of the coach.
   * @param metadataKey The key of the metadata.
   * @param mvalueKey The key of the mvalue.
   * @return <code>true</code> if the metadata exists, or <code>false</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   */
  @Deprecated
  public boolean existsMvalueOnAnswer(
      String companyId, FQCN fqcn, String metadataKey, String mvalueKey) throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> mdit = answers.getMetadata().listIterator();
          while (mdit.hasNext()) {
            Metadata md = mdit.next();
            if (md.getKey().equals(metadataKey)) {
              return md.getMvalue().stream().anyMatch(mv -> mv.getKey().equals(mvalueKey));
            }
          }
          throw new ElementNotFoundException(
              "Metadata key "
                  + metadataKey
                  + " not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  /**
   * Returns a mvalue.
   *
   * @param companyId The id of the company.
   * @param fqcn The instance of the coach.
   * @param metadataKey The key of the metadata.
   * @param mvalueKey The key of the mvalue.
   * @return <code>Mvalue</code> if the mvalue exists, or <code>null</code> otherwise.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public Mvalue getMvalueOnAnswers(
      String companyId, FQCN fqcn, String metadataKey, String mvalueKey) throws CacheException {
    return readOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> mdit = answers.getMetadata().listIterator();
          while (mdit.hasNext()) {
            Metadata md = mdit.next();
            if (md.getKey().equals(metadataKey)) {
              ListIterator<Mvalue> mvit = md.getMvalue().listIterator();
              while (mvit.hasNext()) {
                Mvalue mv = mvit.next();
                if (mv.getKey().equals(mvalueKey)) {
                  return mv;
                }
              }
            }
          }
          return null;
        });
  }

  /**
   * Updates a mvalues. The key of the mvalue must be the same.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @param mvalue The new mvalue object.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   * @throws ElementNotFoundException If the mvalue was not found.
   */
  @Deprecated
  public void updateMvalueOnAnswers(String companyId, FQCN fqcn, String metadataKey, Mvalue mvalue)
      throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> mdit = answers.getMetadata().listIterator();
          while (mdit.hasNext()) {
            Metadata md = mdit.next();
            if (md.getKey().equals(metadataKey)) {
              ListIterator<Mvalue> mvit = md.getMvalue().listIterator();
              while (mvit.hasNext()) {
                Mvalue mv = mvit.next();
                if (mv.getKey().equals(mvalue.getKey())) {
                  mvit.set((Mvalue) mvalue.clone());
                  return null;
                }
              }
              throw new ElementNotFoundException(
                  "Mvalue key "
                      + mvalue.getKey()
                      + " not found in metadata "
                      + metadataKey
                      + " for instance "
                      + fqcn
                      + " in company "
                      + companyId);
            }
          }
          throw new ElementNotFoundException(
              "Metadata key "
                  + metadataKey
                  + " not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  /**
   * Deletes a mvalues.
   *
   * @param companyId The id of the company.
   * @param fqcn the full qualified coach name
   * @param metadataKey The key of the metadata.
   * @param mvalueKey The key of the mvalue.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   * @throws ElementNotFoundException If the metadata was not found.
   * @throws ElementNotFoundException If the mvalue was not found.
   */
  @Deprecated
  public void deleteMvalueOnAnswers(
      String companyId, FQCN fqcn, String metadataKey, String mvalueKey) throws CacheException {
    writeOnAnswers(
        companyId,
        fqcn,
        answers -> {
          ListIterator<Metadata> mdit = answers.getMetadata().listIterator();
          while (mdit.hasNext()) {
            Metadata md = mdit.next();
            if (md.getKey().equals(metadataKey)) {
              if (!md.getMvalue().removeIf(mv -> mv.getKey().equals(mvalueKey))) {
                throw new ElementNotFoundException(
                    "Mvalue key "
                        + mvalueKey
                        + " not found in metadata "
                        + metadataKey
                        + " for instance "
                        + fqcn
                        + " in company "
                        + companyId);
              }
              return null;
            }
          }
          throw new ElementNotFoundException(
              "Metadata key "
                  + metadataKey
                  + " not found for instance "
                  + fqcn
                  + " in company "
                  + companyId);
        });
  }

  // ---------------------
  // ----- AUDIT LOG -----
  // ---------------------

  private <R> R readOnAudits(String companyId, ICommand<Audits, R> command) throws CacheException {
    return data.executeOnCompany(companyId, cache -> cache.readOnAudits(command));
  }

  private void writeOnAudits(String companyId, ICommand<Audits, Void> command)
      throws CacheException {
    data.executeOnCompany(
        companyId,
        cache -> {
          cache.writeOnAudits(command);
          return null;
        });
  }

  /**
   * Appends a new audit object.
   *
   * @param companyId The id of the company.
   * @param audit The new audit object.
   * @throws CacheNotFoundException If the company was not found.
   */
  public void createAuditLog(String companyId, Audit audit) throws CacheException {
    writeOnAudits(
        companyId,
        audits -> {
          audits.getAudit().add((Audit) audit.clone());
          return null;
        });
  }

  /**
   * Returns all audit logs of an user.
   *
   * @param companyId The id of the company.
   * @param username The name of the user.
   * @return A list of all audit logs.
   * @throws CacheNotFoundException If the company was not found.
   */
  public List<Audit> getUserAuditLogs(String companyId, String username) throws CacheException {
    return readOnAudits(
        companyId,
        audits ->
            audits.getAudit().stream()
                .filter(audit -> audit.getUser().equals(username))
                .collect(Collectors.toList()));
  }

  /**
   * Returns all audit logs of a company.
   *
   * @param companyId The id of the company
   * @return A list of all audit logs.
   * @throws CacheNotFoundException If the company was not found.
   */
  public List<Audit> getAllAuditLogs(String companyId) throws CacheException {
    return readOnAudits(companyId, Audits::getAudit);
  }

  // -------------------
  // ----- COACHES -----
  // -------------------

  /**
   * Returns a coach of the defined id.
   *
   * @param coachId The id of the coach.
   * @return The coach if the coach was found, or <code>null</code> otherwise.
   */
  public Questionnaire getCoach(String coachId) throws CacheException {
    return coaches.read(coachId, null, coachLib -> (Questionnaire) coachLib.getFirst().clone());
  }

  /**
   * Returns a coach of the defined id and language.
   *
   * @param coachId The id of the coach.
   * @param locale The language of the coach.
   * @return The coach if the coach was found, or <code>null</code> otherwise.
   */
  public Questionnaire getCoach(String coachId, Locale locale) throws CacheException {
    return coaches.read(coachId, locale, coachLib -> (Questionnaire) coachLib.getFirst().clone());
  }

  /**
   * Checks if a coach is loaded.
   *
   * @param coachId The id of the coach.
   * @return <code>true</code> if the coach is loaded, or <code>false</code> otherwise.
   */
  public boolean existsCoach(String coachId) {
    return coaches.existsCoach(coachId, null);
  }

  /**
   * Checks if a coach is loaded.
   *
   * @param coachId The id of the coach.
   * @param locale The language of the coach.
   * @return <code>true</code> if the coach is loaded, or <code>false</code> otherwise.
   */
  public boolean existsCoach(String coachId, Locale locale) {
    return coaches.existsCoach(coachId, locale);
  }

  /**
   * Returns all loaded coaches.
   *
   * @return A list of all coaches.
   * @throws CacheException If a cache error occurs.
   */
  public List<Questionnaire> getAllCoaches() throws CacheException {
    return coaches.readAll(
        null,
        coachLibs ->
            coachLibs.stream()
                .map(coachLib -> (Questionnaire) coachLib.getFirst().clone())
                .collect(Collectors.toList()));
  }

  /**
   * Returns all loaded coaches of a defined language.
   *
   * @param locale The language of the coach.
   * @return A list of all coaches.
   * @throws CacheException If a cache error occurs.
   */
  public List<Questionnaire> getAllCoaches(Locale locale) throws CacheException {
    return coaches.readAll(
        locale,
        coachLibs ->
            coachLibs.stream()
                .map(coachLib -> (Questionnaire) coachLib.getFirst().clone())
                .collect(Collectors.toList()));
  }

  /**
   * Returns a dictionary of all answers of a company.
   *
   * @param companyId The id of the comapny.
   * @return answer dictionary
   * @throws CacheException If a cache error occurs.
   */
  public Map<String, Answers> getAllAnswersMap(String companyId) throws CacheException {
    return readOnAllAnswers(
        companyId,
        map -> {
          Map<String, Answers> result = new HashMap<>();
          for (Map.Entry<Path, Answers> entry : map.entrySet()) {
            result.put(FQCN.fromPath(entry.getKey()).toString(), entry.getValue());
          }
          return result;
        });
  }

  /**
   * Returns a question object.
   *
   * @param coachId The id of the coach
   * @param qid The id of the question
   * @return The question object if found, or <code>null</code> otherwise
   * @throws ElementNotFoundException if the coach was not found
   */
  public Question getQuestion(String coachId, String qid) throws CacheException {
    return coaches.read(
        coachId,
        null,
        coachLib -> {
          ListIterator<Question> it =
              coachLib.getFirst().getQuestions().getQuestion().listIterator();
          while (it.hasNext()) {
            Question question = it.next();
            if (question.getId().equals(qid)) {
              return (Question) question.clone();
            }
          }
          return null;
        });
  }

  /**
   * Returns a question object.
   *
   * @param coachId The id of the coach.
   * @param qid The id of the question.
   * @param locale The language of the coach.
   * @return The question object if found, or <code>null</code> otherwise
   * @throws ElementNotFoundException if the coach was not found
   */
  public Question getQuestion(String coachId, String qid, Locale locale) throws CacheException {
    return coaches.read(
        coachId,
        locale,
        coachLib -> {
          ListIterator<Question> it =
              coachLib.getFirst().getQuestions().getQuestion().listIterator();
          while (it.hasNext()) {
            Question question = it.next();
            if (question.getId().equals(qid)) {
              return (Question) question.clone();
            }
          }
          return null;
        });
  }

  /**
   * Helper method for hooks interface.
   *
   * @param coachId The queried coach.
   * @return List of defined libraries for the coach.
   */
  public List<CoachLibrary> getLibrariesForQuestionnaire(String coachId) throws CacheException {
    return coaches.read(coachId, null, Tuple::getSecond);
  }

  /**
   * Helper method for hooks interface.
   *
   * @param coachId The queried coach.
   * @return List of defined libraries for the coach.
   */
  public CoachLibrary getLibrary(String coachId, String libId) throws CacheException {
    return coaches.read(
        coachId,
        null,
        coachLib -> {
          ListIterator<CoachLibrary> it = coachLib.getSecond().listIterator();
          while (it.hasNext()) {
            CoachLibrary lib = it.next();
            if (lib.getId().equals(libId)) {
              return lib;
            }
          }
          return null;
        });
  }

  /**
   * Instantiate a coach by a company.
   *
   * @param companyId The id of the company.
   * @param coach The coach to instantiate.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheAlreadyExistsException If the instance is already in use.
   */
  public void instantiateCoach(String companyId, Questionnaire coach) throws CacheException {
    data.executeOnCompany(
        companyId,
        company -> {
          company.instantiateCoach(null, coach, null);
          return null;
        });
  }

  /**
   * Instantiate a sub-coach by a company.
   *
   * @param companyId The id of the company.
   * @param fqcn The fqcn of the calling coach.
   * @param subCoach The instance of the coach.
   * @param selectors The selectors to identify sub-coach instances.
   */
  public void instantiateSubCoach(
      String companyId, FQCN fqcn, Questionnaire subCoach, Set<String> selectors, Metadata parentArgument)
      throws CacheException {
      data.executeOnCompany(
              companyId,
              company -> {
                  company.instantiateCoach(fqcn.toPath().getParent(), subCoach, selectors);

                  // Set parent argument in subcoach
                  String subCoachName = selectors.iterator().next();
                  FQCN subcoachFqcn = FQCN.fromString(String.format("%s.%s.%s", fqcn.getRootCoachId(), subCoach.getId(), subCoachName));
                  setMetadataOnAnswers(companyId, subcoachFqcn, parentArgument);

                  // Set parent context in subcoach
                  CoachLibrary subcoachLibrary = getLibrariesForQuestionnaire(subcoachFqcn.getCoachId()).get(0);
                  CoachLibrary parentCoachLibrary = getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
                  subcoachLibrary.setParent(parentCoachLibrary.getContext());
                  return null;
              });
  }

    /**
     * Gets the currently active question of a coach given its company id and its FQCN
     * @param companyId the company in which the coach is
     * @param fqcn the name of the coach
     * @return the currently active question
     * @throws CacheException if something goes awry
     */
  public Question getCurrentQuestion(String companyId, FQCN fqcn) throws CacheException {
      CoachLibrary library = getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question question = library.getFirstQuestion(fqcn.getName());
      Metadata md = getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_STATE);
      if (md != null) {
          State state = MetadataUtils.fromMd(md, State.class);
          question = getQuestion(fqcn.getCoachId(), state.getResume());
      }
      return question;
  }

  /**
   * Removes an instantiated coach from a company.
   *
   * @param companyId The id of the company.
   * @param path The instance of the coach.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheNotFoundException If the instance was not found.
   */
  public void finalizeCoach(String companyId, Path path) throws CacheException {
    data.executeOnCompany(
        companyId,
        company -> {
          company.deleteCoach(path);
          return null;
        });
  }

    /**
     * Removes a sub-coach from a company
     * @param companyId ID of the company
     * @param path The path of the sub-coach
     * @throws CacheException If any errors happen while deleting the sub-coach
     */
    public void removeSubCoach(String companyId, Path path) throws CacheException {
        data.executeOnCompany(companyId, companyCache -> {
            companyCache.deleteSubCoach(path);
            return null;
        });
    }

    /**
     * Removes a sub-coach from a company given an FQCN
     * @param companyId ID of the company
     * @param fqcn FQCN of the subcoach
     * @throws CacheException If any errors happen while deleting the sub-coach
     */
    public void removeSubCoach(String companyId, FQCN fqcn) throws CacheException {
        CoachLibrary library = getLibrariesForQuestionnaire(fqcn.getRootCoachId()).get(0);
        library.onRemove(fqcn);

        removeSubCoach(companyId, fqcn.toPath());
    }

  /**
   * Lists all instantiated coaches for a company.
   *
   * @param companyId The id of the company.
   * @return List of all instantiated coaches.
   * @throws CacheNotFoundException If the company was not found.
   */
  public List<FQCN> listInstantiatedCoaches(String companyId) throws CacheException {
    return data.executeOnCompany(
            companyId, company -> company.listInstantiatedCoaches().stream().map(FQCN::fromPath))
        .collect(Collectors.toList());
  }

  // ---------------------
  // ----- Replicate -----
  // ---------------------

  /**
   * Zips a company and returns the *.zip archive as a file response.
   *
   * @param companyId The id of the company.
   * @return <code>FileResponse</code> containing the zipped company as *.zip.
   * @throws CacheNotFoundException If the company was not found.
   * @throws CacheException If an error occurs
   */
  public FileResponse zipCompany(String companyId) throws CacheException {
    // cannot use normal output stream, because of jersey StreamingOutput
    return data.executeOnCompany(
        companyId,
        companyCache -> {
          try {
            Path temp = Files.createTempFile(data.path, companyId, null);
            try {
              companyCache.zip(temp);
              return new FileResponse(Files.readAllBytes(temp));
            } finally {
              Files.delete(temp);
            }
          } catch (Exception e) {
            throw new CacheException(e.getMessage());
          }
        });
  }

  /**
   * Unzips a company from a *.zip archive.
   *
   * @param companyId The id of the company.
   * @param inputStream The *.zip archive as <code>InputStream</code>.
   * @throws CacheAlreadyExistsException If the company already exists.
   * @throws CacheException If an error occurred.
   */
  public void createCompanyFromArchive(String companyId, InputStream inputStream)
      throws CacheException {
    Path zip = data.path.resolve(companyId + ".zip");
    try {
      Files.copy(inputStream, zip, StandardCopyOption.REPLACE_EXISTING);
      data.unzipCompany(zip);
    } catch (Exception e) {
      throw new CacheException(e.getMessage());
    }
  }

  /**
   * Creates a files response to download a resource of a company.
   *
   * @param companyId The id of the company.
   * @param relative The relative path of a resource.
   * @return <code>FileResponse</code> containing the resource
   * @throws CacheAlreadyExistsException If the company already exists.
   * @throws CacheException If an error occurred.
   */
  public FileResponse createFileResponse(String companyId, Path relative) throws CacheException {
    // cannot use normal output stream, because of jersey StreamingOutput
    return data.executeOnCompany(companyId, company -> company.createFileResponse(relative));
  }

  /**
   * Synchronizes a resource in a company.
   *
   * @param companyId The id of the company.
   * @param relative The relative path of a resource.
   * @param inputStream The resource as <code>InputStream</code>.
   * @throws CacheAlreadyExistsException If the company already exists.
   * @throws CacheException If an error occurred.
   */
  public void syncFile(String companyId, Path relative, InputStream inputStream, boolean overwrite)
      throws CacheException {
    data.executeOnCompany(
        companyId,
        company -> {
          company.syncFile(relative, inputStream, overwrite);
          return null;
        });
  }

  /**
   * Adds or removes the company read only flag file.
   *
   * @param companyId The id of the company.
   * @param readonly <code>True</code> to mark the company as readonly or <code>false</code>
   *     otherwise.
   * @throws CacheAlreadyExistsException If the company already exists.
   * @throws CacheException If an error occurred.
   */
  public void setCompanyReadonly(String companyId, boolean readonly) throws CacheException {
    data.executeOnCompany(
        companyId,
        cache -> {
          cache.setReadOnly(readonly);
          return null;
        });
  }

  // ---------------------
  // -- Import / Export --
  // ---------------------

  /**
   * Import coach (and sub coaches) data from a zip archive. This operation will

   * @param companyId       The id of the company.
   * @param coachId         The id of the coach (not the instance) to export.
   * @return                Exported data as <code>FileResponse</code> of zip archive.
   * @throws CacheException
   */
  public FileResponse zipCoach(String companyId, String coachId) throws CacheException {
    return data.executeOnCompany(
        companyId,
        companyCache -> {
          try {
            Path temp = Files.createTempFile(data.path, companyId, null);
            try {
              companyCache.zipCoach(temp, coachId);
              return new FileResponse(Files.readAllBytes(temp));
            } finally {
              Files.delete(temp);
            }
          } catch (Exception e) {
            throw new CacheException(e.getMessage());
          }
        });
  }

  /**
   * Import coach (and sub coaches) data from a zip archive. This operation will
   * <b>overwrite</b> any existing data.
   *
   * @param companyId       The id of the company.
   * @param coachId         The id of the coach (not the instance) to overwrite.
   * @param zipUploadStream The archive is expected to match the filesystem structure of a coach.
   * @throws CacheException
   */
  public void unzipCoach(String companyId, String coachId, InputStream zipUploadStream) throws CacheException {
    data.executeOnCompany(
      companyId,
      companyCache -> {
        companyCache.unzipCoach(zipUploadStream, coachId);
        return null;
      });
  }

    /**
     * Exposes all currently active questions inlcuding subcoach questions.
     * @param companyId The id of the company
     * @param fqcn The FQCN
     * @return List of tuple from FQCN to question. The FQCN is helpful to know from which subcoach instance a question
     * is hailing.
     * @throws CacheException If something goes wrong accessing the cache
     */
    public List<Tuple<FQCN, Question>> getActiveQuestionsWithFqcn(String companyId, FQCN fqcn) throws CacheException {
        List<Question> questions = getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0).peekQuestions(null);
        return SubcoachHelper
                .of(companyId, fqcn, this)
                .insertSubcoachQuestions(questions);
    }
}
