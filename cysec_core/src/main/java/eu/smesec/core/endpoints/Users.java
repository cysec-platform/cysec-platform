package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.bridge.execptions.ElementNotFoundException;
import eu.smesec.bridge.generated.Company;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.auth.Secured;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.core.messages.UsersMsg;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.core.utils.Validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

@Secured
@DenyAll
@Path("rest/users")
public class Users {
  private static final Logger logger = Logger.getLogger("LoggingFeature.DEFAULT_LOGGER_NAME");
  private static final Gson addUserGson =
      new GsonBuilder()
          .addDeserializationExclusionStrategy(new FieldsExclusionStrategy("id", "token"))
          .create();
  private static final Gson getUserGson =
      new GsonBuilder()
          .addSerializationExclusionStrategy(new FieldsExclusionStrategy("password"))
          .create();
  private static final Gson updateUserGson = new GsonBuilder().create();

  @Inject private CacheAbstractionLayer cal;
  @Context ServletContext context;

  /**
   * Renders the user table.
   *
   * @return rendered user table
   */
  @GET
  @Path("/render")
  @Produces(MediaType.TEXT_HTML)
  public Response getRenderedUsers() {
    String companyId = (String) context.getAttribute("company");
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
    try {
      String replicaToken = cal.getCompanyReplicaToken(companyId);
      Company company = cal.getCompany(companyId);
      List<User> users = company.getUser();
      UsersMsg msg = new UsersMsg(locale, users.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("users", users);
      model.put("replica", replicaToken);
      model.put("locales", Arrays.asList("en", "de"));
      model.put("locks", Locks.values());
      return Response.status(200).entity(new Viewable("/users/users.jsp", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }

  /**
   * Creates a new user.
   *
   * @param json user data
   * @return response
   */
  @POST
  @RolesAllowed("Admin")
  public Response createUser(String json) {
    if (json == null) {
      logger.log(Level.WARNING, "user json is null");
      return Response.status(400).build();
    }
    String companyId = context.getAttribute("company").toString();
    try {
      User newUser = addUserGson.fromJson(json, User.class);
      if (!Validator.validateUser(newUser)) {
        logger.log(Level.WARNING, "user has invalid attributes");
        return Response.status(400).build();
      }
      logger.log(Level.INFO, "Hashing and salting the password");
      String password = newUser.getPassword();
      CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password, null);
      newUser.setPassword(passwordStorage.getPasswordStorage());
      newUser.setLock(Locks.PENDING);
      cal.createUser(companyId, newUser);
      return Response.status(200).entity(newUser.getId()).build();
    } catch (CacheNotFoundException nfe) {
      //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      logger.log(Level.WARNING, nfe.getMessage());
      return Response.status(400).build();
    } catch (ElementAlreadyExistsException aee) {
      //      logger.log(Level.WARNING, aee.getMessage(), aee);
      logger.log(Level.WARNING, aee.getMessage());
      return Response.status(409).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to create user", e);
    }
    return Response.status(500).build();
  }

  /**
   * Returns an user as json.
   *
   * @param userId The id of the user
   * @return json
   */
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@PathParam("id") long userId) {
    String companyId = (String) context.getAttribute("company");
    try {
      User user = cal.getUser(companyId, userId);
      if (user != null) {
        return Response.status(200).entity(getUserGson.toJson(user)).build();
      }
      return Response.status(404).build();
    } catch (CacheNotFoundException nfe) {
      //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      logger.log(Level.WARNING, nfe.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Updates an existing user.
   *
   * @param userId The id of the user
   * @param json The new user data, the data must contain unchanged data as well!
   * @return response
   */
  @PUT
  @Path("/{id}")
  @RolesAllowed("Admin")
  public Response updateUser(@PathParam("id") long userId, String json) {
    if (json == null) {
      logger.log(Level.WARNING, "user json is null");
      return Response.status(400).build();
    }
    String companyId = context.getAttribute("company").toString();
    try {
      User newUser = updateUserGson.fromJson(json, User.class);
      newUser.setId(userId);
      if (!Validator.validateUser(newUser)) {
        logger.log(Level.WARNING, "user has invalid attributes");
        return Response.status(400).build();
      }
      //      if (newUser.getPassword() != null) {
      //        logger.log(Level.INFO, "Hashing and salting the password");
      //        String password = newUser.getPassword();
      //        CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password, null);
      //        newUser.setPassword(passwordStorage.getPasswordStorage());
      //      }
      cal.updateUser(companyId, newUser);
      return Response.status(200).entity(newUser.getId()).build();
    } catch (CacheNotFoundException nfe) {
      //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      logger.log(Level.WARNING, nfe.getMessage());
      return Response.status(400).build();
    } catch (ElementNotFoundException | ElementAlreadyExistsException ee) {
      //      logger.log(Level.WARNING, ee.getMessage(), ee);
      logger.log(Level.WARNING, ee.getMessage());
      return Response.status(409).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to create user", e);
    }
    return Response.status(500).build();
  }

  /**
   * Deletes an existing user.
   *
   * @param userId The id of the user
   * @return response
   */
  @DELETE
  @Path("/{id}")
  @RolesAllowed("Admin")
  public Response deleteUser(@PathParam("id") long userId) {
    String companyId = context.getAttribute("company").toString();
    try {
      cal.removeUser(companyId, userId);
      return Response.status(204).build();
    } catch (CacheNotFoundException nfe) {
      //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      logger.log(Level.WARNING, nfe.getMessage());
      return Response.status(400).build();
    } catch (ElementNotFoundException aee) {
      //      logger.log(Level.WARNING, aee.getMessage(), aee);
      logger.log(Level.WARNING, aee.getMessage());
      return Response.status(404).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to delete user", e);
    }
    return Response.status(500).build();
  }
}
