package eu.smesec.platform.endpoints;

import eu.smesec.bridge.Library;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.bridge.execptions.CacheException;

import java.util.Collections;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourcesTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/resources";
  private CacheAbstractionLayer cal;

  @Override
  protected Application configure() {
    //Mocks to be injected

    cal = Mockito.mock(CacheAbstractionLayer.class);

    //test env resource config
    ResourceConfig config = new ResourceConfig(Resources.class);
    forceSet(TestProperties.CONTAINER_PORT, "0");

    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(cal).to(CacheAbstractionLayer.class);
        // bind(renderer).to(Renderer.class);
      }
    });
    return config;
  }

  @Test
  public void testResourceFound() throws CacheException {
    String filename = "sign-check-icon.png";
    Library mockLibrary = Mockito.mock(Library.class);
    Mockito.when(mockLibrary.getId()).thenReturn("eu.smesec.library.FirstLibrary");
    Mockito.when(cal.getLibrariesForQuestionnaire("fhnw"))
            .thenReturn(Collections.singletonList(mockLibrary));
    Mockito.when(mockLibrary.getResource(Collections.singletonList(filename)))
            .thenReturn(getClass().getResourceAsStream("/" + filename));

    Response res = target(RESOURCE_PATH + "/{coach}/{library}/" + filename)
            .resolveTemplate("coach", "fhnw")
            .resolveTemplate("library", "eu.smesec.library.FirstLibrary")
            .request(MediaType.APPLICATION_OCTET_STREAM).get();

    Assert.assertEquals(200, res.getStatus());
  }

  @Test
  public void testResourceNotFound() throws CacheException {
    String filename = "filenotfound.png"; //deliberately use non-existent file
    Library mockLibrary = Mockito.mock(Library.class);
    Mockito.when(mockLibrary.getId()).thenReturn("eu.smesec.library.FirstLibrary");
    Mockito.when(cal.getLibrariesForQuestionnaire("fhnw"))
            .thenReturn(Collections.singletonList(mockLibrary));
    Mockito.when(mockLibrary.getResource(Collections.singletonList(filename)))
            .thenReturn(getClass().getResourceAsStream("/" + filename));

    Response res = target(RESOURCE_PATH + "/{coach}/{library}/" + filename)
            .resolveTemplate("coach", "fhnw")
            .resolveTemplate("library", "eu.smesec.library.FirstLibrary")
            .request(MediaType.APPLICATION_OCTET_STREAM).get();

    Assert.assertEquals(404, res.getStatus());
  }
}
