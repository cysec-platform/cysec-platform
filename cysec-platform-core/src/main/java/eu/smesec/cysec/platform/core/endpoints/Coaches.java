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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.cysec.platform.bridge.execptions.ElementNotFoundException;
import eu.smesec.cysec.platform.bridge.generated.*;
import eu.smesec.cysec.platform.bridge.md.LastSelected;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.md.State;
import eu.smesec.cysec.platform.bridge.utils.AuditUtils;
import eu.smesec.cysec.platform.core.auth.Secured;
import eu.smesec.cysec.platform.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.LibCal;
import eu.smesec.cysec.platform.core.cache.ResourceManager;
import eu.smesec.cysec.platform.core.helpers.subcoach.SubcoachHelper;
import eu.smesec.cysec.platform.core.utils.FileResponse;
import eu.smesec.cysec.platform.core.utils.LocaleUtils;
import eu.smesec.cysec.platform.core.json.MValueAdapter;
import eu.smesec.cysec.platform.core.messages.CoachMsg;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
      logger.log(Level.SEVERE, "Error occurred", e);
    }
    return Response.status(500).build();
  }

  /**
   * Resumes a coach.
   * Triggers Library.onResume().
   *
   * @param id The id of the coach
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

      // Resume subcoaches
      List<SubcoachHelper.InstantiatorData> instantiators = SubcoachHelper.getAllInstantiatorsInCoach(companyId, fqcn, cal);
      for (SubcoachHelper.InstantiatorData instantiator : instantiators) {
        for (SubcoachInstances.SubcoachInstance instance : instantiator.getInstances()) {
          CoachLibrary subcoachLibrary = cal.getLibrariesForQuestionnaire(instantiator.getSubcoachId()).get(0);
          FQCN subcoachFqcn = FQCN.from(fqcn.getRootCoachId(), instantiator.getSubcoachId(), instance.getInstanceName());
          subcoachLibrary.onResume(instance.getInstanceName(), subcoachFqcn);
          subcoachLibrary.setParent(library.getContext());
        }
      }

      // update last selected
      LastSelected lastSelected = new LastSelected(fqcn.toString());
      cal.setMetadataOnAnswers(companyId, LibCal.FQCN_COMPANY, MetadataUtils.toMd(lastSelected));
      return Response.status(200).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occurred", e);
    }
    return Response.status(500).build();
  }

  /**
   * Resets a coach to its default state.
   *
   * @param id The fqcn of the coach
   * @return response
   */
  @POST
  @Path("/{id}/reset")
  public Response reset(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    context.setAttribute("fqcn", id);
    FQCN fqcn = FQCN.fromString(id);
    try {
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Questionnaire coach = cal.getCoach(fqcn.getCoachId());

      // Remove coach
      cal.finalizeCoach(companyId, fqcn.toPath().getParent());

      // And then instantiate it again
      cal.instantiateCoach(companyId, coach);
      library.onBegin(FQCN.fromString(id));

      return Response.status(200).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occurred", e);
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
      Question first = library.getFirstQuestion(fqcn.getName());
      return Response.status(200).entity(gson.toJson(first)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occurred", e);
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
      Question question = cal.getCurrentQuestion(companyId, fqcn);
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

      String unescapedValue = value;
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
          } else if (question.getType().equals("subcoachInstantiator")) {
            Set<SubcoachInstances.SubcoachInstance> instancesBefore = answer.getSubcoachInstances() == null
                    ? new HashSet<>()
                    : new HashSet<>(answer.getSubcoachInstances().getSubcoachInstance());

            // Parse the JSON and update the answer
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
            Map<String, String> subcoachInstanceData = new ObjectMapper().readValue(unescapedValue, typeRef);

            SubcoachInstances instances = new SubcoachInstances();
            subcoachInstanceData.forEach((key, val) -> {
              SubcoachInstances.SubcoachInstance instance = new SubcoachInstances.SubcoachInstance();
              instance.setInstanceName(key);
              instance.setParentArgument(val);
              instances.getSubcoachInstance().add(instance);
            });

            answer.setSubcoachInstances(instances);

            // Now let's actually instantiate the subcoaches
            Set<SubcoachInstances.SubcoachInstance> instancesNow = new HashSet<>(instances.getSubcoachInstance());
            Set<SubcoachInstances.SubcoachInstance> addedInstances = instancesNow
                    .stream()
                    .filter(i -> !instancesBefore.contains(i))
                    .collect(Collectors.toSet());
            Set<SubcoachInstances.SubcoachInstance> removedInstances = instancesBefore
                    .stream()
                    .filter(i -> !instancesNow.contains(i))
                    .collect(Collectors.toSet());

            // instantiate newly added instances
            for (SubcoachInstances.SubcoachInstance instance : addedInstances) {
              FQCN subcoachFqcn = FQCN.from(fqcn.getRootCoachId(), question.getSubcoachId(), instance.getInstanceName());
              Questionnaire subcoach = cal.getCoach(question.getSubcoachId());
              Metadata metadata = new Metadata();
              metadata.setKey("subcoach-data");
              Mvalue parentArgument = MetadataUtils.createMvalueStr("parent-argument", instance.getParentArgument());
              Mvalue subcoachInstantiatorId = MetadataUtils.createMvalueStr("subcoach-instantiator-id", question.getId());
              metadata.getMvalue().add(parentArgument);
              metadata.getMvalue().add(subcoachInstantiatorId);
              Set<String> selectors = new HashSet<>(Collections.singletonList(instance.getInstanceName()));

              // Create and resume coach
              cal.instantiateSubCoach(companyId, fqcn, subcoach, selectors, metadata);
              CoachLibrary subcoachLibrary = cal.getLibrariesForQuestionnaire(subcoachFqcn.getCoachId()).get(0);
              subcoachLibrary.onResume(subcoachFqcn.getCoachId(), subcoachFqcn);
            }

            // remove all instances that were removed by user
            for (SubcoachInstances.SubcoachInstance instance : removedInstances) {
              FQCN subcoachFqcn = FQCN.from(fqcn.getRootCoachId(), question.getSubcoachId(), instance.getInstanceName());

              try {
                // We need to wrap this call in a try-catch to make sure the answer is actually updated even
                // if the coach file does not exist for whatever reason
                cal.removeSubCoach(companyId, subcoachFqcn);
              } catch (CacheException e) {
                logger.warning(e.getMessage());
              }
            }
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

          // If we are in a parent coach that potentially has subcoaches, we
          // want to find the subcoaches that were activated during this answer
          // update so we can reevaluate the subcoaches. To find the new subcoches
          // we compare the visible subcoach placeholders before and after running
          // the response change logic
          if (fqcn.isTopLevel()) {
            List<Question> subcoachesBeforeUpdate = library.peekQuestions(question).stream()
                    .filter(q -> Objects.equals(q.getType(), "subcoach")).collect(Collectors.toList());
            library.onResponseChange(question, answer, fqcn);
            List<Question> newSubCoaches = library.peekQuestions(question).stream()
                    .filter(q -> Objects.equals(q.getType(), "subcoach"))
                    .filter(q -> !subcoachesBeforeUpdate.contains(q))
                    .collect(Collectors.toList());

            for (Question newSubCoach : newSubCoaches) {
              FQCN subcoachFqcn = FQCN.fromString(String.format("%s.%s.%s", fqcn.getRootCoachId(), newSubCoach.getSubcoachId(), newSubCoach.getInstanceName()));
              CoachLibrary subcoachLibrary = cal.getLibrariesForQuestionnaire(subcoachFqcn.getCoachId()).get(0);
              subcoachLibrary.onResume(subcoachFqcn.getCoachId(), subcoachFqcn);
            }
          } else {
            // If we are in a subcoach we can simply run the response change logic
            library.onResponseChange(question, answer, fqcn);
          }
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
   * Updates the flagged state of a given Question.
   */
  @POST
  @Path("/{id}/questions/{qid}/flag")
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateFlaggedState(@PathParam("id") String id, @PathParam("qid") String questionId, String value) {
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

      boolean newFlaggedState = Boolean.parseBoolean(value);

      // check question exists
      Question question = cal.getQuestion(coachId, questionId);
      if (question == null) {
        logger.warning("cannot find question " + questionId);
        return Response.status(404).build();
      }

      cal.flagQuestion(companyId, fqcn, questionId, newFlaggedState);

      // todo active
      return Response.status(204).build();

    }catch (CacheException ce) {
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

      FQCN parentFqcn = fqcn.isTopLevel() ? fqcn : fqcn.getParent();
      CoachLibrary parentLibrary = cal.getLibrariesForQuestionnaire(fqcn.getRootCoachId()).get(0);
      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);

      // If we switch to another instance of the same coach we have to resume it first
      if (!library.getActiveInstance().equals(fqcn.getName())) {
        library.setActiveInstance(fqcn.getName());
        library.onResume(questionId, fqcn);
      }

      Question question = cal.getQuestion(fqcn.getCoachId(), questionId, locale);
      if (question == null) {
        return Response.status(404).build();
      }
      // update state
      State state = new State(questionId, null);
      cal.setMetadataOnAnswers(companyId, fqcn, MetadataUtils.toMd(state));
      Optional<String> subcoachInstantiatorId = SubcoachHelper.getSubcoachInstantiatorId(companyId, fqcn, cal);
      if (subcoachInstantiatorId.isPresent()) {
        Answer answer = cal.getAnswer(companyId, fqcn.getParent(), subcoachInstantiatorId.get());
        answer.setCurrentSubcoachInstance(fqcn.getName());
        cal.updateAnswer(companyId, fqcn.getParent(), answer);
      }

      // summary page
      String summaryUrl = res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
          ? "/api/rest/resources/"
              + parentFqcn.getCoachId()
              + "/"
              + parentLibrary.getId()
              + "/assets/jsp/summary.jsp"
          : "/app";

      // endpoint will determine the next question when called
      String nextUrl = "/api/rest/coaches/" + fqcn + "/questions/" + questionId + "/next";


      // fetch data for pagination
      List<Question> questions = parentLibrary.peekQuestions(question);
      List<Tuple<FQCN, Question>> actives = SubcoachHelper.insertSubcoachQuestions(companyId, parentFqcn, cal, questions);
      Map<Tuple<FQCN, Question>, Answer> answers = actives
              .stream()
              .map(tup -> {
                  try {
                      return new AbstractMap.SimpleEntry<>(tup, cal.getAnswer(companyId, tup.getFirst(), tup.getSecond().getId()));
                  } catch (CacheException e) {
                      return new AbstractMap.SimpleEntry<Tuple<FQCN, Question>, Answer>(tup, null);
                  }
              }).filter(e -> e.getValue() != null)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      Map<String, Boolean> flagStatus = actives
              .stream()
              .collect(Collectors.toMap(q -> q.getFirst() + ":" + q.getSecond().getId(), q -> {
                  try {
                      return cal.isQuestionFlagged(companyId, q.getFirst(), q.getSecond().getId());
                  } catch (CacheException e) {
                      return false;
                  }
              }));

      CoachMsg msg = new CoachMsg(locale);
      Map<String, Object> model = new HashMap<>();
      Answer answer = cal.getAnswer(companyId, fqcn, questionId);
      model.put("libJspModel", library.getJspModel(""));
      model.put("msg", msg.getMessages());
      model.put("question", question);
      model.put("answer", answer);
      model.put("fqcn", id);
      model.put("next", nextUrl);
      model.put("summary", summaryUrl);
      model.put("actives", actives);
      model.put("answers", answers);
      model.put("flagStatus", flagStatus);
      model.put("flagStatusKey", fqcn + ":" + question.getId()); // Needed for Flag Status lookup to work.
      model.put("aidList", answer != null && answer.getAidList() != null
          ? Arrays.asList(answer.getAidList().split(" "))
          : Arrays.asList());
      if (question.getType().equals("subcoachInstantiatorOutlet")) {
          model.put("subcoachFqcn", SubcoachHelper.getFirstFqcn(companyId, fqcn, cal, question.getSubcoachInstantiatorId()).orElse(null));
      }

      return Response.status(200).entity(new Viewable("/coaching/coach", model)).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occurred", e);
    }
    return Response.status(500).build();
  }

  /**
   * Routing to the next question based on the current question and its state.
   * 
   * @param id                The id of the coach
   * @param currentQuestionId The id of the current question
   * @return Redirecting to the next question
   */
  @GET()
  @Path("/{id}/questions/{qid}/next")
  public Response nextQuestion(@PathParam("id") String id, @PathParam("qid") String currentQuestionId) {
    String companyId = context.getAttribute("company").toString();
    FQCN fqcn = FQCN.fromString(id);
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());

    try {
      Question question = cal.getQuestion(fqcn.getCoachId(), currentQuestionId, locale);
      if (question == null) {
        return Response.status(404).build();
      }

      CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);

      String summaryUrl = res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
          ? "/api/rest/resources/"
              + fqcn.getCoachId()
              + "/"
              + library.getId()
              + "/assets/jsp/summary.jsp"
          : "/app";

      Question next = library.getNextQuestion(question, fqcn);


      String nextUrl;
      if (next == null) { // Are we at the end of the questionnaire?
        // Let's check if we are in a subcoach managed through an instantiator
        Optional<String> subcoachInstantiatorId = SubcoachHelper.getSubcoachInstantiatorId(companyId, fqcn, cal);
        if (subcoachInstantiatorId.isPresent()) {
          Optional<FQCN> nextSubcoachInstance = SubcoachHelper.getNextSubcoachInstance(companyId, fqcn, cal);
          if (nextSubcoachInstance.isPresent()) {
            // We are in a subcoach and there is a next subcoach instance
            nextUrl = context.getContextPath() + "/app/coach.jsp?fqcn=" + nextSubcoachInstance.get() + "&question=_first";
          } else {
            // In this case we are at the end of the subcoach instantiator outlet, so we go to the next question in the
            // parent coach after the outlet
            Optional<String> outletQuestionId = SubcoachHelper.getFirstOutletQuestionId(companyId, fqcn, cal);
            nextUrl = outletQuestionId
                    .map(s -> context.getContextPath() + "/api/rest/coaches/" + fqcn.getParent() + "/questions/" + s + "/next")
                    .orElseGet(() -> context.getContextPath() + "/app/coach.jsp?fqcn=" + fqcn.getParent() + "&question=_first");
          }
        } else {
          // If are in the root coach, we go to the summary page when the questionnaire is finished
          nextUrl = context.getContextPath() + summaryUrl;
        }
      } else {
        nextUrl = context.getContextPath() + "/app/coach.jsp?fqcn=" + fqcn + "&question=" + next.getId();
      }
      
      return Response.seeOther(URI.create(nextUrl)).build();
    } catch (CacheException e) {
      logger.log(Level.SEVERE, "Error occurred", e);
      return Response.status(500).build();
    }
  }

  /**
   * Export all coach (and sub coaches) data by exporting the stored answers
   * file(s).
   *
   * @param id The id of the coach
   * @return   Coach data as zip archive
   */
  @Secured
  @RolesAllowed("Admin")
  @GET()
  @Path("{id}/export")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportCoach(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      FileResponse coachZip = cal.zipCoach(companyId, id);
      return Response.status(200).entity(coachZip).build();
    } catch (CacheException e) {
      logger.log(Level.SEVERE, "Error while exporting coach data as zip", e);
      return Response.status(500).build();
    }
  }


  /**
   * Import all coach (and sub coaches) data by <b>overwriting</b> the stored answers.
   *
   * @param id              The id of the coach.
   * @param zipUploadStream Coach data as zip (expected to match the file system structure).
   * @param fileData        File metadata.
   * @return
   */
  @Secured
  @RolesAllowed("Admin")
  @POST
  @Path("{id}/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response importCoach(
      @PathParam("id") String id,
      @FormDataParam("file") InputStream zipUploadStream,
      @FormDataParam("file") FormDataContentDisposition fileData) {
        String companyId = context.getAttribute("company").toString();
        FQCN fqcn = FQCN.fromString(id);
        String coachId = fqcn.getCoachId();

        try {
          cal.unzipCoach(companyId, coachId, zipUploadStream);
        } catch (CacheException e) {
          logger.log(Level.SEVERE, "Error while importing coach data from zip", e);
          return Response.status(500).build();
        }

        return Response.status(200).build();
  }
}
