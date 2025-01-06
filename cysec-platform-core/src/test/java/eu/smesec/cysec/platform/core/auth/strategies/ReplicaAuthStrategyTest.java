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
package eu.smesec.cysec.platform.core.auth.strategies;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.utils.TokenUtils;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenUtils.class)
@PowerMockIgnore({"javax.xml.*"})
public class ReplicaAuthStrategyTest {
  private ReplicaAuthStrategy authStrategy;

  private ServletContext context;
  private CacheAbstractionLayer cal;
  private Config config;

  @Before
  public void setup() {
    context = PowerMockito.mock(ServletContext.class, Mockito.CALLS_REAL_METHODS);
    cal = PowerMockito.mock(CacheAbstractionLayer.class);
    config = PowerMockito.mock(Config.class);
    PowerMockito.mockStatic(TokenUtils.class);

    authStrategy = new ReplicaAuthStrategy(cal, config, context);
  }

  @Test
  public void testHeaders() {
    String[] headerNames = new String[] {
        ReplicaAuthStrategy.REPLICA_TOKEN_HEADER
    };
    Assert.assertArrayEquals(headerNames, authStrategy.getHeaderNames().toArray());
  }

  @Test
  public void testAuthenticationEmptyHeader() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    try {
      authStrategy.authenticate(headers, null);
      Assert.fail();
    } catch (BadRequestException e) {
      Assert.assertEquals("invalid auth header", e.getMessage());
    } catch (CacheException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidHeader() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "lkuvlujgvhl");
    try {
      authStrategy.authenticate(headers, null);
      Assert.fail();
    } catch (BadRequestException e) {
      Assert.assertEquals("company/token pattern does not match", e.getMessage());
    } catch (CacheException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticate() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company/testtoken");
    try {
      PowerMockito.when(cal.getCompanyReplicaToken("company")).thenReturn("testtoken");

      Assert.assertTrue(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticateFailed() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company/testtoken");
    try {
      PowerMockito.when(cal.getCompanyReplicaToken("company")).thenReturn("invalidToken");

      Assert.assertFalse(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticateNoCompanyToken() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, "company/testtoken");
    try {
      PowerMockito.when(cal.getCompanyReplicaToken("company")).thenReturn(null);

      Assert.assertFalse(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  // test endpoint to fake method annotation
  private static class Resource {
    @GET
    public String get() {
      return "GET";
    }

    @RolesAllowed("admin")
    public String getAdmin() {
      return "GET";
    }
  }
}
