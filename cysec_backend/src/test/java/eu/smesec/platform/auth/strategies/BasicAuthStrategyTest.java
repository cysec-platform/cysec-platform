package eu.smesec.platform.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.LockedExpetion;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.platform.auth.CryptPasswordStorage;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;

import java.util.Collections;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BasicAuthStrategy.class)
public class BasicAuthStrategyTest {
  private BasicAuthStrategy authStrategy;

  private ServletContext context;
  private CacheAbstractionLayer cal;
  private CryptPasswordStorage passwordStorage;
  private Config config;

  @Before
  public void setup() {
    context = PowerMockito.mock(ServletContext.class, Mockito.CALLS_REAL_METHODS);
    cal = PowerMockito.mock(CacheAbstractionLayer.class);
    passwordStorage = PowerMockito.mock(CryptPasswordStorage.class);
    config = PowerMockito.mock(Config.class);

    authStrategy = new BasicAuthStrategy(cal, config, context);
  }

  @Test
  public void testHeaders() {
    String[] headerNames = new String[] {
        "authorization"
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
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidHeader() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "lkuvlujgvhl");
    try {
      authStrategy.authenticate(headers, null);
      Assert.fail();
    } catch (BadRequestException e) {
      Assert.assertEquals("invalid auth header", e.getMessage());
    } catch (CacheException e) {
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidHeader2() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic dGVzdHVzZXI6cGFzc3dvcmQ=");
    try {
      authStrategy.authenticate(headers, null);
      Assert.fail();
    } catch (BadRequestException e) {
      Assert.assertEquals("invalid auth format: testuser:password", e.getMessage());
    } catch (CacheException e) {
      Assert.fail();
    }
  }

  @Test
  public void testAuthentication() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      PowerMockito.whenNew(CryptPasswordStorage.class).withAnyArguments().thenReturn(passwordStorage);
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
      PowerMockito.when(user.getLocale()).thenReturn(null);
      PowerMockito.when(passwordStorage.verify("password")).thenReturn(true);
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

      Assert.assertTrue(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationEmail() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyQGV4YW1wbGUuY29tOnBhc3N3b3Jk");
    try {
      PowerMockito.whenNew(CryptPasswordStorage.class).withAnyArguments().thenReturn(passwordStorage);
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
      PowerMockito.when(user.getLocale()).thenReturn(null);
      PowerMockito.when(passwordStorage.verify("password")).thenReturn(true);
      PowerMockito.when(cal.getUserByEmail("company", "user@example.com")).thenReturn(user);

      Assert.assertTrue(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationAdmin() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      PowerMockito.whenNew(CryptPasswordStorage.class).withAnyArguments().thenReturn(passwordStorage);
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
      PowerMockito.when(user.getRole()).thenReturn(Collections.singletonList("admin"));
      PowerMockito.when(user.getLocale()).thenReturn(null);
      PowerMockito.when(passwordStorage.verify("password")).thenReturn(true);
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

      Assert.assertTrue(authStrategy.authenticate(headers, Resource.class.getMethod("getAdmin")));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationNonUser() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(null);
      authStrategy.authenticate(headers, Resource.class.getMethod("get"));
      Assert.fail();
    } catch (BadRequestException e) {
      Assert.assertEquals("User user not found in comapny company", e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationLockedPending() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.PENDING);
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

      authStrategy.authenticate(headers, Resource.class.getMethod("get"));
      Assert.fail();
    } catch (LockedExpetion le) {
      Assert.assertEquals("User user is currently locked: PENDING", le.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationLockedLocked() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.LOCKED);
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

      authStrategy.authenticate(headers, Resource.class.getMethod("get"));
      Assert.fail();
    } catch (LockedExpetion le) {
      Assert.assertEquals("User user is currently locked: LOCKED", le.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationForbidden() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
      PowerMockito.when(user.getRole()).thenReturn(Collections.emptyList());
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

      authStrategy.authenticate(headers, Resource.class.getMethod("getAdmin"));
      Assert.fail();
    } catch (ForbiddenException fe) {
      Assert.assertEquals("user user does not have one of the required roles [admin]", fe.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationPasswordMismatch() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic Y29tcGFueS91c2VyOnBhc3N3b3Jk");
    try {
      PowerMockito.whenNew(CryptPasswordStorage.class).withAnyArguments().thenReturn(passwordStorage);
      User user = PowerMockito.mock(User.class);
      PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
      PowerMockito.when(passwordStorage.verify("password")).thenReturn(false);
      PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

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
