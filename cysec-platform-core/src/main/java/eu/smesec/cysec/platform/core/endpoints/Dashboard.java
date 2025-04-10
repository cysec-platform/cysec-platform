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
package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
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
import eu.smesec.cysec.platform.core.json.CoachMetaData;
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
import java.util.stream.Stream;

import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@Secured
@DenyAll
@Path("rest/dashboard")
public class Dashboard {

  public static final String CONFIG_RECOMMENDATIONS_COUNT = "cysec_recommend_count";
  public static final String CONFIG_HIDE_LIB_COMPANY = "cysec_hide_lib_company";
  public static final String CONFIG_ENABLE_DASHBOARD_RECOMMENDATIONS = "cysec_enable_dashboard_recommendations";
  public static final String CONFIG_ENABLE_DASHBOARD_SIDEBAR = "cysec_enable_dashboard_sidebar";

  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Context
  private ServletContext context;
  @Inject
  private CacheAbstractionLayer cal;

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
      Map<String, Questionnaire> loadedCoaches = cal.getAllCoaches(locale).stream()
          // filter out subcoaches, since we don't want to see them on the dashboard
          .filter(q -> q.getParent() == null)
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
      CoachHelper coachHelper = new CoachHelper(
          LibCal.FQCN_COMPANY.toString(),
          companyCoach.getReadableName());
      coachHelper.setDescription(companyCoach.getDescription());
      coachHelper.setBlocks(companyCoach.getBlocks() == null ? null : companyCoach.getBlocks().getBlock());
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

      // Workaround to hide company coach (lib-company) since it currently stores
      // global non-coach data
      // and therefore needs to be loaded for the platform to work. Once the data is
      // stored in a different
      // way, this code block can be removed
      if (!CysecConfig.getDefault().getBooleanValue(contextName, CONFIG_HIDE_LIB_COMPANY)) {
        instantiated.add(coachHelper);
      }
      // End of workaround

      FQCN lastSelectedFqcn = lastSelected != null ? FQCN.fromString(lastSelected.getCoachId()) : LibCal.FQCN_COMPANY;
      // process other coaches
      for (Questionnaire coach : loadedCoaches.values().stream()
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
              Set<String> mvalues = md.getMvalue().stream().map(Mvalue::getKey).collect(Collectors.toSet());
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
      recommendations = recommendations.stream()
          .sorted(Comparator.comparingInt(Recommendation::getOrder))
          .limit(CysecConfig.getDefault().getNumericValue(contextName, CONFIG_RECOMMENDATIONS_COUNT))
          .collect(Collectors.toList());
      badges = badges.subList(Integer.max(0, badges.size() - 3), badges.size());

      // add data for jsp
      DashboardMsg msg = new DashboardMsg(locale, instantiated.size(), remaining.size(), badges.size());
      for (CoachHelper coach : instantiated) {
        List<CoachMetaData> metadata = getMetadata(companyId, FQCN.fromString(coach.getId())).stream()
            .filter(CoachMetaData::isVisible)
            .collect(Collectors.toList());
        coach.setVisibleCoachMetadata(metadata);
      }

      String userName = context.getAttribute("user").toString();
      boolean userIsAdmin = cal.getAllAdminUsers(companyId).stream()
          .anyMatch(u -> u.getUsername().equals(userName));

      // Read config values
      boolean enableRecommendations = CysecConfig.getDefault().getBooleanValue(contextName,
          CONFIG_ENABLE_DASHBOARD_RECOMMENDATIONS);
      boolean enableSidebar = CysecConfig.getDefault().getBooleanValue(contextName, CONFIG_ENABLE_DASHBOARD_SIDEBAR);

      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("userIsAdmin", userIsAdmin);
      model.put("instantiated", instantiated);
      model.put("remaining", remaining);
      model.put("recommendations", recommendations);
      model.put("badges", badges);
      model.put("enableRecommendations", enableRecommendations);
      model.put("enableSidebar", enableSidebar);
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

  @GET
  @Path("/metadata/{id}")
  @Produces(MediaType.TEXT_HTML)
  public Response getMetadata(@PathParam("id") String coachId) {
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
    String company = context.getAttribute("company").toString();
    FQCN fqcn = FQCN.fromString(coachId);

    try {
      List<CoachMetaData> metadata = getMetadata(company, fqcn);

      DashboardMsg msg = new DashboardMsg(locale, 0, 0, 0); // Number of coaches is not relevant here, so we just set it to zero
      Map<String, Object> model = new HashMap<>();
      model.put("metadata", metadata);
      model.put("msg", msg.getMessages());

      return Response.status(200)
          .entity(new Viewable("/dashboard/metadata", model))
          .build();
    } catch (CacheException e) {
      logger.warning(e.getMessage());
      return Response.status(400).build();
    }
  }

  @PUT
  @Path("/metadata/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetadata(@PathParam("id") String coachId, List<CoachMetaData> body) {
    Stream<CoachMetaData> visible = body.stream().filter(it -> it.isVisible());
    Stream<CoachMetaData> hidden = body.stream().filter(it -> !it.isVisible());

    try {
      updateMetadata(visible, CoachMetaData.KEY_VISIBLE, coachId);
      updateMetadata(hidden, CoachMetaData.KEY_HIDDEN, coachId);
    } catch (CacheException e) {
      logger.warning(e.getMessage());
      return Response.status(400).build();
    }

    return Response.status(200).build();
  }

  private List<CoachMetaData> getMetadata(String companyId, FQCN fqcn) throws CacheException {
    Stream<CoachMetaData> stream = Stream.of();

    Metadata visible = cal.getMetadataOnAnswer(companyId, fqcn, CoachMetaData.KEY_VISIBLE);
    if (visible != null) {
      stream = Stream.concat(stream, visible.getMvalue().stream()
          .map(mVal -> new CoachMetaData(
              mVal.getKey(),
              mVal.getStringValueOrBinaryValue().getValue(),
              true)));
    }

    Metadata hidden = cal.getMetadataOnAnswer(companyId, fqcn, CoachMetaData.KEY_HIDDEN);
    if (hidden != null) {
      stream = Stream.concat(
          stream,
          hidden.getMvalue()
              .stream()
              .map(mVal -> new CoachMetaData(
                  mVal.getKey(),
                  mVal.getStringValueOrBinaryValue().getValue(),
                  false)));
    }

    return stream.collect(Collectors.toList());
  }

  private void updateMetadata(Stream<CoachMetaData> stream, String metDataKey, String coachId) throws CacheException {
    Metadata metaData = stream
        .map(meta -> MetadataUtils.createMvalueStr(meta.getKey(), meta.getValue()))
        .collect(
            () -> {
              Metadata meta = new Metadata();
              meta.setKey(metDataKey);
              return meta;
            },
            (meta, mVal) -> meta.getMvalue().add(mVal),
            (meta1, meta2) -> meta1.getMvalue().addAll(meta2.getMvalue()));

    String company = context.getAttribute("company").toString();
    FQCN fqcn = FQCN.fromString(coachId);

    if (cal.getMetadataOnAnswer(company, fqcn, metDataKey) != null) {
      // remove all existing meta data (to conform to PUT semantic) because
      // setMetadataOnAnswers will merge mValues
      cal.deleteMetadataOnAnswers(company, fqcn, metDataKey);
    }

    cal.setMetadataOnAnswers(company, fqcn, metaData);
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
