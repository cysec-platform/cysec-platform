package eu.smesec.bridge;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Answer;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.generated.Questionnaire;

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

  Questionnaire getCoach() throws CacheException;

  Questionnaire getCoach(String coachId) throws CacheException;

  List<CoachLibrary> getLibraries(String coachId) throws CacheException;

  List<Questionnaire> getAllCoaches() throws CacheException;

  void instantiateSubCoach(Questionnaire subCoach, Set<String> selectors)
        throws CacheException;

  void instantiateSubCoach(Questionnaire subCoach, FQCN fqcn, Set<String> selectors)
          throws CacheException;

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
