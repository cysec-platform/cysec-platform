/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.CacheNotFoundException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.LibCal;
import eu.smesec.cysec.platform.core.auth.Secured;
import eu.smesec.cysec.platform.core.json.FieldsExclusionStrategy;
import eu.smesec.cysec.platform.core.json.MValueAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

/** Data endpoint to provide data for external services. */
@Secured
@Path("rest/data")
public class Data {
  @Context ServletContext context;
  private final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private final Gson gson =
      new GsonBuilder()
          .addSerializationExclusionStrategy(
              new FieldsExclusionStrategy(MetadataUtils.MV_ENDURANCE_STATE))
          .registerTypeAdapter(Mvalue.class, new MValueAdapter())
          .create();
  @Inject private CacheAbstractionLayer cal;

  /**
   * Delivers rating of a questionnaire in JSON format.
   *
   * @param id question id
   * @return A JSON object that contains <code>Metadata</code> from the coach
   */
  @GET
  @Path("/{id}/answers")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswers(@PathParam("id") String id) {
    Object attribute = context.getAttribute("company");
    if (attribute == null) {
      throw new NotFoundException();
    }
    String companyId = attribute.toString();

    try {
      FQCN fqcn = FQCN.fromString(id);
      List<Answer> answers = cal.getAllAnswers(companyId, fqcn);
      return Response.ok().entity(gson.toJson(answers)).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(404).build();
    } catch (IllegalArgumentException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Delivers rating of a questionnaire in JSON format.
   *
   * @param id question id
   * @return A JSON object that contains <code>Metadata</code> from the coach
   */
  @GET
  @Path("/{id}/rating")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRating(@PathParam("id") String id) {
    Object attribute = context.getAttribute("company");
    if (attribute == null) {
      throw new NotFoundException();
    }
    String companyId = attribute.toString();

    try {
      FQCN fqcn = FQCN.fromString(id);
      Metadata metadata = cal.getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_RATING);
      if (metadata == null) {
        return Response.status(200).entity("[]").build();
      }
      return Response.ok().entity(gson.toJson(metadata.getMvalue())).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(404).build();
    } catch (IllegalArgumentException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Delivers all recommendations of a questionnaire in JSON format.
   *
   * @return A JSON object that contains <code>Metadata</code> from the coach
   */
  @GET
  @Path("/recommendations")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecommendations() {
    Object attribute = context.getAttribute("company");
    if (attribute == null) {
      throw new NotFoundException();
    }
    String companyId = attribute.toString();

    try {
      List<Metadata> metadata =
          cal.getAllMetadataOnAnswer(companyId, LibCal.FQCN_COMPANY, MetadataUtils.MD_RECOMMENDED);
      return Response.ok().entity(gson.toJson(metadata)).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(404).build();
    } catch (IllegalArgumentException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Delivers the skills of a company in JSON format.
   *
   * @param id question id
   * @return A JSON object that contains the Skills <code>Metadata</code>
   */
  @GET
  @Path("/{id}/skills")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSkills(@PathParam("id") String id) {
    Object attribute = context.getAttribute("company");
    if (attribute == null) {
      throw new NotFoundException();
    }
    String companyId = attribute.toString();

    try {
      FQCN fqcn = FQCN.fromString(id);
      Metadata skills = cal.getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_SKILLS);
      if (skills == null) {
        return Response.ok().entity("[]").build();
      }
      return Response.ok().entity(gson.toJson(skills.getMvalue())).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(404).build();
    } catch (IllegalArgumentException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Returns a list of all instantiated coaches as JSON.
   *
   * @return A JSON object that contains all instantiated coaches
   */
  @GET
  @Path("/coaches")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCoaches() {
    Object attribute = context.getAttribute("company");
    if (attribute == null) {
      throw new NotFoundException();
    }
    String companyId = attribute.toString();
    try {
      // load coach names
      Map<String, String> coaches = new HashMap<>();
      for (Questionnaire coach : cal.getAllCoaches()) {
        coaches.put(coach.getId(), coach.getReadableName());
      }
      // list instantiated coaches
      Map<String, String> instantiated =
          cal.listInstantiatedCoaches(companyId).stream()
              .map(
                  fqcn -> {
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
      return Response.ok().entity(gson.toJson(instantiated)).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(404).build();
    } catch (IllegalArgumentException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
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
