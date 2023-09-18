package io.jans.idp.keycloak.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.keycloak.component.ComponentValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jans.idp.keycloak.util.Constants;

public class JansConfigSource implements ConfigSource {

    private static Logger LOG = LoggerFactory.getLogger(JansConfigSource.class);
    private String CONFIG_FILE_PATH = null;
    private static final String CONFIG_FILE_NAME = "jans-keycloak-storage-api.properties";
    private Properties properties = null;
    Map<String, String> propertiesMap = new HashMap<>();

    public JansConfigSource() {
        this.CONFIG_FILE_PATH = System.getProperty(Constants.JANS_CONFIG_PROP_PATH);
        LOG.info("\n\n this.CONFIG_FILE_PATH:{}", CONFIG_FILE_PATH);
    }

    public JansConfigSource(String configFilePath) {
        LOG.info("\n\n configFilePath:{}", configFilePath);

        if (StringUtils.isBlank(configFilePath)) {
            throw new ComponentValidationException("Cannot load config property file as path null !!!");
        }

        this.CONFIG_FILE_PATH = configFilePath;
        this.loadProperties();
    }

    @Override
    public Map<String, String> getProperties() {
        LOG.info("\n\n Getting properties \n\n");
        return propertiesMap;
    }

    @Override
    public Set<String> getPropertyNames() {
        LOG.debug("\n\n Getting Property Names \n\n");
        try {
            return properties.stringPropertyNames();

        } catch (Exception e) {
            LOG.error("\n\n Unable to read properties from file: " + CONFIG_FILE_NAME, e);
        }
        return Collections.emptySet();
    }

    @Override
    public int getOrdinal() {
        return 800;
    }

    @Override
    public String getValue(String name) {
        LOG.info("\n\n JansConfigSource()::getValue() - name:{}", name);
        try {
            return properties.getProperty(name);
        } catch (Exception e) {
            LOG.error("\n\n Unable to read properties from file: " + CONFIG_FILE_NAME, e);
        }

        return null;
    }

    @Override
    public String getName() {
        return CONFIG_FILE_NAME;
    }

    public String getQualifiedFileName() {
        String fileSeparator = FileSystems.getDefault().getSeparator();
        LOG.info("\n\n JansConfigSource()::getValue() - fileSeparator:{}", fileSeparator);
        return this.CONFIG_FILE_PATH + fileSeparator + CONFIG_FILE_NAME;
    }

    /*
     * private Properties loadProperties() {
     * LOG.debug("\n\n JansConfigSource()::loadProperties() \n\n\n"); LOG.
     * debug("\n\n JansConfigSource()::loadProperties() - getQualifiedFileName():{}"
     * , getQualifiedFileName());
     * 
     * // Load the properties file ClassLoader loader =
     * Thread.currentThread().getContextClassLoader(); try ( InputStream inputStream
     * = loader.getResourceAsStream(getQualifiedFileName())) { properties = new
     * Properties(); properties.load(inputStream);
     * properties.stringPropertyNames().stream().forEach(key ->
     * propertiesMap.put(key, properties.getProperty(key)));
     * LOG.debug("\n\n JansConfigSource()::loadProperties() - properties :{} {}",
     * properties,"\n\n\n"); return properties; } catch (Exception e) {
     * LOG.error("\n\n **2**  Failed to load configuration from : " + FILE_CONFIG,
     * e); throw new WebApplicationException("Failed to load configuration from "+
     * FILE_CONFIG, e); } }
     */

    private Properties loadProperties() {
        LOG.info("\n\n\n ***** JansConfigSource::loadProperties() - Properties form Config.Scope ");
        FileInputStream file = null;
        try {
            // Get file path
            String filePath = getQualifiedFileName();
            LOG.info("\n\n\n ***** JansConfigSource::loadProperties() - filePath:{}", filePath);

            if (StringUtils.isNotBlank(filePath)) {

                // load the file handle for main.properties
                file = new FileInputStream(filePath);
                LOG.info("\n\n\n ***** JansConfigSource::loadProperties() - file =" + file + "\n\n");
                ClassLoader loader = Thread.currentThread().getContextClassLoader();

                if (file != null) {
                    LOG.info("\n\n\n ***** RemoteUserStorageProviderFactory::readFile() - loading file \n\n");
                    // load all the properties from this file
                    properties = new Properties();
                    properties.load(file);
                    properties.stringPropertyNames().stream()
                            .forEach(key -> propertiesMap.put(key, properties.getProperty(key)));
                    LOG.debug("\n\n JansConfigSource()::loadProperties() - properties :{} {}", properties, "\n\n\n");
                    if (properties != null) {
                        printProperties(properties);
                    }
                    // we have loaded the properties, so close the file handle

                } else {
                    LOG.error("Config properties file is null!");
                    throw new ComponentValidationException("CConfig properties file is null!!!");
                }

                if (properties != null) {
                    printProperties(properties);
                } else {
                    LOG.error("Could not load config properties!");
                    throw new ComponentValidationException("Could not load config properties!!!");
                }

            } else {
                LOG.error("Property file is null!");
                throw new ComponentValidationException("Config property file is null!!!");
            }

        } catch (Exception ex) {
            LOG.error("Failed to load property file", ex);
            throw new ComponentValidationException("Failed to load property file!!!");
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
            }
        }
        return properties;
    }

    private static void printProperties(Properties prop) {
        prop.keySet().stream().map(key -> key + ": " + prop.getProperty(key.toString())).forEach(System.out::println);
    }

}
