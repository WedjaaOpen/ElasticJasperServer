package net.wedjaa.jasper.elasticsearch.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * This class will provide the installation procedure
 * for the plugin.
 * 
 * @author Fabio MrWHO Torchetti <mrwho@wedjaa.net>
 *
 */
public class Installer {

	private class Library {

		private String name;
		private String version;
		private String fullPath;
		private String fileName;
		private boolean valid;

		public Library(String libraryFile) {
			Path libraryPath = Paths.get(libraryFile);
			fileName = libraryPath.getFileName().toString();
			fullPath = libraryFile;
			valid = getNameAndVersion(fileName);
		}

		private boolean getNameAndVersion(String filename) {
			Pattern pattern = Pattern.compile("^(.*)-([\\.0-9]+)(.[Ff]inal){0,1}\\.jar");
			Matcher matcher = pattern.matcher(filename);
			if(matcher.matches()) {
				name = matcher.group(1);
				version = matcher.group(2);
				return true;
			}
			return false;
		}

		@Override
		public boolean equals(Object otherObject) {
			
			boolean result = false;

			if (otherObject instanceof Library){
				Library otherLibrary = (Library) otherObject;
				if (!otherLibrary.isValid()) {
					return false;
				}
				String otherName = otherLibrary.getName();
				result = otherName.equals(this.getName());
			}

			return result;
		}


		public String getName() {
			return name;
		}


		public String getVersion() {
			return version;
		}

		public String getFullPath() {
			return fullPath;
		}

		public String getFileName() {
			return fileName;
		}

		public boolean isValid() {
			return valid;
		}

	}

	private static final String SRC_LIB_PATH = "libs";
	private static final String SRC_CFG_PATH = "config";
	private static final String ES_CONFIG = "applicationContext-es-datasource.xml";
	private static final String ES_PROPERTIES = "es_datasource.properties";
	private static final String WEBINF_PATH = "WEB-INF";
	private static final String BUNDLES_PATH = "bundles";
	private static final String CLASSES_PATH = "classes";
	private static final String LIB_PATH = "lib";
	private static final String JSERVER_PROPERTIES = "jasperreports.properties";
	private static final String JSERVER_REST_CONFIG = "applicationContext-rest-services.xml";
	private static final String ES_ADAPTER_PROPERTY = "net.sf.jasperreports.query.executer.factory.elasticsearch";
	private static final String ES_ADAPTER_FACTORY = "net.wedjaa.jasper.elasticsearch.query.ESQueryExecuterFactory";
	private static final String QUERY_LANGUAGES_ID = "queryLanguagesCe";
	private static final String LANGUAGE_ELEMENT = "value";
	private static final String ES_QUERY_LANGUAGE = "elasticsearch";

	// Success return codes
	public static final int SUCCESS = 0;
	public static final int UPGRADED = 1;

	// Error return codes
	public static final int WRONG_SOURCE = 2;
	public static final int WRONG_PATH = 3;
	public static final int PROPERTIES_UPDATE_FAILED = 4;
	public static final int ADAPTER_CONFIG_FAILED = 5;
	public static final int FAILED_LIBRARIES_INSTALL = 6;
	public static final int FAILED_CONFIGURATION_INSTALL = 7;


	private String JsHome;


	/**
	 * Creates an instance of the Installer class.
	 * 
	 * @param js_home	This is the path to the Jasper Server
	 * 				installation.
	 */
	public Installer(String JsHome) {
		this.JsHome = JsHome;
	}

	/**
	 * Joins two paths using the system path separator and
	 * resolving to the absolute path.
	 * 
	 * @param basePath	The path we want to add to.
	 * @param subPath		The path we want to join with the basePath.
	 * @return			The absolute path of the two joined paths.
	 */
	private String joinPaths(String basePath, String subPath) {
		Path path = Paths.get(basePath, subPath);
		return path.toAbsolutePath().toString();	  
	}

	/**
	 * 
	 * @return	The path to the WEB-INF folder.
	 */
	private String getWebinfPath() {
		return joinPaths(JsHome, WEBINF_PATH);
	}

	/**
	 * 
	 * @return	The path to the bundles folder.
	 */
	private String getBundlesPath() {
		return joinPaths(getWebinfPath(), BUNDLES_PATH);
	}

	/**
	 * 
	 * @return	The path to the classes folder.
	 */
	private String getClassesPath() {
		return joinPaths(getWebinfPath(), CLASSES_PATH);
	}

	/**
	 * 
	 * @return	The path to the libraries folder.
	 */
	private String getLibsPath() {
		return joinPaths(getWebinfPath(), LIB_PATH);
	}


	private String getRestConfigPath() {
		return joinPaths(getWebinfPath(), JSERVER_REST_CONFIG);
	}

