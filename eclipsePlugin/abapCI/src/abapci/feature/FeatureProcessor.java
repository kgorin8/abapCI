package abapci.feature;

import java.util.List;

import org.eclipse.core.resources.IProject;

import abapci.activation.Activation;
import abapci.domain.SourcecodeState;
import abapci.domain.TestState;
import abapci.manager.AUnitTestManager;
import abapci.manager.AtcTestManager;
import abapci.manager.DevelopmentProcessManager;
import abapci.manager.JenkinsManager;
import abapci.manager.ThemeUpdateManager;
import abapci.presenter.ContinuousIntegrationPresenter;

public class FeatureProcessor {

	private AUnitTestManager aUnitTestManager;
	private JenkinsManager jenkinsManager;
	private AtcTestManager atcTestManager;

	private ThemeUpdateManager themeUpdateManager;

	private FeatureFacade featureFacade;
	private ContinuousIntegrationPresenter presenter;
	private List<Activation> inactiveObjects;
	private DevelopmentProcessManager developmentProcessManager;

	public FeatureProcessor(ContinuousIntegrationPresenter presenter, IProject project, List<String> initialPackages) {

		this.presenter = presenter;
		aUnitTestManager = new AUnitTestManager(presenter, project, initialPackages);
		jenkinsManager = new JenkinsManager(presenter, project, initialPackages);
		atcTestManager = new AtcTestManager(presenter, project, initialPackages);
		developmentProcessManager = new DevelopmentProcessManager();

		themeUpdateManager = new ThemeUpdateManager();

		featureFacade = new FeatureFacade();

	}

	public void setPackagesAndObjects(List<String> packageNames, List<Activation> inactiveObjects) {
		aUnitTestManager.setPackages(packageNames);
		atcTestManager.setPackages(packageNames);
		this.inactiveObjects = inactiveObjects;
	}

	public void processEnabledFeatures() {

		try {
			if (featureFacade.getUnitFeature().isActive()) {
				SourcecodeState oldSourcecodeState = developmentProcessManager.getSourcecodeState();

				TestState unitTestState = TestState.UNDEF;
				if (featureFacade.getUnitFeature().isRunActivatedObjectsOnly()) {
					if (inactiveObjects != null) {
						unitTestState = aUnitTestManager.executeAllPackages(presenter.getCurrentProject(),
								presenter.getAbapPackageTestStatesForCurrentProject(), inactiveObjects);
					}
				} else {
					unitTestState = aUnitTestManager.executeAllPackages(presenter.getCurrentProject(),
							presenter.getAbapPackageTestStatesForCurrentProject(), null);
				}

				developmentProcessManager.setUnitTeststate(unitTestState);
				themeUpdateManager.updateTheme(developmentProcessManager.getSourcecodeState());

			}

			if (featureFacade.getAtcFeature().isActive()) {
				TestState atcTestState = null;
				if (featureFacade.getAtcFeature().isRunInitial()
						&& presenter.getAbapPackageTestStatesForCurrentProject().stream()
								.anyMatch(item -> item.getAtcTestState().equals(TestState.OFFL))) {
					atcTestState = atcTestManager.executeAllPackages(presenter.getCurrentProject(),
							presenter.getAbapPackageTestStatesForCurrentProject(), inactiveObjects);
				}

				if (featureFacade.getAtcFeature().isRunActivatedObjects() && inactiveObjects != null) {

					atcTestState = atcTestManager.executeAllPackages(presenter.getCurrentProject(),
							presenter.getAbapPackageTestStatesForCurrentProject(), inactiveObjects);
				}

				if (atcTestState != null) {
					developmentProcessManager.setAtcTeststate(atcTestState);
					themeUpdateManager.updateTheme(developmentProcessManager.getSourcecodeState());
				}
			}

			presenter.updateViewsAsync(developmentProcessManager.getSourcecodeState());

		} catch (

		Exception ex) {
			presenter.setStatusMessage("Testrun failed, exception: " + ex.getMessage());
		}

	}

}
