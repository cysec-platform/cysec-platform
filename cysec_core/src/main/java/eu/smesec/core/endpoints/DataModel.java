package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Answer;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.generated.Mvalue;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.bridge.utils.Tuple;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.core.json.MValueAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Data endpoint to provide data for external services.
 */
public class DataModel {

    private final Logger logger = Logger.getLogger(DataModel.class.getName());
    private final Gson gson =
            new GsonBuilder()
                    .addSerializationExclusionStrategy(
                            new FieldsExclusionStrategy(MetadataUtils.MV_ENDURANCE_STATE))
                    .registerTypeAdapter(Mvalue.class, new MValueAdapter())
                    .create();
    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Delivers answers of a questionnaire in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains <code>Answers</code> from the coach
     */
    public String getAnswers(String id) throws CacheException {
        String company = LibCal.getCompany();
        if (company == null) return "{}";
        FQCN fqcn = FQCN.fromString(id);
        List<Answer> allAnswers = cal.getAllAnswers(company, fqcn);
        return gson.toJson(allAnswers);
    }

    /**
     * Delivers rating of a questionnaire in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains <code>Metadata</code> from the coach
     */
    public String getRating(String id) throws CacheException {
        String company = LibCal.getCompany();
        if (company == null) return "{}";

        FQCN fqcn = FQCN.fromString(id);
        Metadata metadata = cal.getMetadataOnAnswer(company, fqcn, MetadataUtils.MD_RATING);
        if (metadata != null) return gson.toJson(metadata.getMvalue());
        return "{}";
    }

    /**
     * Delivers all recommendations of a questionnaire in JSON format.
     *
     * @return A JSON object that contains <code>Metadata</code> from the coach
     */
    public String getRecommendations() throws CacheException {
        String company = LibCal.getCompany();
        List<Metadata> metadata =
                cal.getAllMetadataOnAnswer(company, LibCal.FQCN_COMPANY, MetadataUtils.MD_RECOMMENDED);
        return gson.toJson(metadata);
    }

    /**
     * Delivers the skills of a company in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains the Skills <code>Metadata</code>
     */
    public String getSkills(String id) throws CacheException {
        String company = LibCal.getCompany();

        FQCN fqcn = FQCN.fromString(id);
        Metadata skills = cal.getMetadataOnAnswer(company, fqcn, MetadataUtils.MD_SKILLS);
        if (skills != null) return gson.toJson(skills.getMvalue());
        return "{}";
    }

    /**
     * Returns a list of all instantiated coaches as JSON.
     *
     * @return A JSON object that contains all instantiated coaches
     */

    public String getCoaches() throws CacheException {
        String company = LibCal.getCompany();
        // load coach names
        Map<String, String> coaches = new HashMap<>();
        for (Questionnaire coach : cal.getAllCoaches()) {
            coaches.put(coach.getId(), coach.getReadableName());
        }
        // list instantiated coaches
        Map<String, String> instantiated = getInstantiatedCoaches(company, coaches);
        return gson.toJson(instantiated);

    }

    private Map<String, String> getInstantiatedCoaches(String company, Map<String, String> coaches) throws CacheException {
        return cal.listInstantiatedCoaches(company).stream()
                .map(fqcn -> {
                    String name =
                            fqcn.coacheIds()
                                    .map(coaches::get)
                                    .reduce((s, s2) -> s + "." + s2)
                                    .orElse("");
                    String coachName = fqcn.getName();
                    if (!coachName.equals("default")) {
                        name += "." + coachName;
                    }
                    return new Tuple<>(fqcn.toString(), name);
                })
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    //  private FQCN constructFQCN(Questionnaire coach) {
    //    FQCN coachFQCN = FQCN.fromString(coach.getId());
    //    if(coach.getParent() != null) {
    //      FQCN companyFQCN = FQCN.fromString(coach.getParent());
    //      coachFQCN = companyFQCN.resolveDefault(coach.getId());
    //    }
    //    return coachFQCN;
    //  }
}
