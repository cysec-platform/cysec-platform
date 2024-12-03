/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * <p>This interface defines the access of a library on the Coach Abstraction Layer.</p>
 */
public interface ILibCal {
  void setMetadata(FQCN fqcn, Metadata metadata) throws CacheException;

  Metadata getMetadata(FQCN fqcn, String metadataKey) throws CacheException;

  List<Metadata> getAllMetadata(FQCN fqcn) throws CacheException;

  void deleteMetadata(FQCN fqcn, String metadataKey) throws CacheException;

  void removeMvalues(FQCN fqcn, String metadataKey, Set<String> mvalueKeys)
        throws CacheException;

  @Deprecated
  void setMetadataOnAnswers(Metadata metadata) throws CacheException;

  @Deprecated
  Metadata getMetadataOnAnswers(String metadataKey) throws CacheException;

  @Deprecated
  List<Metadata> getAllMetadataOnAnswer() throws CacheException;

  @Deprecated
  void deleteMetadataOnAnswers(String metadataKey) throws CacheException;

  @Deprecated
  void removeMvaluesFromAnswers(String metadataKey, Set<String> mvalueKeys)
        throws CacheException;
  
  Answer getAnswer(Object questionId) throws CacheException;

  Answer getAnswer(String coachId, Object questionId) throws CacheException;

  List<Answer> getAllAnswers() throws CacheException;

  void createAnswer(String coachId, Answer answer) throws CacheException;

  void updateAnswer(String coachId, Answer answer) throws CacheException;

  void removeAnswer(String coachId, Object questionId) throws CacheException;

  Questionnaire getCoach() throws CacheException;

  Questionnaire getCoach(String coachId) throws CacheException;

  List<CoachLibrary> getLibraries(String coachId) throws CacheException;

  List<Questionnaire> getAllCoaches() throws CacheException;

  void instantiateSubCoach(Questionnaire subCoach, Set<String> selectors, Metadata parentArgument)
        throws CacheException;

  void instantiateSubCoach(Questionnaire subCoach, FQCN fqcn, Set<String> selectors, Metadata parentArgument)
          throws CacheException;

  void removeSubCoach(FQCN fqcn) throws CacheException;

  @Deprecated
  void setMetadataOnCompany(Metadata metadata) throws CacheException;

  @Deprecated
  Metadata getMetadataOnCompany(String metadataKey) throws CacheException;

  @Deprecated
  List<Metadata> getAllMetadataOnCompany() throws CacheException;

  @Deprecated
  void deleteMetadataOnCompany(String metadataKey) throws CacheException;

  @Deprecated
  void removeMvaluesFroCompany(String metadataKey, Set<String> mvalueKeys)
        throws CacheException;

  void registerResources(CoachLibrary library) throws IOException;

  boolean checkResource(String coachId, String libId, String path);

  void unregisterResources(String coachId) throws IOException;
}
