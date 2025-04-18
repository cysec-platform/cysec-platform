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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.services.MailServiceImpl;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.User;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class ResetPasswordTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/resetPassword";

  private ServletContext context;
  private CacheAbstractionLayer cal;
  private MailServiceImpl mailService;

  private Set<String> fakeSet;
  private User jdoe;

  @Override
  protected Application configure() {
    //Mocks to be injected
    context = Mockito.mock(ServletContext.class);
    cal = Mockito.mock(CacheAbstractionLayer.class);
    mailService = Mockito.mock(MailServiceImpl.class);

    //test env resource config
    ResourceConfig config = new ResourceConfig(PasswordForgottenService.class);
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

    fakeSet = new TreeSet<>();
    fakeSet.add("password");
    fakeSet.add("audit");

    jdoe = new User();
    jdoe.setFirstname("John");
    jdoe.setSurname("Doe");
    jdoe.setEmail("jdoe@fhnw.cd");
  }

  @Ignore
  @Test
  public void testGetForm() {
    when(cal.getCompanyIds()).thenReturn(fakeSet);
    Response res = target(RESOURCE_PATH).request().get();
    Assert.assertEquals(200, res.getStatus());
  }

  @Test
  public void testCreateTokenSuccess() {
    when(context.getAttribute("company")).thenReturn("fhnw");
    try {
      when(cal.existsCompany("fhnw")).thenReturn(true);
      when(cal.getUserByEmail(anyString(), anyString())).thenReturn(jdoe);
    } catch (CacheException ce) {
      ce.printStackTrace();
      Assert.fail();
    }
    Response res = target(RESOURCE_PATH + "/create")
          .queryParam("email", "jdoe@fhnw.tld")
          .queryParam("company", "fhnw")
          .request().post(null);
    Assert.assertEquals(200, res.getStatus());
  }

  @Test
  public void testCreateTokenFailure() throws CacheException {
    when(context.getAttribute("company")).thenReturn("fhnw");
    when(cal.existsCompany("fhnw")).thenReturn(true);
    when(cal.getUserByEmail(anyString(), anyString())).thenReturn(jdoe);
    Response res = target(RESOURCE_PATH + "/create")
          .queryParam("email", "jdoe@fhnw.tld")
          .queryParam("company", "fhnw")
          .request().post(null);
    Assert.assertEquals(500, res.getStatus());
  }

  @Test
  public void testVerifyNoUserFound() throws CacheException {
    when(context.getAttribute("company")).thenReturn("fhnw");
    when(cal.existsCompany("fhnw")).thenReturn(true);
    when(cal.getUserByEmail(anyString(), anyString())).thenReturn(jdoe);
    Response res = target(RESOURCE_PATH)
          .path("not_found")
          .queryParam("password1", "password")
          .queryParam("password2", "password")
          .request().post(null);
    Assert.assertEquals(404, res.getStatus());
  }


}
