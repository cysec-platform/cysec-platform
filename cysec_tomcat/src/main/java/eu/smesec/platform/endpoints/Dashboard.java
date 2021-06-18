package eu.smesec.platform.endpoints;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.*;
import eu.smesec.bridge.md.*;
import eu.smesec.bridge.utils.Tuple;
import eu.smesec.platform.auth.Secured;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.core.endpoints.DashboardModel;
import eu.smesec.core.helpers.dashboard.CoachHelper;
import eu.smesec.core.utils.LocaleUtils;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Secured
@DenyAll
@Path("rest/dashboard")
public class Dashboard {
    public static final String RECOMMENDATIONS_SIZE = "cysec_recommend_count";

    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    @Context
    private ServletContext context;

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Renders the questionnaire list for a company.
     *
     * @return The rendered html document.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {
        logger.info("Retrieving dashboard");
        String companyId = context.getAttribute("company").toString();
        Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
        String contextName = context.getContextPath().substring(1);

        List<CoachHelper> remaining = new ArrayList<>();
        List<Recommendation> recommendations = new ArrayList<>();
        List<Badge> badges = new ArrayList<>();
        LastSelected lastSelected = null;
        Tuple<FQCN, Skills> skills = new Tuple<>(null, null);

        Map<String, Questionnaire> allCoaches;
        try {
            allCoaches = getAllCoaches(locale);
        } catch (CacheException e) {
            logger.log(Level.WARNING, "Error loading coaches", e);
            return Response.status(500).build();
        }
        Questionnaire companyCoach = getCompanyCoach(allCoaches);

        Map<String, Answers> answersMap;
        try {
            answersMap = getAllAnswersMap(companyId);
        } catch (CacheException e) {
            logger.log(Level.WARNING, "Error loading answers", e);
            return Response.status(500).build();
        }

        Answers companyAnswers = getCompanyAnswers(answersMap);

        Map<Questionnaire, Map<FQCN, Answers>> coachAnswersMap = new HashMap<>();
        if (answersMap != null) {
            coachAnswersMap = getCoachAnswerMap(answersMap, allCoaches);
        }

        //TODO: eu.smesec.core.helpers.dashboard.coachhelper readableName and readableClass are never used,
        // why are they in the constructor?
        CoachHelper coachHelper =
                new CoachHelper(
                        LibCal.FQCN_COMPANY.toString(),
                        companyCoach.getReadableName(),
                        companyCoach.getReadableClass());

        configCoachHelper(coachHelper, companyCoach);

        processMetadata(companyAnswers, recommendations, badges, lastSelected, coachHelper, skills);

        List<CoachHelper> coachHelpers = new ArrayList<>();
        coachHelpers.add(coachHelper);

        FQCN lastSelectedFqcn =
                lastSelected != null ? FQCN.fromString(lastSelected.getCoachId()) : LibCal.FQCN_COMPANY;

        //sort coaches
        List<Questionnaire> sortedCoaches = sortCoaches(allCoaches);

        // process other coaches
        skills = processOtherCoaches(remaining, skills, allCoaches, coachAnswersMap, coachHelpers, lastSelectedFqcn, sortedCoaches);
        // trim recommendations and badges
        recommendations = trimRecommendations(contextName, recommendations);
        badges = trimBadges(badges);

        Map<String, Object> model = DashboardModel.getModel(locale, allCoaches, coachHelpers, recommendations, remaining, badges, skills);
        return Response.status(200).entity(new Viewable("/dashboard/dashboard", model)).build();
    }

    private Tuple<FQCN, Skills> processOtherCoaches(List<CoachHelper> remaining, Tuple<FQCN, Skills> skills, Map<String, Questionnaire> allCoaches, Map<Questionnaire, Map<FQCN, Answers>> coachAnswersMap, List<CoachHelper> coachHelpers, FQCN lastSelectedFqcn, List<Questionnaire> sortedCoaches) {
        CoachHelper coachHelper;
        for (Questionnaire coach : sortedCoaches) {
            String coachId = coach.getId();
            // if not the company coach
            if (!coachId.equals(LibCal.FQCN_COMPANY.getCoachId())) {
                logger.info(String.format("Processing coach %s", coach.getReadableName()));
                if (!coachAnswersMap.containsKey(coach)) {
                    if (coach.getParent() == null) {
                        logger.info(String.format("Adding coach %s to list of remaining coaches", coachId));
                        remaining.add(new CoachHelper(coachId, coach.getReadableName(), coach.getReadableClass()));
                    }
                } else {
                    skills = processOtherCoachesMetadata(skills, allCoaches, coachAnswersMap, coachHelpers, lastSelectedFqcn, coach);
                }
            }
        }
        return skills;
    }

    private Tuple<FQCN, Skills> processOtherCoachesMetadata(Tuple<FQCN, Skills> skills, Map<String, Questionnaire> allCoaches, Map<Questionnaire, Map<FQCN, Answers>> coachAnswersMap, List<CoachHelper> coachHelpers, FQCN lastSelectedFqcn, Questionnaire coach) {
        CoachHelper coachHelper;
        for (Map.Entry<FQCN, Answers> entry : coachAnswersMap.get(coach).entrySet()) {
            FQCN fqcn = entry.getKey();
            String name = getAnswerName(fqcn, allCoaches);
            coachHelper = new CoachHelper(fqcn.toString(), name, coach.getReadableClass());
            configCoachHelper(coachHelper, coach);
            logger.info("Start processing metadata");
            Answers answers = entry.getValue();
            for (Metadata md : answers.getMetadata()) {
                String mdKey = md.getKey();
                Set<String> mvalues =
                        md.getMvalue().stream().map(Mvalue::getKey).collect(Collectors.toSet());
                // rating
                if (mdKey.equals(MetadataUtils.MD_RATING)
                        && mvalues.contains(MetadataUtils.MV_MICRO_SCORE)
                        && mvalues.contains(MetadataUtils.MV_MICRO_GRADE)) {
                    logger.info("Adding rating");
                    coachHelper.setRating(MetadataUtils.fromMd(md, Rating.class));
                } else if (lastSelectedFqcn.equals(fqcn) && mdKey.equals(MetadataUtils.MD_SKILLS)) {
                    logger.info("Adding skills");
                    skills = new Tuple<>(fqcn, MetadataUtils.fromMd(md, Skills.class));
                }
            }
            logger.info("Done processing metadata");
            coachHelpers.add(coachHelper);
        }
        return skills;
    }