	private String getJasperPropertiesPath() {
		return joinPaths(getClassesPath(), JSERVER_PROPERTIES);
	}

	/**
	 * This method checks for the existence of a path
	 * and - optionally - if that path is a directory.
	 * 
	 * @param path	The path we want to check.
	 * @param isDir	True if we expect the path to be a directory.
	 * @return		False is the path does not exists or it's not a
	 * 				directory and we were expecting one.
	 */
	private boolean pathExists(String path, boolean isDir) {
		File fileCheck = new File(path);
		boolean exists = fileCheck.exists();
		if (!isDir || !exists) {
			return exists;
		}
		return fileCheck.isDirectory();
	}

	/**
	 * Method used to check if a path exists.
	 * 
	 * @param path	The filesystem path to check.
	 * @return		True if the path exists, false if it is not.
	 */
	private boolean pathExists(String path) {
		return pathExists(path, false);
	}

	/**
	 * Method that checks is a path exists and it's a directory.
	 * 
	 * @param path	The filesystem path to check.
	 * @return		True if the path exists and it's a directory, false otherwise.
	 */
	private boolean directoryExists(String path) {
		return pathExists(path, true);
	}


	/**
	 * 
	 * @return true if the path is a Jasper Server installation, false if it's not.
	 */
	private boolean verifyServerPath() {
		// Verify that the base paths are in here
		if (!directoryExists(getWebinfPath())
				|| !directoryExists(getBundlesPath()) 
				|| !directoryExists(getClassesPath())
				|| !directoryExists(getLibsPath())
				) {
			System.out.println("Missing Jasper Server application paths.");
			return false;
		}

		if (!pathExists(getJasperPropertiesPath())
				|| !pathExists(getRestConfigPath())) {
			System.out.println("Missing Jasper Server configuration files.");
			return false;
		}

		return true;
	}

	/**
	 * Method that loads the properties of the Jasper Server
	 * 
	 * @return	The Jasper Server properties.
	 */
	private Properties loadProperties() {
		Properties properties = new Properties();
		File propertiesFile = new File(getJasperPropertiesPath());
		try {
			FileInputStream propertiesStream = new FileInputStream(propertiesFile);
			properties.load(propertiesStream);
			propertiesStream.close();
		} catch (Exception ex) {
			System.err.println("Error loading Jasper Server properties: " + ex.getLocalizedMessage());
		}
		return properties;
	}

	/**
	 * Method that saves the properties for Jasper Server
	 * 
	 * @param properties	The properties to save.
	 * 
	 * @return	True if the properties were successfully saved, false otherwise.
	 */
	private boolean saveProperties(Properties properties) {
		File propertiesFile = new File(getJasperPropertiesPath());
		boolean successful = false;
		try {
			FileOutputStream propertiesStream = new FileOutputStream(propertiesFile);
			properties.store(propertiesStream, "Jasper Modified Properties - ES Adapter Added");
			propertiesStream.close();
			successful = true;
		} catch (Exception ex) {
			System.err.println("Error saving Jasper Server properties: " + ex.getLocalizedMessage());
		}
		return successful;
	}

	/**
	 * Method that loads the Document representation of the REST adapters
	 * configuration file.
	 * 
	 * @return	REST Adapters configurations as a Document.
	 */
	private Document getRestConfiguration() {
		Document restConfiguration = null;
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		File restConfigFile = new File(getRestConfigPath());
		try {
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			FileInputStream restConfigStream = new FileInputStream(restConfigFile);
			// StreamSource xmlSource = new StreamSource(restConfigStream, "http://sampleApps.net/static/dataDef1.1.dtd");
			restConfiguration = documentBuilder.parse(restConfigStream);
			restConfigStream.close();
		} catch (Exception ex) {
			System.err.println("Error getting Jasper Server rest configuration: " + ex.getLocalizedMessage());
		}
		return restConfiguration;
	}

	/**
	 * This method saves the configuration of the REST adapters passed
	 * as a Document.
	 * 
	 * @param restConfiguration	A Document representation of the REST adapters configuration.
	 * 
	 * @return	True on successful save of the configuration, false otherwise.
	 * 
	 */
	private boolean saveRestConfiguration(Document restConfiguration) {
		boolean success = false;
		File restConfigFile = new File(getRestConfigPath());
		try {
			FileOutputStream restConfigStream = new FileOutputStream(restConfigFile);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource configSource = new DOMSource(restConfiguration);
			StreamResult streamSavingResult = new StreamResult(restConfigStream);
			transformer.transform(configSource, streamSavingResult);
			restConfigStream.close();
			success = true;
		} catch(Exception ex) {
			System.err.println("Error saving Jasper Server rest configuration: " + ex.getLocalizedMessage());
		}
		return success;
	}

