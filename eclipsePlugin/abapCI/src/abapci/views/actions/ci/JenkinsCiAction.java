package abapci.views.actions.ci;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import abapci.AbapCiPlugin;
import abapci.domain.AbapPackageTestState;
import abapci.handlers.JenkinsHandler;
import abapci.views.ViewModel;

public class JenkinsCiAction extends AbstractCiAction {

	public JenkinsCiAction(String label, String tooltip) {
		this.setText(label);
		this.setToolTipText(tooltip);
		this.setImageDescriptor(AbapCiPlugin.getImageDescriptor("icons/jenkins.ico"));
	}

	@Override
	public void run() {

		List<AbapPackageTestState> packageTestStates = ViewModel.INSTANCE.getPackageTestStates();

		for (Iterator<Entry<String, String>> iter = getSelectedPackages().entrySet().iterator(); iter.hasNext();) {

			String packageName = iter.next().getValue();

			try {
				new JenkinsHandler().execute(packageName);
			} catch (Exception ex) {
				// TODO
			}

			for (AbapPackageTestState packageTestState : packageTestStates) {
				if (packageName == packageTestState.getPackageName()) {
					packageTestState.setJenkinsInfo("Job triggered");
				}

				ViewModel.INSTANCE.updatePackageTestStates();
			}
		}
	}
}
