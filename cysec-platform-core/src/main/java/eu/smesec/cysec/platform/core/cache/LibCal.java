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

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.ILibCal;
import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Coach Abstraction Layer interface implementation for libraries.
 *
 * @author Claudio Seitz
 * @version 1.0
 */
public class LibCal implements ILibCal {
  public static final FQCN FQCN_COMPANY = FQCN.fromString("lib-company");

  private final CacheAbstractionLayer cal;
  private final ServletContext context;
  private final ResourceManager resManager;

  LibCal(CacheAbstractionLayer cal, @Context ServletContext context, ResourceManager resManager) {
    this.cal = cal;
    this.context = context;
    this.resManager = resManager;
  }

  private String getCompanyId() {
    return context.getAttribute("company").toString();
  }

  private FQCN getCoachContext() {
    return FQCN.fromString(context.getAttribute("fqcn").toString());
  }

  @Override
  public void setMetadata(FQCN fqcn, Metadata metadata) throws CacheException {
    cal.setMetadataOnAnswers(getCompanyId(), fqcn, metadata);
  }

  @Override
  public Metadata getMetadata(FQCN fqcn, String metadataKey) throws CacheException {
    return cal.getMetadataOnAnswer(getCompanyId(), fqcn, metadataKey);
  }

  @Override
  public List<Metadata> getAllMetadata(FQCN fqcn) throws CacheException {
    return cal.getAllMetadataOnAnswer(getCompanyId(), fqcn);
  }

  @Override
  public void deleteMetadata(FQCN fqcn, String metadataKey) throws CacheException {
    cal.deleteMetadataOnAnswers(getCompanyId(), fqcn, metadataKey);
  }

  @Override
  public void removeMvalues(FQCN fqcn, String metadataKey, Set<String> mvalueKeys)
      throws CacheException {
    cal.removeMvaluesFromAnswer(getCompanyId(), fqcn, metadataKey, mvalueKeys);
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#getAnswer(String, Object)
   */
  @Deprecated
  @Override
  public Answer getAnswer(Object questionId) throws CacheException {
    return cal.getAnswer(getCompanyId(), getCoachContext(), questionId);
  }

  @Override
  public Answer getAnswer(String coachId, Object questionId) throws CacheException {
    return cal.getAnswer(getCompanyId(), FQCN.fromString(coachId), questionId);
  }

  @Override
  public List<Answer> getAllAnswers() throws CacheException {
    return cal.getAllAnswers(getCompanyId(), getCoachContext());
  }

  @Override
  public void createAnswer(String coachId, Answer answer) throws CacheException {
    cal.createAnswer(getCompanyId(), FQCN.fromString(coachId), answer);
  }

  @Override
  public void updateAnswer(String coachId, Answer answer) throws CacheException {
    cal.updateAnswer(getCompanyId(), FQCN.fromString(coachId), answer);
  }

  @Override
  public void removeAnswer(String coachId, Object questionId) throws CacheException {
    cal.removeAnswer(getCompanyId(), FQCN.fromString(coachId), questionId);
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#getCoach(String)
   */
  @Deprecated
  @Override
  public Questionnaire getCoach() throws CacheException {
    return cal.getCoach(getCoachContext().getCoachId());
  }

  @Override
  public Questionnaire getCoach(String coachId) throws CacheException {
    return cal.getCoach(coachId);
  }

  @Override
  public List<CoachLibrary> getLibraries(String coachId) throws CacheException {
    return cal.getLibrariesForQuestionnaire(coachId);
  }

  @Override
  public List<Questionnaire> getAllCoaches() throws CacheException {
    return cal.getAllCoaches();
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#instantiateSubCoach(Questionnaire, FQCN, Set, Metadata)
   */
  @Override
  public void instantiateSubCoach(Questionnaire subCoach, Set<String> selectors, Metadata parentArgument)
      throws CacheException {
    cal.instantiateSubCoach(getCompanyId(), getCoachContext(), subCoach, selectors, parentArgument);
  }

  @Override
  public void instantiateSubCoach(Questionnaire subCoach, FQCN fqcn, Set<String> selectors, Metadata parentArgument)
      throws CacheException {
    cal.instantiateSubCoach(getCompanyId(), fqcn, subCoach, selectors, parentArgument);
  }

  @Override
  public void removeSubCoach(FQCN fqcn) throws CacheException {
    cal.removeSubCoach(getCompanyId(), fqcn.toPath());
  }

  @Override
  public void registerResources(CoachLibrary library) throws IOException {
    resManager.registerLibResources(library);
  }

  @Override
  public boolean checkResource(String coachId, String libId, String path) {
    return resManager.hasResource(coachId, libId, path);
  }

  @Override
  public void unregisterResources(String coachId) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#setMetadata(FQCN, Metadata)
   */
  @Deprecated
  @Override
  public void setMetadataOnAnswers(Metadata metadata) throws CacheException {
    cal.setMetadataOnAnswers(getCompanyId(), getCoachContext(), metadata);
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#getMetadata(FQCN, String)
   */
  @Deprecated
  @Override
  public Metadata getMetadataOnAnswers(String metadataKey) throws CacheException {
    return cal.getMetadataOnAnswer(getCompanyId(), getCoachContext(), metadataKey);
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#getAllMetadata(FQCN)
   */
  @Deprecated
  @Override
  public List<Metadata> getAllMetadataOnAnswer() throws CacheException {
    return cal.getAllMetadataOnAnswer(getCompanyId(), getCoachContext());
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#deleteMetadata(FQCN, String)
   */
  @Deprecated
  @Override
  public void deleteMetadataOnAnswers(String metadataKey) throws CacheException {
    cal.deleteMetadataOnAnswers(getCompanyId(), getCoachContext(), metadataKey);
  }

  /**
   * Deprecated method, this method will be removed in a later release.
   *
   * @see LibCal#removeMvaluesFroCompany(String, Set)
   */
  @Deprecated
  @Override
  public void removeMvaluesFromAnswers(String metadataKey, Set<String> mvalueKeys)
      throws CacheException {
    cal.removeMvaluesFromAnswer(getCompanyId(), getCoachContext(), metadataKey, mvalueKeys);
  }

  @Override
  public void setMetadataOnCompany(Metadata metadata) throws CacheException {
    cal.setMetadataOnAnswers(getCompanyId(), FQCN_COMPANY, metadata);
  }

  @Override
  public Metadata getMetadataOnCompany(String metadataKey) throws CacheException {
    return cal.getMetadataOnAnswer(getCompanyId(), FQCN_COMPANY, metadataKey);
  }

  @Override
  public List<Metadata> getAllMetadataOnCompany() throws CacheException {
    return cal.getAllMetadataOnAnswer(getCompanyId(), FQCN_COMPANY);
  }

  @Override
  public void deleteMetadataOnCompany(String metadataKey) throws CacheException {
    cal.deleteMetadataOnAnswers(getCompanyId(), FQCN_COMPANY, metadataKey);
  }

  @Override
  public void removeMvaluesFroCompany(String metadataKey, Set<String> mvalueKeys)
      throws CacheException {
    cal.removeMvaluesFromAnswer(getCompanyId(), FQCN_COMPANY, metadataKey, mvalueKeys);
  }
}
