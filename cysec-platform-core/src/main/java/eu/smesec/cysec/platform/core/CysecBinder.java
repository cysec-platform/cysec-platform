/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core;

import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.ResourceManager;
import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.config.CysecConfig;
import eu.smesec.cysec.platform.core.services.MailServiceImpl;

import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class CysecBinder extends AbstractBinder {
  private ServletContext context;

  public CysecBinder(@Context ServletContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    try {
      Config config = CysecConfig.getDefault();
      // Instantiate Singletons
      ResourceManager resManager =
          new ResourceManager(Paths.get(context.getRealPath(ApplicationConfig.JSP_TEMPLATE_HOME)));
      CacheAbstractionLayer cal = new CacheAbstractionLayer(context, resManager, config);
      MailServiceImpl mailService = new MailServiceImpl();
      // bind Singletons
      bind(cal).to(CacheAbstractionLayer.class);
      bind(resManager).to(ResourceManager.class);
      bind(mailService).to(MailServiceImpl.class);
    } catch (Exception e) {
      throw new RuntimeException("Error in binding configuration");
    }
  }
}
