package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.generated.Answers;
import eu.smesec.cysec.platform.bridge.generated.Attachment;
import eu.smesec.cysec.platform.bridge.generated.Attachments;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.md.Badge;
import eu.smesec.cysec.platform.bridge.md.LastSelected;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.md.Rating;
import eu.smesec.cysec.platform.bridge.md.Recommendation;
import eu.smesec.cysec.platform.bridge.md.Skills;
import eu.smesec.cysec.platform.bridge.md.State;
import eu.smesec.cysec.platform.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.LibCal;
import eu.smesec.cysec.platform.core.config.CysecConfig;
import eu.smesec.cysec.platform.core.helpers.dashboard.CoachHelper;
import eu.smesec.cysec.platform.core.messages.DashboardMsg;
import eu.smesec.cysec.platform.core.utils.LocaleUtils;
import eu.smesec.cysec.platform.core.auth.Secured;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.stream.Collectors;
import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@Secured
@DenyAll
@Path("rest/dashboard")
public class Dashboard {
  public static final String RECOMMENDATIONS_SIZE = "cysec_recommend_count";

  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Context private ServletContext context;
  @Inject private CacheAbstractionLayer cal;

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
    try {
      List<CoachHelper> remaining = new ArrayList<>();
      List<Recommendation> recommendations = new ArrayList<>();
      List<Badge> badges = new ArrayList<>();
      LastSelected lastSelected = null;
      Tuple<FQCN, Skills> skills = null;
      // fetch dashboard data
      Map<String, Questionnaire> loadedCoaches =
          cal.getAllCoaches(locale).stream()
              .collect(Collectors.toMap(Questionnaire::getId, q -> q));
      Map<String, Answers> answersMap = cal.getAllAnswersMap(companyId);
      Map<Questionnaire, Map<FQCN, Answers>> coachAnswersMap = new HashMap<>();
      for (Map.Entry<String, Answers> answers : answersMap.entrySet()) {
        FQCN fqcn = FQCN.fromString(answers.getKey());
        Questionnaire coach = loadedCoaches.get(fqcn.getCoachId());
        if (!coachAnswersMap.containsKey(coach)) {
          coachAnswersMap.put(coach, new HashMap<>());
        }
        coachAnswersMap.get(coach).put(fqcn, answers.getValue());
      }
      // extract company coach
      Questionnaire companyCoach = loadedCoaches.get(LibCal.FQCN_COMPANY.getCoachId());
      // process company coach
      CoachHelper coachHelper =
          new CoachHelper(
              LibCal.FQCN_COMPANY.toString(),
              companyCoach.getReadableName());
      coachHelper.setDescription(companyCoach.getDescription());
      coachHelper.setBlocks(companyCoach.getBlocks().getBlock());
      Attachments attachments = companyCoach.getAttachments();
      if (attachments != null) {
        for (Attachment attachment : attachments.getAttachment()) {
          if (attachment.getId().equals("icon")) {
            logger.info(String.format("Adding icon to coach %s", companyCoach.getReadableName()));
            coachHelper.setIcon(attachment.getContent().getValue());
            break;
          }
        }
      }
      Answers companyAnswers = answersMap.get(LibCal.FQCN_COMPANY.toString());
      for (Metadata md : companyAnswers.getMetadata()) {
        String mdKey = md.getKey();
        if (mdKey.startsWith(MetadataUtils.MD_RECOMMENDED)) {
          // add all metadata belonging to RECOMMENDED, given:
          // Name, Description and Order present
          logger.info("Adding recommendations");
          recommendations.add(MetadataUtils.fromMd(md, Recommendation.class));
        } else if (mdKey.startsWith(MetadataUtils.MD_BADGES)) {
          // add all metadata belonging to BADGES
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
      List<CoachHelper> instantiated = new ArrayList<>();
      instantiated.add(coachHelper);
      FQCN lastSelectedFqcn =
          lastSelected != null ? FQCN.fromString(lastSelected.getCoachId()) : LibCal.FQCN_COMPANY;
      // process other coaches
      for (Questionnaire coach :
          loadedCoaches.values().stream()
              .sorted(Comparator.comparingInt(Questionnaire::getOrder))
              .collect(Collectors.toList())) {
        // skip already processed company coach
        if (coach.getId().equals(LibCal.FQCN_COMPANY.getCoachId())) {
          continue;
        }
        logger.info(String.format("Processing coach %s", coach.getReadableName()));
        String coachId = coach.getId();
        if (!coachAnswersMap.containsKey(coach)) {
          if (coach.getParent() == null) {
            // add coach to remaining section
            logger.info(String.format("Adding coach %s to list of remaining coaches", coachId));
            remaining.add(
                new CoachHelper(coachId, coach.getReadableName()));
          }
        } else {
          for (Map.Entry<FQCN, Answers> entry : coachAnswersMap.get(coach).entrySet()) {
            FQCN fqcn = entry.getKey();
            String name = getAnswerName(fqcn, loadedCoaches);
            // installed coach
            coachHelper = new CoachHelper(fqcn.toString(), name);
            coachHelper.setDescription(coach.getDescription());
            if (coach.getBlocks() != null) {
              coachHelper.setBlocks(coach.getBlocks().getBlock());
            }
            // coach icon attachments
            attachments = coach.getAttachments();
            if (attachments != null) {
              for (Attachment attachment : attachments.getAttachment()) {
                if (attachment.getId().equals("icon")) {
                  logger.info(String.format("Adding icon to coach %s", coach.getReadableName()));
                  coachHelper.setIcon(attachment.getContent().getValue());
                  break;
                }
              }
            }
            // metadata
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
            instantiated.add(coachHelper);
          }
        }
      }
      // trim recommendations and badges
      recommendations =
          recommendations.stream()
              .sorted(Comparator.comparingInt(Recommendation::getOrder))
              .limit(CysecConfig.getDefault().getNumericValue(contextName, RECOMMENDATIONS_SIZE))
              .collect(Collectors.toList());
      badges = badges.subList(Integer.max(0, badges.size() - 3), badges.size());

      // add data for jsp
      DashboardMsg msg =
          new DashboardMsg(locale, instantiated.size(), remaining.size(), badges.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("instantiated", instantiated);
      model.put("remaining", remaining);
      model.put("recommendations", recommendations);
      model.put("badges", badges);
      if (skills != null) {
        model.put(
            "skills",
            new Tuple<>(getAnswerName(skills.getFirst(), loadedCoaches), skills.getSecond()));
      }
      return Response.status(200).entity(new Viewable("/dashboard/dashboard", model)).build();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error loading dashboard", e);
    }
    return Response.status(500).build();
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
