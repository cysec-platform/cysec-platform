package eu.smesec.core.cache;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.ILibCal;
import eu.smesec.bridge.Library;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Answer;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.generated.Questionnaire;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Coach Abstraction Layer interface implementation for libraries.
 *
 * @author Claudio Seitz
 * @version 1.0
 */
public class LibCal implements ILibCal {
    public static final FQCN FQCN_COMPANY = FQCN.fromString("lib-company");
    private static String coachContext;
    private static String company = null;

    private final CacheAbstractionLayer cal;
    private final ResourceManager resManager;

    LibCal(CacheAbstractionLayer cal, ResourceManager resManager) {
//        LibCal.company = company;
        this.cal = cal;
        this.resManager = resManager;
    }

    public static void setCoachContext(String coachContext) {
        LibCal.coachContext = coachContext;
    }

    private FQCN getCoachContext() {
        return FQCN.fromString(coachContext);
    }

    public static void setCompany(String company) {
        LibCal.company = company;
    }

    public static String getCompany() {
        return company;
    }

    @Override
    public void setMetadata(FQCN fqcn, Metadata metadata) throws CacheException {
        cal.setMetadataOnAnswers(getCompany(), fqcn, metadata);
    }

    @Override
    public Metadata getMetadata(FQCN fqcn, String metadataKey) throws CacheException {
        return cal.getMetadataOnAnswer(getCompany(), fqcn, metadataKey);
    }

    @Override
    public List<Metadata> getAllMetadata(FQCN fqcn) throws CacheException {
        return cal.getAllMetadataOnAnswer(getCompany(), fqcn);
    }

    @Override
    public void deleteMetadata(FQCN fqcn, String metadataKey) throws CacheException {
        cal.deleteMetadataOnAnswers(getCompany(), fqcn, metadataKey);
    }

    @Override
    public void removeMvalues(FQCN fqcn, String metadataKey, Set<String> mvalueKeys)
            throws CacheException {
        cal.removeMvaluesFromAnswer(getCompany(), fqcn, metadataKey, mvalueKeys);
    }

    /**
     * Deprecated method, this method will be removed in a later release.
     *
     * @see LibCal#getAnswer(String, Object)
     */
    @Deprecated
    @Override
    public Answer getAnswer(Object questionId) throws CacheException {
        return cal.getAnswer(getCompany(), getCoachContext(), questionId);
    }

    @Override
    public Answer getAnswer(String coachId, Object questionId) throws CacheException {
        return cal.getAnswer(getCompany(), FQCN.fromString(coachId), questionId);
    }

    @Override
    public List<Answer> getAllAnswers() throws CacheException {
        return cal.getAllAnswers(getCompany(), getCoachContext());
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
    public List<Library> getLibraries(String coachId) throws CacheException {
        return cal.getLibrariesForQuestionnaire(coachId);
    }

    @Override
    public List<Questionnaire> getAllCoaches() throws CacheException {
        return cal.getAllCoaches();
    }

    /**
     * Deprecated method, this method will be removed in a later release.
     *
     * @see LibCal#instantiateSubCoach(Questionnaire, FQCN, Set)
     */
    @Override
    public void instantiateSubCoach(Questionnaire subCoach, Set<String> selectors)
            throws CacheException {
        cal.instantiateSubCoach(getCompany(), getCoachContext(), subCoach, selectors);
    }

    @Override
    public void instantiateSubCoach(Questionnaire subCoach, FQCN fqcn, Set<String> selectors)
            throws CacheException {
        cal.instantiateSubCoach(getCompany(), fqcn, subCoach, selectors);
    }

    @Override
    public void registerResources(Library library) throws IOException {
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
        cal.setMetadataOnAnswers(getCompany(), getCoachContext(), metadata);
    }

    /**
     * Deprecated method, this method will be removed in a later release.
     *
     * @see LibCal#getMetadata(FQCN, String)
     */
    @Deprecated
    @Override
    public Metadata getMetadataOnAnswers(String metadataKey) throws CacheException {
        return cal.getMetadataOnAnswer(getCompany(), getCoachContext(), metadataKey);
    }

    /**
     * Deprecated method, this method will be removed in a later release.
     *
     * @see LibCal#getAllMetadata(FQCN)
     */
    @Deprecated
    @Override
    public List<Metadata> getAllMetadataOnAnswer() throws CacheException {
        return cal.getAllMetadataOnAnswer(getCompany(), getCoachContext());
    }

    /**
     * Deprecated method, this method will be removed in a later release.
     *
     * @see LibCal#deleteMetadata(FQCN, String)
     */
    @Deprecated
    @Override
    public void deleteMetadataOnAnswers(String metadataKey) throws CacheException {
        cal.deleteMetadataOnAnswers(getCompany(), getCoachContext(), metadataKey);
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
        cal.removeMvaluesFromAnswer(getCompany(), getCoachContext(), metadataKey, mvalueKeys);
    }

    @Override
    public void setMetadataOnCompany(Metadata metadata) throws CacheException {
        cal.setMetadataOnAnswers(getCompany(), FQCN_COMPANY, metadata);
    }

    @Override
    public Metadata getMetadataOnCompany(String metadataKey) throws CacheException {
        return cal.getMetadataOnAnswer(getCompany(), FQCN_COMPANY, metadataKey);
    }

    @Override
    public List<Metadata> getAllMetadataOnCompany() throws CacheException {
        return cal.getAllMetadataOnAnswer(getCompany(), FQCN_COMPANY);
    }

    @Override
    public void deleteMetadataOnCompany(String metadataKey) throws CacheException {
        cal.deleteMetadataOnAnswers(getCompany(), FQCN_COMPANY, metadataKey);
    }

    @Override
    public void removeMvaluesFroCompany(String metadataKey, Set<String> mvalueKeys)
            throws CacheException {
        cal.removeMvaluesFromAnswer(getCompany(), FQCN_COMPANY, metadataKey, mvalueKeys);
    }
}
