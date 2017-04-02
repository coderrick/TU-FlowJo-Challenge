package awss3plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.treestar.lib.PluginHelper;
import com.treestar.lib.core.WorkspacePluginInterface;
import com.treestar.lib.xml.SElement;
import com.treestar.lib.xml.XMLUtil;

/**
 * @author Zachary Wentworth
 */
public class AWSS3Plugin implements WorkspacePluginInterface {

	static AWSS3GUI m_AWSS3GUI;
	private AWSManager m_AWSManager;

	/**
	 * Default constructor creates the plugin GUI interface and updates the
	 * values based on the preferences set by the user if this is not the first
	 * time being ran
	 */
	public AWSS3Plugin() {
		m_AWSS3GUI = new AWSS3GUI();
		m_AWSS3GUI.startPlugin();
		m_AWSS3GUI.updateGUI();
	}

	/**
	 * this function loads the saved user prefs that are contained within a
	 * SElement in the FlowJo workspace XMLUtil
	 *
	 * @param workspaceElement
	 *            SElement (directed to the specific element within the
	 *            workspace containing these prefs)
	 */
	private void loadInSavedInfo(SElement workspaceElement) {

		System.out.println("plugin prefs are being loaded...");

		// get whether or not this is the first time the plugin is being setup
		m_AWSS3GUI.setPluginFirstTimeSetup(workspaceElement.getAttribute("FirstTimeSetup"));
		System.out.println("FirstTimeSetup = " + workspaceElement.getAttribute("FirstTimeSetup").toString());

		// get whether or not the user wants the authentication information to
		// be saved for reauthenticating
		m_AWSS3GUI.setSaveUserCreds(workspaceElement.getAttribute("saveUserCreds"));
		System.out.println("saveUserCreds = " + workspaceElement.getAttribute("saveUserCreds").toString());

		// set the Access Key to login to AWS S3 with
		m_AWSS3GUI.setSavedAccessKey(workspaceElement.getAttribute("accessKey").toString());
		System.out.println("access key = " + workspaceElement.getAttribute("accessKey").toString());

		// set the Secret Key to login to AWS S3 with
		m_AWSS3GUI.setSavedSecretKey(workspaceElement.getAttribute("secretKey").toString());
		System.out.println("secret key = " + workspaceElement.getAttribute("secretKey").toString());

		// set the selected bucket
		m_AWSS3GUI.setSavedSelectedBucketName(workspaceElement.getAttribute("BucketName").toString());
		System.out.println("Bucket Name = " + workspaceElement.getAttribute("BucketName").toString());

		// set whether or not workspaces are uploaded on save
		m_AWSS3GUI.setSaveWSPToggled(workspaceElement.getAttribute("SaveWSP").toString());
		System.out.println("SaveWSP = " + workspaceElement.getAttribute("SaveWSP").toString());

		// set whether or not the latest workspace will be downloaded on open
		m_AWSS3GUI.setDownloadLatestToggled(workspaceElement.getAttribute("DownloadLatest").toString());
		System.out.println("SaveWSP = " + workspaceElement.getAttribute("DownloadLatest").toString());
	}

	/**
	 * this function adds the ServerURL to the workspace XML
	 * 
	 * @return String ServerUrl
	 */
	@Override
	public String getServerUrl() {
		return "http://localhost:8080/ServerPluginTest";
	}

	private String opened;

	/**
	 * this function is used to add items to an SElement within the Workspace
	 * XML. In this plugin this is where the plugin preferences are saved.
	 */
	@Override
	public SElement getElement() {

		SElement pluginPrefs = new SElement("Prefs");
		if (opened != null && !opened.isEmpty())
			pluginPrefs.setString("opened", opened);

		// save preferences
		pluginPrefs.setString("FirstTimeSetup", m_AWSS3GUI.getPluginFirstTimeSetup());
		pluginPrefs.setString("saveUserCreds", m_AWSS3GUI.getSaveUserCreds());
		pluginPrefs.setString("accessKey", m_AWSS3GUI.getSavedAccessKey());
		pluginPrefs.setString("secretKey", m_AWSS3GUI.getSavedSecretKey());
		pluginPrefs.setString("BucketName", m_AWSS3GUI.getSavedSelectedBucketName());
		pluginPrefs.setString("SaveWSP", m_AWSS3GUI.getSaveWSPToggled());
		pluginPrefs.setString("DownloadLatest", m_AWSS3GUI.getDownloadLatestToggled());
		pluginPrefs.setString("TimeStamp", new Timestamp(System.currentTimeMillis()).toString());

		System.out.println(pluginPrefs.toString());

		return pluginPrefs;
	}

