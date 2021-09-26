package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.ElementNotFoundException;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UserTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/users";
  private ServletContext context;
  private CacheAbstractionLayer cal;

  @Override
  protected Application configure() {
    //Mocks to be injected
    context = Mockito.mock(ServletContext.class);
    cal = Mockito.mock(CacheAbstractionLayer.class);

    //test env resource config
    ResourceConfig config = new ResourceConfig(Users.class);
    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(context).to(ServletContext.class);
        bind(cal).to(CacheAbstractionLayer.class);
      }
    });

    return config;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp(); //must be called to start test server
    Mockito.reset(context);
    Mockito.reset(cal);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testCreateUser() {
    String companyId = "fhnw";
    long userId = 1000L;
    User user = new User();
    user.setId(userId);
    user.setUsername("test");
    user.setEmail("test@example.com");
    user.setPassword("hashed password");
    user.setFirstname("test");
    user.setSurname("test");
    user.setLock(Locks.NONE);
    String json = "{\"username\":\"test\", \"password\":\"secret\",\"email\":\"test@example.com\",\"firstname\":\"test\",\"surname\":\"test\"}";

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doNothing().when(cal).updateUser(Mockito.eq(companyId), Mockito.any(User.class));
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().put(Entity.entity(json, MediaType.APPLICATION_JSON));
      Assert.assertEquals(200, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testGetUser() {
    String companyId = "fhnw";
    long userId = 1000L;
    User user = new User();
    user.setId(userId);
    user.setUsername("test");
    user.setEmail("test@example.com");
    user.setPassword("hashed password");
    user.setFirstname("test");
    user.setSurname("test");
    user.setLock(Locks.NONE);

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doReturn(user).when(cal).getUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().get();
      Assert.assertEquals(200, res.getStatus());
      String json = res.readEntity(String.class);
      Assert.assertEquals("{\"id\":1000,\"username\":\"test\",\"email\":\"test@example.com\",\"firstname\":\"test\",\"surname\":\"test\",\"lock\":\"NONE\"}", json);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testGetUserMissingCompany() {
    String companyId = "fhnw";
    long userId = 1000L;

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doThrow(new CacheNotFoundException("Company " + companyId + " not found"))
              .when(cal).getUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().get();
      Assert.assertEquals(400, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testGetMissingUser() {
    String companyId = "fhnw";
    long userId = 1000L;

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doReturn(null).when(cal).getUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().get();
      Assert.assertEquals(404, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * FIXME validator
   */

  @Ignore
  @Test
  public void testUpdateUser() {
    String companyId = "fhnw";
    long userId = 1000L;
    User user = new User();
    user.setId(userId);
    user.setUsername("test");
    user.setEmail("test@example.com");
    user.setPassword("hashed password");
    user.setFirstname("test");
    user.setSurname("test");
    user.setLock(Locks.NONE);
    String json = "{\"lock\":\"PENDING\"}";

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doNothing().when(cal).updateUser(Mockito.eq(companyId), Mockito.any(User.class));
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().put(Entity.entity(json, MediaType.APPLICATION_JSON));
      Assert.assertEquals(200, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testDeleteUser() {
    String companyId = "fhnw";
    long userId = 1000L;

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doNothing().when(cal).removeUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().delete();
      Assert.assertEquals(204, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testDeleteUserMissingCompany() {
    String companyId = "missing-company";
    long userId = 1000L;

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doThrow(new CacheNotFoundException("Company " + companyId + " not found"))
              .when(cal).removeUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().delete();
      Assert.assertEquals(400, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testDeleteMissingUser() {
    String companyId = "fhnw";
    long userId = 1000L;

    try {
      Mockito.doReturn(companyId).when(context).getAttribute("company");
      Mockito.doThrow(new ElementNotFoundException("User " + userId + " not found"))
              .when(cal).removeUser(companyId, userId);
      Response res = target(RESOURCE_PATH + "/{id}")
          .resolveTemplate("id", userId)
          .request().delete();
      Assert.assertEquals(404, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
