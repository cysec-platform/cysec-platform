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

import eu.smesec.cysec.platform.bridge.utils.AuditUtils;
import java.time.LocalDateTime;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.services.MailService;
import eu.smesec.cysec.platform.core.services.MailServiceImpl;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Locks;
import eu.smesec.cysec.platform.bridge.generated.Token;
import eu.smesec.cysec.platform.bridge.generated.User;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class PasswordResetTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/resetPassword";
  private CacheAbstractionLayer cal;
  private ServletContext context;
  private MailService mailService;

  private User user;

  @Override
  protected Application configure() {
    //Mocks to be injected
    cal = Mockito.mock(CacheAbstractionLayer.class);
    context = Mockito.mock(ServletContext.class);
    mailService = Mockito.mock(MailServiceImpl.class);

    //test env resource config
    ResourceConfig config = new ResourceConfig(PasswordForgottenService.class);
    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(cal).to(CacheAbstractionLayer.class);
        bind(context).to(ServletContext.class);
        bind(Mockito.mock(MailServiceImpl.class)).to(MailServiceImpl.class);
      }
    });

    return config;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp(); //must be called to start test server
    Mockito.reset(cal);

    String comp = "fhnw";
    Set<String> companies = new TreeSet<>();
    companies.add(comp);

    user = new User();
    user.setUsername("jbauer");
    user.setFirstname("Jack");
    user.setSurname("Bauer");
    user.setEmail("jack.bauer@ctu.gov");
    user.setPassword("SpeakFriendandEnter");
    user.getRole();//create empty role list
    user.setLock(Locks.NONE);
  }


  @Test
  public void testCreateTokenSuccessful() throws CacheException {
    String email = "john-doe@company.tld";
    String company = "fhnw";
    List<User> userList = new ArrayList<>();
    userList.add(user);

    userList.add(user);
      when(cal.getUserByEmail(company, email)).thenReturn(user);

    Response res = target(RESOURCE_PATH + "/create")
            .queryParam("email", email)
            .queryParam("company", company)
            .request()
            .post(Entity.json(""));

    Assert.assertEquals(200, res.getStatus());
  }

  @Test
  public void testResetSuccessful() throws CacheException {
    String email = "john-doe@company.tld";
    String company = "fhnw";
    String password1 = "asdf1234!";
    String password2 = "asdf1234!";
    List<User> userList = new ArrayList<>();
    userList.add(user);
    userList.add(user);
    Token token = new Token();
    token.setId("f063bfa969a6b57122f4e42e15cc0f82");
    //create new token for user
    token.setExpiry(AuditUtils.toXmlGregorianCalendar(LocalDateTime.now().plusDays(1)));
    user.getToken().add(token);

      when(cal.existsCompany(company)).thenReturn(true);
      when(cal.getAllUsers(company)).thenReturn(userList);

    Response res = target(RESOURCE_PATH + "/verifyToken/")
            .path(token.getId())
            .queryParam("password1", password1)
            .queryParam("password2", password2)
            .queryParam("email", email)
            .queryParam("company", company)
            .request()
            .post(Entity.json(""));

    Assert.assertEquals(200, res.getStatus());
  }

  @Test
  @Ignore
  public void testFailBadPassword() {
    String password1 = "asdf1234!";
    String password2 = "asdf1234!";
    String company = "fhnw";
    Token token = new Token();

    Response res = target(RESOURCE_PATH + "/verifyToken/")
            .path(token.getId())
            .queryParam("password1", company)
            .queryParam("password2", company)
            .queryParam("company", company)
            .request()
            .post(Entity.text(""));

    Assert.assertEquals(200, res.getStatus());
  }
}