	/**
	 * this function is called when a workspace is opened. In this plugin it
	 * will check if there is a more recent version of the workspace on the
	 * server if the option is toggled, and prompt the user to download this
	 * latest version. The plugin setting window is loaded on open.
	 *
	 * @param workspaceElement
	 *            SElement of the current workspace that is being opened
	 * @return boolean true plugin was opened without interuption
	 */
	@Override
	public boolean openWorkspace(SElement workspaceElement) {

		// check if the download latest is toggled
		if (m_AWSS3GUI.getDownloadLatestToggled().equals("true")) {

			boolean replaceFile = false;
			boolean authenticationSuccess = false;

			authenticationSuccess = loginToAWSOnOpen(workspaceElement.getChild("Servers").getChild("Server"));

			// check if the aws login was a success before logging
			if (authenticationSuccess) {
				SElement localPrefs = workspaceElement.getChild("Servers").getChild("Server");
				System.out.println(localPrefs.getAttribute("BucketName"));
				System.out.println(PluginHelper.getWorkspaceName(workspaceElement).toString());

				m_AWSManager.get_m_conn().getObject(
						new GetObjectRequest(localPrefs.getAttribute("BucketName"),
								PluginHelper.getWorkspaceName(workspaceElement).toString()),
						new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath() + "\\"
								+ "WSPlatestVersionChecker"));

				// write the existing aws file to the same location
				File existingWSP = new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath()
						+ "\\" + "WSPlatestVersionChecker");

				// convert wsp to selelemt
				SElement prefsElem = new XMLUtil().fileToElement(existingWSP);

				// get time stamps from the SElements as strings
				String AWSTimeStamp = new String(
						prefsElem.getChild("Servers").getChild("Server").getAttribute("TimeStamp").toString());
				String localWSPTimeStamp = new String(
						workspaceElement.getChild("Servers").getChild("Server").getAttribute("TimeStamp").toString());

				// convert to TimeStamp
				java.sql.Timestamp Time1 = java.sql.Timestamp.valueOf(AWSTimeStamp);
				java.sql.Timestamp Time2 = java.sql.Timestamp.valueOf(localWSPTimeStamp);

				// convert to Long
				long tsTime1 = Time1.getTime();
				long tsTime2 = Time2.getTime();

				// compare the two time stamps
				if (tsTime1 > tsTime2) {

					// verify if user wants to replace (MODAL)
					JFrame frame = new JFrame();
					String message = "There is a newer version of this workspace located on your connected AWS server, \ndo you want to replace your local workspace with the new version? \n(This will close FlowJo and you will have to reopen your Workspaces)";
					int answer = JOptionPane.showConfirmDialog(frame, message);
					if (answer == JOptionPane.YES_OPTION) {
						// User clicked YES. replace the file and set boolean
						// variable to close FlowJo is set to true
						m_AWSManager.get_m_conn().getObject(
								new GetObjectRequest(localPrefs.getAttribute("BucketName"),
										PluginHelper.getWorkspaceName(workspaceElement).toString()),
								new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath()
										+ "\\" + PluginHelper.getWorkspaceName(workspaceElement).toString()));
						// replace the file boolean value set to true to
						// activate the close method
						replaceFile = true;

					} else if (answer == JOptionPane.NO_OPTION) {
						// User clicked NO. nothing happens, behaviour is
						// buiness as usual
					}

				}

				// load the saved preferences found in the workspace XML
				loadInSavedInfo(workspaceElement.getChild("Servers").getChild("Server"));
				// start the plugin and update the gui based on prefs
				m_AWSS3GUI.startPlugin();
				m_AWSS3GUI.updateGUI();

