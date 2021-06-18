package eu.smesec.core.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
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
@PrepareForTest(AdminAuthStrategy.class)
public class AdminModelAuthStrategyTest {
  private AdminAuthStrategy authStrategy;

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

    authStrategy = new AdminAuthStrategy(cal, config, context);
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
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("pwd");

      Assert.assertTrue(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidAdminPrefix() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_cysec_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("admin");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("pwd");

      Assert.assertFalse(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidAdmin() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("adminX adminY");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("pwdX pwdY");

      Assert.assertFalse(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidPwd() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("user admin");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("upw apw");

      Assert.assertFalse(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationInvalidPwdList() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("user admin");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("upw");

      Assert.assertFalse(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationPwdLTUsers() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("user admin client");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("upw pwd");


      Assert.assertTrue(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testAuthenticationPwdGTUsers() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("authorization", "Basic X2FkbWluXy9hZG1pbjpwd2Q=");
    try {
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PREFIX)).thenReturn("_admin_");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_NAMES)).thenReturn("user admin");
      PowerMockito.when(config.getStringValue(null, AdminAuthStrategy.ADMIN_PWS)).thenReturn("upw pwd apw");

      Assert.assertTrue(authStrategy.authenticate(headers, null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