    private void processMetadata(Answers companyAnswers, List<Recommendation> recommendations, List<Badge> badges, LastSelected lastSelected, CoachHelper coachHelper, Tuple<FQCN, Skills> skills) {
        for (Metadata md : companyAnswers.getMetadata()) {
            String mdKey = md.getKey();
            if (mdKey.startsWith(MetadataUtils.MD_RECOMMENDED)) {
                logger.info("Adding recommendations");
                recommendations.add(MetadataUtils.fromMd(md, Recommendation.class));
            } else if (mdKey.startsWith(MetadataUtils.MD_BADGES)) {
                logger.info("Adding badges");
                badges.add(MetadataUtils.fromMd(md, Badge.class));
            } else if (mdKey.startsWith(MetadataUtils.MD_LAST_SELECTED)) {
                lastSelected = MetadataUtils.fromMd(md, LastSelected.class);
            } else if (mdKey.startsWith(MetadataUtils.MD_RATING)) {
                coachHelper.setRating(MetadataUtils.fromMd(md, Rating.class));
            } else if (mdKey.startsWith(MetadataUtils.MD_STATE)) {
                coachHelper.setState(MetadataUtils.fromMd(md, State.class));
            } else if (mdKey.startsWith(MetadataUtils.MD_SKILLS)) {
                skills = new Tuple<>(LibCal.FQCN_COMPANY, MetadataUtils.fromMd(md, Skills.class));
            }
        }
    }

    private List<Questionnaire> sortCoaches(Map<String, Questionnaire> allCoaches) {
        return allCoaches.values().stream()
                .sorted(Comparator.comparingInt(Questionnaire::getOrder))
                .collect(Collectors.toList());
    }

    private void configCoachHelper(CoachHelper coachHelper, Questionnaire coach) {
        coachHelper.setDescription(coach.getDescription());
        coachHelper.setBlocks(coach.getBlocks().getBlock());

        Attachments attachments = coach.getAttachments();
        setCoachHelperIcon(coach, coachHelper, attachments);
    }

    private Answers getCompanyAnswers(Map<String, Answers> answersMap) {
        if (answersMap == null) return null;
        return answersMap.get(LibCal.FQCN_COMPANY.toString());
    }

    private Questionnaire getCompanyCoach(Map<String, Questionnaire> allCoaches) {
        if (allCoaches == null) return null;
        return allCoaches.get(LibCal.FQCN_COMPANY.getCoachId());
    }

    private void setCoachHelperIcon(Questionnaire companyCoach, CoachHelper coachHelper, Attachments attachments) {
        if (attachments == null) return;
        for (Attachment attachment : attachments.getAttachment()) {
            if (attachment.getId().equals("icon")) {
                logger.info(String.format("Adding icon to coach %s", companyCoach.getReadableName()));
                coachHelper.setIcon(attachment.getContent().getValue());
                break;
            }
        }
    }

    private Map<Questionnaire, Map<FQCN, Answers>> getCoachAnswerMap(Map<String, Answers> answersMap, Map<String, Questionnaire> allCoaches) {
        Map<Questionnaire, Map<FQCN, Answers>> coachAnswersMap = new HashMap<>();
        for (Map.Entry<String, Answers> answers : answersMap.entrySet()) {
            FQCN fqcn = FQCN.fromString(answers.getKey());
            Questionnaire coach = allCoaches.get(fqcn.getCoachId());
            if (!coachAnswersMap.containsKey(coach)) {
                coachAnswersMap.put(coach, new HashMap<>());
            }
            coachAnswersMap.get(coach).put(fqcn, answers.getValue());
        }
        return coachAnswersMap;
    }

    private Map<String, Answers> getAllAnswersMap(String companyId) throws CacheException {
        return cal.getAllAnswersMap(companyId);
    }

    private Map<String, Questionnaire> getAllCoaches(Locale locale) throws CacheException {
        return cal.getAllCoaches(locale).stream()
                .collect(Collectors.toMap(Questionnaire::getId, q -> q));
    }

    private List<Badge> trimBadges(List<Badge> badges) {
        return badges.subList(Integer.max(0, badges.size() - 3), badges.size());
    }

    private List<Recommendation> trimRecommendations(String contextName, List<Recommendation> recommendations) {
        return recommendations.stream()
                .sorted(Comparator.comparingInt(Recommendation::getOrder))
                .limit(CysecConfig.getDefault().getNumericValue(contextName, RECOMMENDATIONS_SIZE))
                .collect(Collectors.toList());
    }

    private String getAnswerName(FQCN fqcn, Map<String, Questionnaire> coaches) {
        StringBuilder name = new StringBuilder(coaches.get(fqcn.getCoachId()).getReadableName());
        String fileName = fqcn.getName();
        if (!fileName.equals("default")) {
            name.append(".").append(fileName);
        }
        return name.toString();
    }
}