				// delete the workspace downloaded to check the prefs
				File fileToDelete = new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath()
						+ "\\" + "WSPlatestVersionChecker");
				fileToDelete.delete();

				/******************************************************************
				 * WARNING, THIS METHOD WILL CLOSE flowjo Completely.
				 *******************************************************************/
				if (replaceFile == true) {
					com.treestar.flowjo.main.Main.exit();
				}

			}
		} else {
			// load prefs file
			loadInSavedInfo(workspaceElement.getChild("Servers").getChild("Server"));
			// start the plugin and update the gui based on prefs
			m_AWSS3GUI.startPlugin();
			m_AWSS3GUI.updateGUI();
		}

		return true;
	}

	/**
	 * this function is used to login to the AWS server when the workspace is
	 * opened to make sure that the credentials are still valid. If an error
	 * occurs while logging in the user will have to re-setup the plugin.
	 *
	 * @param workspaceElement
	 *            SElement the current workspace that is being opened
	 * @return boolean authenticationSuccess (whether or not the authentication
	 *         process was a successful)
	 */
	private boolean loginToAWSOnOpen(SElement workspaceElement) {

		boolean authenticationSuccess = false;

		// get keys
		System.out.println(workspaceElement.getAttribute("accessKey").toString());
		System.out.println(workspaceElement.getAttribute("secretKey").toString());

		// set up the AWSManager
		m_AWSManager = new AWSManager();
		m_AWSManager.set_accessKey(workspaceElement.getAttribute("accessKey").toString());
		m_AWSManager.set_secretKey(workspaceElement.getAttribute("secretKey").toString());

		// check authentaction information
		try {
			m_AWSManager.Authenticate();
			authenticationSuccess = true;
		} catch (Exception e) {

			// if failure occurs when logging into the AWS S3 server, have the
			// user re-setup the plugin
			JFrame frame = new JFrame();
			String message = "There was an error when authenticating your AWS S3 credentials\nYou will need to re setup the plugin";
			JOptionPane.showMessageDialog(null, message, "AWS S3 Plugin Error", JOptionPane.ERROR_MESSAGE);
			// show the authentication screen and make the user re setup the
			// plugin
			m_AWSS3GUI.setPluginFirstTimeSetup("true");
			m_AWSS3GUI.startPlugin();
			authenticationSuccess = false;
		}

		return authenticationSuccess;
	}

	/**
	 * this function is used to upload the workspace to S3 when saving the
	 * workspace and lets the user know the workspace has been uploaded. Items
	 * are only uploaded to S3 if the plugin has been set-up and if the option
	 * has been toggled in the plugin settings panel.
	 *
	 * @param workspaceElement
	 *            SElement the current workspace that is being saved
	 */
	@Override
	public void save(final SElement workspaceElement) {

		try {

			if (m_AWSS3GUI.getPluginFirstTimeSetup().equals("false") && m_AWSS3GUI.getSaveWSPToggled().equals("true")) {

				// show plugin prefs on save
				m_AWSManager = new AWSManager(AWSS3GUI.get_AWSManager());

				// file path for the workspace
				File myWorkspace = new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath()
						+ "\\" + PluginHelper.getWorkspaceName(workspaceElement));
				new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath() + "\\"
						+ "S3UploadTemp").mkdir();

				// create a temp copy of the current workspace to upload
				/******************************************************************
				 * A temp is created because this method is called before FlowJo
				 * saves the workspace. Therefore, a new version has to be
				 * created temporarily to upload what is currently in the
				 * workspace.
				 *******************************************************************/
				File tempFileToUpload = new File(
						PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath() + "\\"
								+ "S3UploadTemp\\" + PluginHelper.getWorkspaceName(workspaceElement));
				XMLUtil tempSave = new XMLUtil();
				tempSave.write(workspaceElement, tempFileToUpload);

				// upload the workspace to S3
				m_AWSManager.uploadToS3(m_AWSManager.get_userUploadBucket(), tempFileToUpload,
						PluginHelper.getWorkspaceName(workspaceElement));

				// get the items in the bucket (testing purpose)
				m_AWSManager.getItemsInBucket(m_AWSManager.get_userUploadBucket());

				// delete the temporary workspace that was created for upload
				try {
					FileUtils.deleteDirectory(
							new File(PluginHelper.getWorkspaceAnalysisFolder(workspaceElement).getAbsolutePath() + "\\"
									+ "S3UploadTemp"));
				} catch (IOException e) {

					e.printStackTrace();
				}

				// show that the workspace uploaded successfully
				String message = "Workspace Uploaded to \nS3 Successfully";
				JOptionPane.showMessageDialog(null, message, "upload complete", JOptionPane.INFORMATION_MESSAGE);

				// show the plugin
				m_AWSS3GUI.startPlugin();

			} else {
				// if the plugin has been added to the workspace but not set up,
				// prompt user to setup the plugin.
				System.out.println("first time run");
				m_AWSS3GUI.startPlugin();
			}

			// if there is any issue upload to the server, have the user re
			// setup the plugin
		} catch (Exception e) {
			String message = "There was an error when uploading to your AWS S3\nYou will need to re setup the plugin";
			JOptionPane.showMessageDialog(null, message, "AWS S3 Plugin Error", JOptionPane.ERROR_MESSAGE);
			// show the authentication screen and make the user re setup the
			// plugin
			m_AWSS3GUI.setPluginFirstTimeSetup("true");
			m_AWSS3GUI.startPlugin();
		}

	}

	/**
	 * unused interface function
	 */
	@Override
	public void endSession() {
	}

	/**
	 * @retun String version
	 */
	@Override
	public String getVersion() {
		return "1.0";
	}

}