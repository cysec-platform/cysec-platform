package eu.smesec.platform.auth.strategies;

import eu.smesec.bridge.execptions.LockedExpetion;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;

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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.Collections;

public class HeaderAuthStrategyTest {
    private static final String cysec_header_username = "oidc_claim_preferred_username";
    private static final String cysec_header_email = "oidc_claim_email";
    private static final String cysec_header_company = "oidc_claim_company";
    private static final String cysec_header_firstname = "oidc_claim_given_name";
    private static final String cysec_header_lastname = "oidc_claim_family_name";
    private static final String cysec_header_locale = "oidc_claim_locale";

    private HeaderAuthStrategy authStrategy;

    private ServletContext context;
    private CacheAbstractionLayer cal;
    private Config config;

    @Before
    public void setup() {
        context = PowerMockito.mock(ServletContext.class, Mockito.CALLS_REAL_METHODS);
        cal = PowerMockito.mock(CacheAbstractionLayer.class);
        config = PowerMockito.mock(Config.class);

        PowerMockito.when(context.getContextPath()).thenReturn("/cysec");
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_NAME))
                .thenReturn(cysec_header_username);
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_MAIL))
                .thenReturn(cysec_header_email);
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_COMPANY))
                .thenReturn(cysec_header_company);
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_FIRSTNAME))
                .thenReturn(cysec_header_firstname);
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_LASTNAME))
                .thenReturn(cysec_header_lastname);
        PowerMockito.when(config.getStringValue("cysec", HeaderAuthStrategy.OIDC_LOCALE))
                .thenReturn(cysec_header_locale);

        authStrategy = new HeaderAuthStrategy(cal, config, context);
    }

    @Test
    public void testHeaders() {
        String[] headerNames = new String[]{
                cysec_header_username,
                cysec_header_email,
                cysec_header_company
        };
        Assert.assertArrayEquals(headerNames, authStrategy.getHeaderNames().toArray());
    }

    @Test
    public void testAuthentication() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
        try {
            User user = PowerMockito.mock(User.class);
            PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
            PowerMockito.when(user.getLocale()).thenReturn(null);
            PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

            Assert.assertTrue(authStrategy.authenticate(headers, Resource.class.getMethod("get")));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testAuthenticationAdmin() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
        try {
            User user = PowerMockito.mock(User.class);
            PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
            PowerMockito.when(user.getRole()).thenReturn(Collections.singletonList("admin"));
            PowerMockito.when(user.getLocale()).thenReturn(null);
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
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
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
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
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
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
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
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_company, "company");
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
    public void testMissingHeaders() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "");
        headers.add(cysec_header_company, "company");
        try {
            User user = PowerMockito.mock(User.class);
            PowerMockito.when(user.getLock()).thenReturn(Locks.NONE);
            PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);

            authStrategy.authenticate(headers, Resource.class.getMethod("get"));
            Assert.fail();
        } catch (BadRequestException fe) {
            Assert.assertTrue(fe.getMessage().startsWith("missing oidc fields"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void testCreateNewCompany() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(cysec_header_username, "user");
        headers.add(cysec_header_email, "user@example.com");
        headers.add(cysec_header_firstname, "Thomas");
        headers.add(cysec_header_company, "company");
        try {
            User user = PowerMockito.mock(User.class);
            PowerMockito.when(user.getLock()).thenReturn(Locks.LOCKED);
            PowerMockito.when(cal.getUserByName("company", "user")).thenReturn(user);
            PowerMockito.when(cal.existsCompany("company")).thenReturn(false);
            // do nothing on company creation
            PowerMockito.doNothing().when(cal).createCompany("company", "company", user);
            authStrategy.authenticate(headers, Resource.class.getMethod("get"));

        } catch (LockedExpetion le) {
            Assert.assertEquals("User user is currently locked: LOCKED", le.getMessage());
            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void testCreateNewUserInExistingCompany() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add(cysec_header_username, "user2");
        headers.add(cysec_header_email, "user2@example.com");
        headers.add(cysec_header_firstname, "Hank");
        headers.add(cysec_header_company, "company");
        try {
            User user = PowerMockito.mock(User.class);
            PowerMockito.when(user.getLock()).thenReturn(Locks.LOCKED);
            PowerMockito.when(cal.existsCompany("company")).thenReturn(true);
            PowerMockito.when(cal.getUserByName("company", "user2")).thenReturn(user);
            // do nothing on company creation
            PowerMockito.doNothing().when(cal).createCompany("company", "company", user);
            authStrategy.authenticate(headers, Resource.class.getMethod("get"));
            // New users remain in locked state
            Assert.fail();
        } catch (LockedExpetion le) {
            Assert.assertEquals("User user2 is currently locked: LOCKED", le.getMessage());
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
