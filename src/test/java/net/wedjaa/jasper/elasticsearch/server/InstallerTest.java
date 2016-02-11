package net.wedjaa.jasper.elasticsearch.server;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class InstallerTest {

	private static final String TARGET_PATH = "target";

	private String joinPaths(String basePath, String subPath) {
		Path path = Paths.get(basePath, subPath);
		return path.toAbsolutePath().toString();	  
	}
	
	private String getTestDirectoryPath(String path) {
		ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
	}
	
	private String getWrongDirectoryPath() {
		return getTestDirectoryPath("wrongdir");
	}

	private String getInstallDirectoryPath() {
		return getTestDirectoryPath("install");
	}
	private String getUpgradeDirectoryPath() {
		return getTestDirectoryPath("upgrade");
	}

	private String getInstallPath() {
		return joinPaths(System.getProperty("user.dir"), TARGET_PATH);
	}
	
	@Test
	public void testNonExistingInstallPath() {
		System.out.println("Testing non existing path");
		Installer installer = new Installer("does_not_exist");
		
		assertEquals(Installer.WRONG_PATH, installer.install(getInstallPath()));
	}

	@Test
	public void testWrongInstallPath() {
		System.out.println("Testing non JServer path");
		Installer installer = new Installer(getWrongDirectoryPath());
		assertEquals(Installer.WRONG_PATH, installer.install(getInstallPath()));
	}

	@Test
	public void testInitialInstall() {
		System.out.println("Testing Installation");
		Installer installer = new Installer(getInstallDirectoryPath());
		assertEquals(Installer.SUCCESS, installer.install(getInstallPath()));
	}
	
	@Test
	public void testUpgrade() {
		System.out.println("Testing Upgrade");
		Installer installer = new Installer(getUpgradeDirectoryPath());
		assertEquals(Installer.UPGRADED, installer.install(getInstallPath()));
	}
	
}
