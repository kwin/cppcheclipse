package com.googlecode.cppcheclipse.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.cdtvariables.IUserVarSupplier;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

public class ToolchainSettingsTest {

	private IProject project;
	private ICdtVariableManager cdtVariableManager;
	private IPathVariableManager pathVariableManager;

	@Before
	public void setUp() throws CoreException, IOException {
		project = createProject("emptyProject");

		cdtVariableManager = CCorePlugin.getDefault().getCdtVariableManager();
		assertNotNull(cdtVariableManager);
		pathVariableManager = project.getWorkspace().getPathVariableManager();
		assertNotNull(pathVariableManager);
	}

	@After
	public void tearDown() throws CoreException {
		project.delete(true, null);
	}

	// compare with org.eclipse.cdt.core.tests.IEnvironmentVariableManagerTests
	public void addCdtEnvironmentVariable(IProject project, String name,
			String value) throws CoreException {
		ICProjectDescription prjDesc = CoreModel.getDefault()
				.getProjectDescription(project);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault()
				.getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager
				.getContributedEnvironment();

		ICConfigurationDescription activeConfiguration = prjDesc
				.getActiveConfiguration();

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable(name, value);
		contribEnv.addVariable(var, activeConfiguration);

		// to make the changes on the project description become effective, we
		// have to call setProjectDescription
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Get an environment variable:
		IEnvironmentVariable var2 = envManager.getVariable(var.getName(),
				activeConfiguration, true);
	}

	public void addCdtUserVariable(IProject project, String name, String value)
			throws CoreException {
		ICProjectDescription prjDesc = CoreModel.getDefault()
				.getProjectDescription(project);
		ICConfigurationDescription activeConfiguration = prjDesc
				.getActiveConfiguration();

		IUserVarSupplier userVarSupplier = CCorePlugin.getUserVarSupplier();
		userVarSupplier.createMacro(name, ICdtVariable.VALUE_PATH_DIR, value,
				activeConfiguration);

		// to make the changes on the project description become effective, we
		// have to call setProjectDescription
		CoreModel.getDefault().setProjectDescription(project, prjDesc);
	}

	// add CDT nature
	// http://cdt-devel-faq.wikidot.com/#toc27
	private void addCDTNature(IProject project) throws CoreException {
		// IProgressMonitor monitor
		CProjectNature.addCNature(project, null);
		ICProjectDescriptionManager mgr = CoreModel.getDefault()
				.getProjectDescriptionManager();
		ICProjectDescription description = mgr.createProjectDescription(
				project, true);
		description.createConfiguration("config", "config-name", null);
		mgr.setProjectDescription(project, description);
	}

	// taken over from
	// org.eclipse.cdt.make.builder.tests.CDataProviderTests.java
	private IProject createProject(final String name) throws CoreException {
		final Object[] result = new Object[1];
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = root.getProject(name);
				IProjectDescription description = null;

				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}

				if (!project.isOpen()) {
					project.open(null);
				}

				description = project.getDescription();

				CCorePlugin.getDefault().createCDTProject(description, project,
						MakeCorePlugin.CFG_DATA_PROVIDER_ID,
						new NullProgressMonitor());
				result[0] = project;
			}
		}, null);
		return (IProject) result[0];
	}

	// this test does only run on specific platforms (in this case only
	// Unix-based systems)
	@Test
	public void testResolveIncludePath() throws CoreException {
		// should make the test listed in issue 39,
		// http://code.google.com/a/eclipselabs.org/p/cppcheclipse/issues/detail?id=39

		// URI
		addCdtUserVariable(project, "FOOBAR", "/test/a/b/c");
		checkResolveIncludePath("${FOOBAR}/test", "/test/a/b/c/test");

		// non URI with space
		addCdtUserVariable(project, "FOOBAR", "C:\\a a");
		checkResolveIncludePath("${FOOBAR}\\test", "C:\\a a\\test");

		// use Windows path as URI with space
		addCdtUserVariable(project, "FOOBAR", "/C:/a a");
		checkResolveIncludePath("${FOOBAR}/test", "/C:/a a/test");

		// check with Eclipse Workspace Variable
		checkResolveIncludePath("/${ProjName}/toto", "/emptyProject/toto");
	}

	@Test(expected = CdtVariableException.class)
	public void testResolveIncludePathInexistentCdtVariable()
			throws CdtVariableException {
		checkResolveIncludePath("${FOOBAR}/test", "/test/a/b/c/test");
	}

	@Test
	@Ignore
	public void testResolveIncludePathLinked() throws CoreException {
		// add linked path
		IFolder linkedFolder = project.getFolder("linkedFolder");
		IFolder realFolder = project.getFolder("realFolder");
		realFolder.create(true, true, null);
		IFile file = realFolder.getFile("file1");
		// file.create();

		linkedFolder.createLink(realFolder.getLocationURI(), IResource.REPLACE,
				null);

		// TODO: check that the real file is resolved
		checkResolveIncludePath("/${ProjName}/toto", "/emptyProject/toto");

		// TODO: check absolute filename
	}

	private void checkResolveIncludePath(String includePath,
			String expectedResolvedPath) throws CdtVariableException {

		ToolchainSettings toolchainSettings = new ToolchainSettings(project);
		Collection<File> files = toolchainSettings.resolveIncludePath(new File(
				includePath), pathVariableManager);
		assertEquals(1, files.size());
		assertThat(files, JUnitMatchers.hasItem(new File(expectedResolvedPath)));
	}

}
