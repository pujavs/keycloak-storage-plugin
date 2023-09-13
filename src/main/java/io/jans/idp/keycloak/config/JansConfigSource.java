package io.jans.idp.keycloak.config;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JansConfigSource implements ConfigSource {

    private static Logger LOG = LoggerFactory.getLogger(JansConfigSource.class);
    //private static final String FILE_CONFIG = "jans-application.properties";
    private static final String FILE_CONFIG = "jans-keycloak.properties";
    private Properties properties = null;
    Map<String, String> propertiesMap = new HashMap<>();

    public JansConfigSource() {
        this.loadProperties();
    }

    @Override
    public Map<String, String> getProperties() {
        LOG.debug("\n\n Getting properties \n\n");
        return propertiesMap;
    }

    @Override
    public Set<String> getPropertyNames() {
        LOG.debug("\n\n Getting Property Names \n\n");
        try {
            return properties.stringPropertyNames();

        } catch (Exception e) {
            LOG.error("\n\n Unable to read properties from file: " + FILE_CONFIG, e);
        }
        return Collections.emptySet();
    }

    @Override
    public int getOrdinal() {
        return 800;
    }

    @Override
    public String getValue(String name) {
        LOG.debug("\n\n JansConfigSource()::getValue() - name:{}", name);
        try {
            return properties.getProperty(name);
        } catch (Exception e) {
            LOG.error("\n\n Unable to read properties from file: " + FILE_CONFIG, e);
        }

        return null;
    }

    @Override
    public String getName() {
        return FILE_CONFIG;
    }

    private Properties loadProperties() {
		LOG.debug("\n\n JansConfigSource()::loadProperties() \n\n\n");
		

        // Load the properties file
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try ( InputStream inputStream = loader.getResourceAsStream(FILE_CONFIG)) { 
            properties = new Properties();
            properties.load(inputStream);
            properties.stringPropertyNames().stream().forEach(key -> propertiesMap.put(key, properties.getProperty(key)));
			LOG.debug("\n\n JansConfigSource()::loadProperties() - properties :{} {}", properties,"\n\n\n");
            return properties;
        } catch (Exception e) {
			LOG.error("\n\n **2**  Failed to load configuration from : " + FILE_CONFIG, e);
            throw new WebApplicationException("Failed to load configuration from "+ FILE_CONFIG, e);
        }
    }

}
