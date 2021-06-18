package eu.smesec.platform;

import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.ResourceManager;
import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.platform.services.MailServiceImpl;

import java.nio.file.Paths;
import java.util.function.Supplier;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class CysecBinder extends AbstractBinder {
    private final ServletContext context;

    public CysecBinder(@Context ServletContext context) {
        this.context = context;
    }

    // TODO: Don't know if this will work with the suppliers.
    @Override
    protected void configure() {
        try {
            Supplier<Config> configSupplier = CysecConfig::getDefault;
            // Instantiate Singletons
            ResourceManager.init(() -> Paths.get(context.getRealPath(ApplicationConfig.JSP_TEMPLATE_HOME)));
            ResourceManager resManager = ResourceManager.getInstance();
            Supplier<ResourceManager> resManagerSupplier = () -> resManager;
            Supplier<String> contextPathSupplier = context::getContextPath;
            Supplier<String> company = () -> context.getAttribute("company").toString();
            CacheAbstractionLayer.init(contextPathSupplier, company, resManagerSupplier, configSupplier);
            CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();
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