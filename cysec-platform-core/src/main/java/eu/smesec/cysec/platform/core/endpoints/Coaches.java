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
package eu.smesec.cysec.platform.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.cysec.platform.bridge.execptions.ElementNotFoundException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Audit;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.generated.UserAction;
import eu.smesec.cysec.platform.bridge.md.LastSelected;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.md.State;
import eu.smesec.cysec.platform.bridge.utils.AuditUtils;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.LibCal;
import eu.smesec.cysec.platform.core.cache.ResourceManager;
import eu.smesec.cysec.platform.core.utils.LocaleUtils;
import eu.smesec.cysec.platform.core.json.MValueAdapter;
import eu.smesec.cysec.platform.core.messages.CoachMsg;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.text.StringEscapeUtils;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@DenyAll
@Path("rest/coaches")
public class Coaches {
  @Context
  ServletContext context;
  private final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private final Gson gson = new GsonBuilder().registerTypeAdapter(Mvalue.class, new MValueAdapter()).create();
  @Inject
  private CacheAbstractionLayer cal;
  @Inject
  private ResourceManager res;

  /**
   * Instantiates a new coach.
   * Triggers Library.onBegin().
   *
   * @param id The id of the coach
   * @return response
   */
  @POST
  @Path("/{id}/instantiate")
  public Response instantiateCoach(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      Questionnaire coach = cal.getCoach(id);
      if (coach != null) {
        cal.instantiateCoach(companyId, coach);
        // set context for answer file per company
        CoachLibrary library = cal.getLibrariesForQuestionnaire(id).get(0);
        library.onBegin(FQCN.fromString(id));
        return Response.status(200).build();
      }
      return Response.status(404).build();
    } catch (CacheException ce) {
      logger.log(Level.SEVERE, "Error instantiating coach", ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Resumes a coach.
   * Triggers Library.onResume().
   *
   * @param id The if of the coach
   * @return response
   */
  @POST
  @Path("/{id}/resume")
  public Response resumeCoach(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      context.setAttribute("fqcn", id);
      FQCN fqcn = FQCN.fromString(id);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      library.onResume(fqcn.getCoachId(), fqcn);
      // update last selected
      LastSelected lastSelected = new LastSelected(fqcn.toString());
      cal.setMetadataOnAnswers(companyId, LibCal.FQCN_COMPANY, MetadataUtils.toMd(lastSelected));
      return Response.status(200).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the first question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/first")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFirst(@PathParam("id") String id) {
    try {
      FQCN fqcn = FQCN.fromString(id);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question first = library.getFirstQuestion();
      return Response.status(200).entity(gson.toJson(first)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the last question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/last")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getLast(@PathParam("id") String id) {
    try {
      FQCN fqcn = FQCN.fromString(id);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question last = library.getLastQuestion();
      return Response.status(200).entity(gson.toJson(last)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the current question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/current")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCurrent(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      FQCN fqcn = FQCN.fromString(id);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question question = library.getFirstQuestion();
      Metadata md = cal.getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_STATE);
      if (md != null) {
        State state = MetadataUtils.fromMd(md, State.class);
        question = cal.getQuestion(fqcn.getCoachId(), state.getResume());
      }
      return Response.status(200).entity(gson.toJson(question)).build();
    } catch (CacheException ce) {
      logger.log(Level.SEVERE, "Error occured", ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Updates an answers of an instantiated coach.
   *
   * @param id    The fully qualified coach name
   * @param qid   The id of the question
   * @param value new answer value
   * @return response
   */
  @POST
  @Path("/{id}/answers/{qid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateAnswer(
      @PathParam("id") String id, @PathParam("qid") String qid, String value) {
    String companyId = context.getAttribute("company").toString();
    String userId = context.getAttribute("user").toString();
    try {
      FQCN fqcn = FQCN.fromString(id);
      String coachId = fqcn.getCoachId();
      Questionnaire coach = cal.getCoach(coachId);
      if (coach == null) {
        logger.warning("cannot find coach " + coachId);
        return Response.status(404).build();
      }
      CoachLibrary library = cal.getLibrariesForQuestionnaire(coachId).get(0);

      value = StringEscapeUtils.escapeHtml4(value);

      // check question exists
      Question question = cal.getQuestion(coachId, qid);
      if (question == null) {
        logger.warning("cannot find question " + qid);
        return Response.status(404).build();
      }
      // handle answer, danger for race-condition for deselect
      synchronized (Coaches.class) {
        Answer answer = cal.getAnswer(companyId, fqcn, qid);
        String after = "";
        String before = "";
        if (answer != null) {
          // update existing
          if (question.getType().startsWith("Astar")) {
            // Create new List as Arrays.asList is unmodifiable
            List<String> options;
            if (answer.getAidList() == null) {
              options = new ArrayList<>();
              // before is already empty, no changing required
            } else {
              options = new ArrayList<>(Arrays.asList(answer.getAidList().split(" ")));
              before = options.toString();
            }
            if (options.contains(value)) {
              options.remove(value);
            } else {
              options.add(value);
            }
            String newOptions = String.join(" ", options);
            after = newOptions;
            answer.setAidList(newOptions);

          } else {
            before = answer.getText();
            after = value;
          }
          answer.setText(value); // update the Text field. Indicates last given option
          cal.updateAnswer(companyId, fqcn, answer);
        } else {
          // leave "before on initial empty value"
          // create new Answer
          answer = new Answer();
          answer.setQid(question.getId());
          answer.setText(value);
          if (question.getType().startsWith("Astar")) {
            answer.setAidList(value);
            after = answer.getQid();
          } else {
            after = value;
          }
          cal.createAnswer(companyId, fqcn, answer);
        }

        // Add audit entry
        // Make sure to use empty string if "before" or "after" is null
        Audit audit = AuditUtils.createAudit(
            userId,
            UserAction.MODIFIED,
            before == null ? "" : before,
            after == null ? "" : after);
        cal.createAuditLog(companyId, audit);

        library.onResponseChange(question, answer, fqcn);
      }
      Question next = library.getNextQuestion(question, fqcn);
      List<Question> active = library.peekQuestions(question);

      JsonObject data = new JsonObject();
      String url = next != null
          ? "/app/coach.jsp?fqcn=" + fqcn.toString() + "&question=" + next.getId()
          : res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
              ? "/api/rest/resources/"
                  + fqcn.getCoachId()
                  + "/"
                  + library.getId()
                  + "/assets/jsp/summary.jsp"
              : "/app";
      data.addProperty("next", url);

      // todo active
      return Response.status(200).entity(data.toString()).build();

    } catch (ElementAlreadyExistsException | ElementNotFoundException se) {
      logger.warning(se.getMessage());
      return Response.status(409).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Renders a question.
   *
   * @param id         The id of the coach
   * @param questionId the id of the question
   * @return rendered question as html
   */
  @GET
  @Path("/{id}/questions/{qid}/render")
  @Produces(MediaType.TEXT_HTML)
  public Response renderQuestion(@PathParam("id") String id, @PathParam("qid") String questionId) {
    String companyId = context.getAttribute("company").toString();
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
    try {
      FQCN fqcn = FQCN.fromString(id);
      context.setAttribute("fqcn", id);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question question = cal.getQuestion(fqcn.getCoachId(), questionId, locale);
      if (question == null) {
        return Response.status(404).build();
      }
      // update state
      State state = new State(questionId, null);
      cal.setMetadataOnAnswers(companyId, fqcn, MetadataUtils.toMd(state));

      // summary page
      String summaryUrl = res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
          ? "/api/rest/resources/"
              + fqcn.getCoachId()
              + "/"
              + library.getId()
              + "/assets/jsp/summary.jsp"
          : "/app";

      // next page
      Question next = library.getNextQuestion(question, fqcn);
      String nextUrl = next != null
          ? "/app/coach.jsp?fqcn=" + fqcn.toString() + "&question=" + next.getId()
          : summaryUrl;

      // question states for pagination
      List<Question> peakQuestions = library.peekQuestions(question);
      List<AbstractMap.SimpleEntry<Question, Answer>> actives = peakQuestions.stream()
          .map(q -> {
            try {
              return new AbstractMap.SimpleEntry<>(q, cal.getAnswer(companyId, fqcn, q.getId()));
            } catch (CacheException e) {
              return new AbstractMap.SimpleEntry<Question, Answer>(q, null);
            }
          }).collect(Collectors.toList());

      CoachMsg msg = new CoachMsg(locale);
      Map<String, Object> model = new HashMap<>();
      Answer answer = cal.getAnswer(companyId, fqcn, questionId);
      model.put("msg", msg.getMessages());
      model.put("question", question);
      model.put("answer", answer);
      model.put("fqcn", id);
      model.put("next", nextUrl);
      model.put("summary", summaryUrl);
      model.put("actives", actives);
      model.put("aidList", answer != null && answer.getAidList() != null
          ? Arrays.asList(answer.getAidList().split(" "))
          : Arrays.asList());
      return Response.status(200).entity(new Viewable("/coaching/coach", model)).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  // @GET
  // @Path("/{id}/pagination/{qid}/render")
  // @Produces(MediaType.TEXT_HTML)
  // public Response renderPagination(@PathParam("id") String id,
  // @PathParam("qid") String questionId) {
  // String companyId = context.getAttribute("company").toString();
  // try {
  // FQCN fqcn = FQCN.fromString(id);
  // context.setAttribute("fqcn", id);
  // Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
  // Question question = cal.getQuestion(fqcn.getCoachId(), questionId);
  // if (question == null) {
  // return Response.status(404).build();
  // }
  // Answer answer = cal.getAnswer(companyId, fqcn, questionId);
  // // update state
  // State state = new State(questionId, null);
  // cal.setMetadataOnAnswers(companyId, fqcn, MetadataUtils.toMd(state));
  //
  // Map<String, Object> model = new HashMap<>();
  // model.put("question", question);
  // model.put("answer", answer);
  // model.put("fqcn", id);
  // model.put("next", library.getNextQuestion(question, fqcn).getId());
  // model.put("active", library.peekQuestions(question));
  // return Response.status(200).entity(new Viewable("/coaching/coach",
  // model)).build();
  // } catch (CacheException ce) {
  // logger.severe(ce.getMessage());
  // return Response.status(400).build();
  // } catch (Exception e) {
  // logger.log(Level.SEVERE, "Error occured", e);
  // }
  // return Response.status(500).build();
  // }
}
