package eu.smesec.platform.endpoints;

import eu.smesec.bridge.generated.User;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.services.MailServiceImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SignUpTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/signUp";
  private ServletContext context;
  private CacheAbstractionLayer cal;
  private MailServiceImpl mailService;

  @Override
  protected Application configure() {
    //Mocks to be injected
    context = Mockito.mock(ServletContext.class);
    cal = Mockito.mock(CacheAbstractionLayer.class);
    mailService = Mockito.mock(MailServiceImpl.class);


    //test env resource config
    ResourceConfig config = new ResourceConfig(SignUp.class);
    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(context).to(ServletContext.class);
        bind(cal).to(CacheAbstractionLayer.class);
        bind(mailService).to(MailServiceImpl.class);
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

  @Test
  public void testSignUpUser() {
    String companyId = "fhnw";
    String json = "{\"username\":\"test\", \"password\":\"secret\",\"email\":\"test@example.com\",\"firstname\":\"test\",\"surname\":\"test\"}";

    try {
      Response res = target(RESOURCE_PATH + "/user")
          .queryParam("company", companyId)
          .request().post(Entity.entity(json, MediaType.APPLICATION_JSON));
      Assert.assertEquals(200, res.getStatus());
      Mockito.verify(cal, Mockito.times(1))
          .createUser(Mockito.eq(companyId), Mockito.any(User.class));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testSignUpCompany() {
    String companyId = "fhnw";
    String companyName = "fhnw";
    String json = "{\"username\":\"test\", \"password\":\"secret\",\"email\":\"test@example.com\",\"firstname\":\"test\",\"surname\":\"test\"}";

    try {
      Response res = target(RESOURCE_PATH + "/company")
          .queryParam("id", companyId)
          .queryParam("name", companyName)
          .request().post(Entity.entity(json, MediaType.APPLICATION_JSON));
      Assert.assertEquals(200, res.getStatus());
      Mockito.verify(cal, Mockito.times(1))
          .createCompany(Mockito.eq(companyId), Mockito.eq(companyName), Mockito.any(User.class));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}