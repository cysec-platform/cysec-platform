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

import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.ResourceManager;
import eu.smesec.cysec.platform.core.utils.FileResponse;

import java.nio.file.Paths;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourcesTest extends JerseyTest {
  private final static String RESOURCE_PATH = "rest/resources";
  private CacheAbstractionLayer cal;
  private ResourceManager resManager;

  @Override
  protected Application configure() {
    //Mocks to be injected
    cal = Mockito.mock(CacheAbstractionLayer.class);
    resManager = Mockito.mock(ResourceManager.class);

    ResourceConfig config = new ResourceConfig(Resources.class);
    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(cal).to(CacheAbstractionLayer.class);
        bind(resManager).to(ResourceManager.class);
      }
    });
    return config;
  }

  @Test
  public void testResourceFound() {
    String coachId = "lib-company";
    String library = "eu.smesec.library.FirstLibrary";
    String filename = "file.png";
    FileResponse data = new FileResponse(new byte[0]);

    try {
      Mockito.when(resManager.getResource(Paths.get(coachId, library, filename))).thenReturn(data);
      Response res = target(RESOURCE_PATH + "/{coach}/{library}/" + filename)
              .resolveTemplate("coach", coachId)
              .resolveTemplate("library", library)
              .request(MediaType.APPLICATION_OCTET_STREAM).get();
      Assert.assertEquals(200, res.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testResourceNotFound() {
    String coachId = "lib-company";
    String library = "eu.smesec.library.FirstLibrary";
    String filename = "filenotfound.png";

    Response res = target(RESOURCE_PATH + "/{coach}/{library}/" + filename)
            .resolveTemplate("coach", coachId)
            .resolveTemplate("library", library)
            .request(MediaType.APPLICATION_OCTET_STREAM).get();

    Assert.assertEquals(404, res.getStatus());
  }
}