	/**
	 * Method that checks if the Jasper Server configurations
	 * has already defined the adapter factory for the ES datasource.
	 * 
	 * @return	True if the configurations was loaded and the adapter defined, false otherwise.
	 */
	private boolean hasFactoryDefined() {
		// Check if we are upgrading or installing
		Properties jsProperties = loadProperties();
		if (jsProperties.containsKey(ES_ADAPTER_PROPERTY)
				&& jsProperties.getProperty(ES_ADAPTER_PROPERTY).equals(ES_ADAPTER_FACTORY) ) {
			return true;
		}
		return false;
	}

	private Node getNodeById(Document document, String id) {
		Node node = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			node = (Node) xpath.evaluate("//*[@id='" + id + "']", document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			System.err.println("Could not find node with id '"+ id +"'");
		}
		return node;
	}

	/**
	 * This method will return a list of String representing the query
	 * languages defined in the REST endpoints configuration.
	 * 
	 * @param restConfiguration	Document representing the REST adapters configuration.
	 * 
	 * @return	A List<String> of defined query languages
	 */
	private List<String> getQueryLanguages(Document restConfiguration) {
		List<String> foundLanguages = new ArrayList<String>();

		Node utilListNode = getNodeById(restConfiguration, QUERY_LANGUAGES_ID);

		if (utilListNode == null) {
			System.err.println("No query languages found");
			return foundLanguages;
		}

		NodeList utilList = utilListNode.getChildNodes();

		for ( int utilIdx=0; utilIdx<utilList.getLength(); utilIdx++) {
			Node util = utilList.item(utilIdx);
			foundLanguages.add(util.getTextContent());
		}

		return foundLanguages;
	}

	/**
	 * This method will check if the ES Datasource query language
	 * is defined in the current Jasper Server configuration.
	 * 
	 * @return	True if the configuration is successfully loaded and the query language is defined.
	 */
	private boolean hasRestEndpointAdded() {

		Document restConfiguration = getRestConfiguration();

		if (restConfiguration == null) {
			return false;
		}

		if ( getQueryLanguages(restConfiguration).contains(ES_QUERY_LANGUAGE)) {
			return true;
		}

		return false;
	}


	/**
	 * This method adds the ElasticSearch query factory to the
	 * Jasper Server properties.
	 * 
	 * @return	True if successful, False if it failed.
	 */
	private boolean addElasticQuery() {
		Properties currentProperties = loadProperties();
		currentProperties.setProperty(ES_ADAPTER_PROPERTY, ES_ADAPTER_FACTORY);
		if (!saveProperties(currentProperties)) {
			System.err.println("Failed to save the properties");
			return false;
		}
		return true;
	}

	/**
	 * This method adds the ElasticSearch query language to the
	 * Jasper Server REST adapter configuration.
	 * 
	 * @return	True if successful, False if it failed.
	 */
	private boolean addElasticRestEndpoint() {
		Document restConfiguration = getRestConfiguration();
		Node languages = getNodeById(restConfiguration, QUERY_LANGUAGES_ID);
		if (languages == null) {
			System.err.println("Failed to load existing languages - can't update.");
			return false;
		}
		Element esLanguage = restConfiguration.createElement(LANGUAGE_ELEMENT);
		esLanguage.setTextContent(ES_QUERY_LANGUAGE);
		languages.appendChild(esLanguage);
		if(!saveRestConfiguration(restConfiguration)) {
			System.err.println("Failed to save the new configuration.");
			return false;
		}
		return true;
	}

	/**
	 * Method that checks that the source path specified is a valid
	 * installation path.
	 * 
	 * @param sourcesPath	The source path of the installation
	 * 
	 * @return	True if the specified path is a valid installation source, false otherwise.
	 */
	private boolean checkInstallSources(String sourcesPath) {

		String configPath = joinPaths(sourcesPath, SRC_CFG_PATH);
		String libsPath = joinPaths(sourcesPath, SRC_LIB_PATH);
		if (!directoryExists(libsPath)
				|| !directoryExists(configPath)) {
			return false;
		}

		String es_config = joinPaths(configPath, ES_CONFIG);
		String es_props = joinPaths(configPath, ES_PROPERTIES);

		if (!pathExists(es_config)
				|| !pathExists(es_props) ) {
			return false;
		}

		return true;
	}

