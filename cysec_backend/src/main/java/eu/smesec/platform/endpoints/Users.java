package eu.smesec.platform.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.bridge.execptions.ElementNotFoundException;
import eu.smesec.bridge.generated.Company;
import eu.smesec.platform.auth.CryptPasswordStorage;
import eu.smesec.platform.auth.Secured;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.json.FieldsExclusionStrategy;
import eu.smesec.platform.messages.UsersMsg;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;

import java.util.*;
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

import eu.smesec.platform.utils.LocaleUtils;
import eu.smesec.platform.utils.Validator;
import org.glassfish.jersey.server.mvc.Viewable;

@Secured
@DenyAll
@Path("rest/users")
public class Users {
  static Logger logger = Logger.getLogger("LoggingFeature.DEFAULT_LOGGER_NAME");

  @Inject
  private CacheAbstractionLayer cal;
  @Context
  ServletContext context;

  private static Gson addUserGson = new GsonBuilder()
        .addDeserializationExclusionStrategy(new FieldsExclusionStrategy("id", "token"))
        .create();
  private static Gson getUserGson = new GsonBuilder()
        .addSerializationExclusionStrategy(new FieldsExclusionStrategy("password"))
        .create();
  private static Gson updateUserGson = new GsonBuilder()
        .create();


  /***
   * <p>Fetches all users of a given company and
   * returns an HTML representation using templates/user.html.</p>
   * @return An html table with all users
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

  /***
   * <p>Fetches all users of a given company and
   * returns an HTML representation using templates/user.html.</p>
   * @return An html table with all users
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
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(400).build();
    } catch (ElementAlreadyExistsException aee) {
      logger.log(Level.WARNING, aee.getMessage(), aee);
      return Response.status(409).build();
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Failed to create user", e);
    }
    return Response.status(500).build();
  }

  /***
   * <p>Fetch a particular user of a given company.</p>
   *
   * @return The <code>User</code> representation of the requested user.
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
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

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
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(400).build();
    } catch (ElementNotFoundException | ElementAlreadyExistsException ee) {
      logger.log(Level.WARNING, ee.getMessage(), ee);
      return Response.status(409).build();
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Failed to create user", e);
    }
    return Response.status(500).build();
  }

  /***
   * <p>Fetches all users of a given company and
   * returns an HTML representation using templates/user.html.</p>
   * @return An html table with all users
   */
  @DELETE
  @Path("/{id}")
  @RolesAllowed("Admin")
  public Response deleteUser(@PathParam("id") long userId) {
    String companyId = context.getAttribute("company").toString();
    try {
      logger.log(Level.INFO, "delete user user " + userId);
      cal.removeUser(companyId, userId);
      return Response.status(204).build();
    } catch (CacheNotFoundException nfe) {
      logger.log(Level.WARNING, nfe.getMessage(), nfe);
      return Response.status(400).build();
    } catch (ElementNotFoundException aee) {
      logger.log(Level.WARNING, aee.getMessage(), aee);
      return Response.status(404).build();
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Failed to delete user", e);
    }
    return Response.status(500).build();
  }
}