	private boolean installConfigurations(String sourcePath) {
		
		String configPath = joinPaths(sourcePath, SRC_CFG_PATH);
		String sourceConfig = joinPaths(configPath, ES_CONFIG);
		String dstConfig = joinPaths(getWebinfPath(), ES_CONFIG);
		String sourceProps = joinPaths(configPath, ES_PROPERTIES);
		String dstProps = joinPaths(getBundlesPath(), ES_PROPERTIES);
		
		try {
			Files.copy(Paths.get(sourceConfig), Paths.get(dstConfig)
					, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			Files.copy(Paths.get(sourceProps), Paths.get(dstProps)
					, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (Exception ex) {
			System.err.println("Error copying configuration files: " + ex.getLocalizedMessage());
			return false;
		}
		return true;
	}

	private List<Library> loadLibraries(String path) {
		List<Library>  libraries = new ArrayList<Library>();
		File libsPath = new File(path);
		for (final File fileEntry : libsPath.listFiles()) {
			// Ignore directories - we are shallow copying
			if (!fileEntry.isDirectory()) {
				libraries.add(new Library(fileEntry.getAbsolutePath()));
			}
		}
		return libraries;
	}

	private boolean installLibraries(String sourcePath) {

		String libsPath = joinPaths(sourcePath, SRC_LIB_PATH);
		List<Library> newLibraries = loadLibraries(libsPath);
		System.out.println("Installing " + newLibraries.size() + " libraries");

		List<Library> oldLibraries = loadLibraries(getLibsPath());

		for (Library current: newLibraries) {
			if (current.isValid()) {
				Library oldLibrary = null;
				if ( oldLibraries.indexOf(current) >= 0 ) {
					oldLibrary = oldLibraries.get(oldLibraries.indexOf(current));
				}
				String dstPath = joinPaths(getLibsPath(), current.getFileName());
				try {
					if (oldLibrary == null || 
							(oldLibrary != null && 
							!oldLibrary.getVersion().equals(current.getVersion())) ) {

						if (oldLibrary != null) {
							System.out.print("Replacing " + current.getName());
							System.out.print(" V" + oldLibrary.getVersion() + " with V");
							System.out.println(current.getVersion());
							File oldLibFile = new File(oldLibrary.getFullPath());
							oldLibFile.delete();
						}
						Files.copy(Paths.get(current.getFullPath()), Paths.get(dstPath)
								, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					}
				} catch( Exception ex) {
					System.err.println("Error installing the library: " + ex.getLocalizedMessage());
					return false;
				}
			} else {
				System.err.println("Invalid library found in sources: " + current.getFileName());
				return false;
			}
		}
		
		return true;
	}

	private int installFiles(String installPath) {
		
		if (!installLibraries(installPath)) {
			return FAILED_LIBRARIES_INSTALL;
		}
		
		if (!installConfigurations(installPath)) {
			return FAILED_CONFIGURATION_INSTALL;
		}
		
		return SUCCESS;
	}
	
	/**
	 * This method will execute an upgrade of an existing installation. The method
	 * is private because it is called from install when a previous installation
	 * is found.
	 * 
	 * @param installPath The source installations files path.
	 * 
	 * @return 0 on success, > 0 in case of errors.
	 */
	private int upgradeInstallation(String installPath) {
		// Install has checked already the basics - which means that
		// the properties and REST endpoints are already done. We now
		// need just to install our new JARs.

		int result = installFiles(installPath);
		
		if ( result != SUCCESS ) {
			return result;
		}
		
		return UPGRADED;
	}

	/**
	 * This installation method allows to specify a path from which to
	 * load the files that will be needing installation - the configuration
	 * files and the JAR libraries.
	 * 
	 * @param installPath	Path in which the config files and libraries are.
	 * 
	 * @return The result of the installation process: SUCCESS, UPGRADED or a failure code.
	 */
	public int install(String installPath) {

		System.out.println("Installing from " + installPath);

		if (!checkInstallSources(installPath)) {
			return WRONG_SOURCE;
		}

		if (!verifyServerPath()) {
			return WRONG_PATH;
		}

		if ( hasFactoryDefined()
				&& hasRestEndpointAdded()) {
			// This is possibly an upgrade
			return upgradeInstallation(installPath);
		}

		// Add our factory in the properties file
		if (!addElasticQuery()) {
			return PROPERTIES_UPDATE_FAILED;
		}

		// Add the elassticsearch language to the REST 
		// adapter configuration
		if (!addElasticRestEndpoint()) {
			return ADAPTER_CONFIG_FAILED;
		}

		
		return installFiles(installPath);
	}

	/**
	 *
	 * This is the main installation method - it will use the current path as the
	 * path from which to load the jars and configuration files.
	 * 
	 * @return The result of the installation process: SUCCESS, UPGRADED or a failure code.
	 */
	public int install() {
		return install(System.getProperty("user.dir"));
	}

	/**
	 * This method is what allows the installer to be launched in the command line.
	 * 
	 * @param arguments Arguments passed to the main function when run from the command line.
	 */
	public static void main (String [] arguments) {

		if (arguments.length != 1) {
			System.out.println("Usage: \njava -jar es-server-ds.jar <JS_HOME>\n");
			System.exit(1);
		}

		Installer pluginInstaller = new Installer(arguments[0]);

		System.exit(pluginInstaller.install());
	}

}
