package awss3plugin;

import java.awt.EventQueue;
import javax.swing.JFrame;
import java.awt.CardLayout;
import java.awt.Desktop;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Button;
import javax.swing.JTextPane;
import java.awt.Color;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JToggleButton;
import javax.swing.JProgressBar;

/**
 * @author Zachary Wentworth
 */
public class AWSS3GUI {

	// AWSManager object used for interfacing with AWS
	private static AWSManager m_AWSManager;

	// data members that handle the plugin prefs
	private String pluginFirstTimeSetup;
	private String savedAccessKey;
	private String savedSecretKey;
	private String saveUserCreds;
	private String savedSelectedBucketName;
	private String saveWSPToggled;
	private String downloadLatestToggled;

	// GUI Elements
	private JFrame frame;

	// panels
	private JPanel pnlAuthentication;
	private JPanel pnlLoading;
	private JPanel pnlFailedLogin;
	private JPanel pnlS3Tree;
	private JPanel pnlSettings;

	// authentication fields
	private JTextField fldAccessKey;
	private JTextField pwdFldSecretKey;
	private JCheckBox chckbxSaveCredentials;

	private JLabel lblFailedLogin;

	// bucket creator dialog
	private S3BucketCreator bucketCreatorDialog;

	// S3 Bucket Tree
	private JScrollPane treeScrollPane;
	private JTree treeS3;

	// settings panels
	JToggleButton tglUploadWorkspace;
	JToggleButton tglGetLatest;

	/**
	 * getter for the the AWS_Manager
	 *
	 * @return AWSManager m_AWSManger
	 */
	public static AWSManager get_AWSManager() {
		return m_AWSManager;
	}

	/**
	 * getter for getting the saved bucket name selected by the user in the
	 * bucket selector screen
	 *
	 * @return String savedSelectedBucketName
	 */
	public String getSavedSelectedBucketName() {
		return savedSelectedBucketName;
	}

	/**
	 * setter for setSavedSelectedBucketName
	 *
	 * @param savedSelectedBucketName
	 */
	public void setSavedSelectedBucketName(String savedSelectedBucketName) {
		this.savedSelectedBucketName = savedSelectedBucketName;
	}

	/**
	 * getter for the savedAccessKey
	 *
	 * @return String savedAccessKey
	 */
	public String getSavedAccessKey() {
		return savedAccessKey;
	}

	/**
	 * setter for the savedAccessKey
	 *
	 * @param savedAccessKey
	 */
	public void setSavedAccessKey(String savedAccessKey) {
		this.savedAccessKey = savedAccessKey;
	}

	/**
	 * getter for the savedSecretKey
	 *
	 * @return String savedAccessKey
	 */
	public String getSavedSecretKey() {
		return savedSecretKey;
	}

	/**
	 * setter for the savedSecretKey
	 *
	 * @param savedSecretKey
	 */
	public void setSavedSecretKey(String savedSecretKey) {
		this.savedSecretKey = savedSecretKey;
	}

	/**
	 * getter for saveUserCreds
	 *
	 * @return String saveUserCreds
	 */
	public String getSaveUserCreds() {
		return saveUserCreds;
	}

	/**
	 * setter for saveUserCreds
	 *
	 * @param saveUserCreds
	 */
	public void setSaveUserCreds(String saveUserCreds) {
		this.saveUserCreds = saveUserCreds;
	}

	/**
	 * getter for saveWSPToggled
	 *
	 * @return String saveWSPToggled
	 */
	public String getSaveWSPToggled() {
		return saveWSPToggled;
	}

	/**
	 * setter for saveWSPToggled
	 *
	 * @param saveWSPToggled
	 */
	public void setSaveWSPToggled(String saveWSPToggled) {
		this.saveWSPToggled = saveWSPToggled;
	}

	/**
	 * getter for pluginFirstTimeSetup
	 *
	 * @return String pluginFirstTimeSetup
	 */
	public String getPluginFirstTimeSetup() {
		return pluginFirstTimeSetup;
	}

	/**
	 * setter for pluginFirstTimeSetup
	 *
	 * @param pluginFirstTimeSetup
	 */
	public void setPluginFirstTimeSetup(String pluginFirstTimeSetup) {
		this.pluginFirstTimeSetup = pluginFirstTimeSetup;
	}

	/**
	 * getter for downloadLatestToggled
	 *
	 * @return String downloadLatestToggled
	 */
	public String getDownloadLatestToggled() {
		return downloadLatestToggled;
	}

	/**
	 * setter for downloadLatestToggled
	 *
	 * @param downloadLatestToggled
	 */
	public void setDownloadLatestToggled(String downloadLatestToggled) {
		this.downloadLatestToggled = downloadLatestToggled;
	}

	/**
	 * default constructor initializes AWSS3GUI data preferences and initializes
	 * the GUI
	 */
	public AWSS3GUI() {

		m_AWSManager = new AWSManager();

		pluginFirstTimeSetup = "true";
		savedAccessKey = "";
		savedSecretKey = "";
		saveUserCreds = "false";
		savedSelectedBucketName = "";
		saveWSPToggled = "true";
		downloadLatestToggled = "true";

		initialize();
	}

	/**
	 * Create the plugin window
	 */
	public void startPlugin() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// AWSS3GUI window = new AWSS3GUI();
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// display proper screen based on boolean value of
		// pluginFirstTimeSetup
		if (pluginFirstTimeSetup.equals("true")) {
			pnlAuthentication.setVisible(true);
			pnlSettings.setVisible(false);
		} else {
			// show settings menu
			pnlAuthentication.setVisible(false);
			pnlSettings.setVisible(true);
		}

	}

	/**
	 * Update the GUI based on plugin preferences
	 */
	public void updateGUI() {
		// check if this is the first time the plugin has ran
		// if so set toggles true for uploading workspaces and getting the
		// latest from the server
		if (pluginFirstTimeSetup.equals("true")) {
			tglUploadWorkspace.setSelected(true);
			tglGetLatest.setSelected(true);
		} else {

			// set toggle based on prefs
			if (saveWSPToggled.equals("true")) {
				tglUploadWorkspace.setSelected(true);
			} else {
				tglUploadWorkspace.setSelected(false);
			}

			// set toggle based on prefs
			if (downloadLatestToggled.equals("true")) {
				tglGetLatest.setSelected(true);
			} else {
				tglGetLatest.setSelected(false);
			}
		}

		GetSavedAuthenticationInfo();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// set up window
		frame = new JFrame();
		frame.setBounds(100, 100, 642, 522);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));

		// set up the Authentication panel
		initAuthenticationPanel();

		// set up the Loading window
		initLoadingPanel();

		// set up the failed login window
		initFailedLoginPanel();

		// set up the s3 tree window
		initS3TreePanel();

		// set up the settings panel
		initSettingsPanel();

	}

	/**
	 * initializes the Authentication Panel
	 */
	private void initAuthenticationPanel() {
		// get the saved authentication info if it has been saved in the
		// workspace file

		// create the panel ->pnlAuthentication
		pnlAuthentication = new JPanel();
		frame.getContentPane().add(pnlAuthentication, "name_6878110704687");
		pnlAuthentication.setLayout(null);

		// create label -> lblAwsSLog
		JLabel lblAwsSLog = new JLabel("AWS S3 log in");
		lblAwsSLog.setFont(new Font("Calibri", Font.PLAIN, 20));
		lblAwsSLog.setBounds(255, 108, 114, 27);
		pnlAuthentication.add(lblAwsSLog);

		// create label -> lblAccessKey
		JLabel lblAccessKey = new JLabel("access key");
		lblAccessKey.setFont(new Font("Calibri", Font.PLAIN, 17));
		lblAccessKey.setBounds(71, 147, 81, 27);
		pnlAuthentication.add(lblAccessKey);

		// create label -> lblSecretKey
		JLabel lblSecretKey = new JLabel("secret key");
		lblSecretKey.setFont(new Font("Calibri", Font.PLAIN, 17));
		lblSecretKey.setBounds(71, 186, 81, 27);
		pnlAuthentication.add(lblSecretKey);

		// create text field -> fldAccessKey
		fldAccessKey = new JTextField();
		fldAccessKey.setColumns(10);
		fldAccessKey.setBounds(164, 148, 296, 22);
		pnlAuthentication.add(fldAccessKey);

		// create check box -> chckbxSaveCredentials
		chckbxSaveCredentials = new JCheckBox("Save Credentials?");
		chckbxSaveCredentials.setSelected(true);
		chckbxSaveCredentials.setBounds(246, 260, 132, 25);
		pnlAuthentication.add(chckbxSaveCredentials);

		// create button -> btnAuthenticate
		JButton btnAuthenticate = new JButton("Authenticate");
		btnAuthenticate.setBounds(255, 222, 114, 25);
		pnlAuthentication.add(btnAuthenticate);
		// btnAuthenticate is pressed
		btnAuthenticate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AuthenticateButtonPressed();
			}
		});

		// create text field ->pwdFldSecretKey
		pwdFldSecretKey = new JTextField();
		pwdFldSecretKey.setBounds(164, 187, 296, 22);
		pnlAuthentication.add(pwdFldSecretKey);

		// create button -> btnAWShelp
		JButton btnAWShelp = new JButton("Where do I find my AWS Credentials ?");
		btnAWShelp.setForeground(SystemColor.textHighlight);
		btnAWShelp.setBackground(SystemColor.menu);
		btnAWShelp.setBounds(173, 298, 277, 25);
		pnlAuthentication.add(btnAWShelp);
		// btnAWShelp is pressed
		btnAWShelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open browser and go to AWS S3 login help web page
				AWSHelpWebPage();
			}
		});

		GetSavedAuthenticationInfo();
	}

	/**
	 * initializes Loading Panel
	 */
	private void initLoadingPanel() {

		// create panel -> pnlLoading
		pnlLoading = new JPanel();
		frame.getContentPane().add(pnlLoading, "name_7288241654455");
		pnlLoading.setLayout(null);

		// create label -> lblLoading
		JLabel lblLoading = new JLabel("Loading...");
		lblLoading.setBounds(279, 217, 66, 41);
		pnlLoading.add(lblLoading);

		// create progress bar -> prgBarLoading
		JProgressBar prgBarLoading = new JProgressBar();
		prgBarLoading.setBounds(239, 253, 146, 14);
		pnlLoading.add(prgBarLoading);
		prgBarLoading.setVisible(true);
		prgBarLoading.setIndeterminate(true);
	}

	/**
	 * initializes the Failed Login Panel
	 */
	private void initFailedLoginPanel() {

		// create panel -> pnlFailedLogin
		pnlFailedLogin = new JPanel();
		frame.getContentPane().add(pnlFailedLogin, "name_7295171156888");
		pnlFailedLogin.setLayout(null);

		// create label
		lblFailedLogin = new JLabel("The credentials entered were invalid, please try again");
		lblFailedLogin.setBounds(154, 155, 315, 51);
		pnlFailedLogin.add(lblFailedLogin);

		// create button -> btnOK
		Button btnOK = new Button("OK");
		btnOK.setBounds(272, 225, 79, 24);
		pnlFailedLogin.add(btnOK);
		// btnOK is pressed
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pnlFailedLogin.setVisible(false);
				pnlAuthentication.setVisible(true);
			}
		});
	}

	/**
	 * initializes the S3 Tree Panel
	 */
	private void initS3TreePanel() {

		// create panel -> pnlS3Tree
		pnlS3Tree = new JPanel();
		frame.getContentPane().add(pnlS3Tree, "name_7319782992158");
		pnlS3Tree.setLayout(null);

		// create scroll pane -> treeScrollPane
		treeScrollPane = new JScrollPane();
		treeScrollPane.setBounds(27, 26, 569, 260);
		pnlS3Tree.add(treeScrollPane);

		// create label -> lblSelectionError
		final JLabel lblSelectionError = new JLabel("You must select an S3 bucket to upload to!");
		lblSelectionError.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSelectionError.setForeground(Color.RED);
		lblSelectionError.setBounds(178, 419, 267, 16);
		pnlS3Tree.add(lblSelectionError);
		lblSelectionError.setVisible(false);

		// create button -> btnSelectFolderTo
		JButton btnSelectFolderTo = new JButton("Select folder to upload your data");
		btnSelectFolderTo.setBounds(188, 299, 247, 25);
		pnlS3Tree.add(btnSelectFolderTo);
		btnSelectFolderTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Selected Bucket = " + savedSelectedBucketName.toString());
				if (!savedSelectedBucketName.toString().equals("")) {
					m_AWSManager.set_userUploadBucket(savedSelectedBucketName);
					pnlS3Tree.setVisible(false);
					pnlSettings.setVisible(true);
					// set the plugin to set up
					pluginFirstTimeSetup = "false";
					// write to the file that it has been setup
				} else {
					lblSelectionError.setVisible(true);
				}
			}
		});

		// create button -> btnNewButton
		JButton btnNewButton = new JButton("Back");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pnlS3Tree.setVisible(false);
				pnlAuthentication.setVisible(true);
			}
		});
		btnNewButton.setBounds(263, 352, 97, 25);
		pnlS3Tree.add(btnNewButton);
	}

	/**
	 * initializes the Settings Panel
	 */
	private void initSettingsPanel() {

		// create Panel -> pnlSettings
		pnlSettings = new JPanel();
		frame.getContentPane().add(pnlSettings, "name_7335852232820");
		pnlSettings.setLayout(null);

		// create label -> lblPluginSettings
		JLabel lblPluginSettings = new JLabel("FlowJo AWS S3 Plugin Settings");
		lblPluginSettings.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblPluginSettings.setBounds(189, 66, 246, 22);
		pnlSettings.add(lblPluginSettings);

		// create label -> lblUploadWorkspaceOnSave
		JLabel lblUploadWorkspaceOnSave = new JLabel("Upload Workspace on Save");
		lblUploadWorkspaceOnSave.setBounds(234, 151, 156, 16);
		pnlSettings.add(lblUploadWorkspaceOnSave);

		// create label -> lblGetLatestVersion
		JLabel lblGetLatestVersion = new JLabel("Get latest workspace version on open");
		lblGetLatestVersion.setBounds(234, 203, 214, 16);
		pnlSettings.add(lblGetLatestVersion);

		// create toggle -> tglUploadWorkspace
		tglUploadWorkspace = new JToggleButton("");
		tglUploadWorkspace.setFocusPainted(false);
		tglUploadWorkspace.setSelectedIcon(new ImageIcon(AWSS3GUI.class.getResource("/iconsandimages/onToggle.png")));
		tglUploadWorkspace.setIcon(new ImageIcon(AWSS3GUI.class.getResource("/iconsandimages/offToggle.png")));
		tglUploadWorkspace.setContentAreaFilled(false);
		tglUploadWorkspace.setBorderPainted(false);
		tglUploadWorkspace.setBounds(141, 140, 81, 39);
		pnlSettings.add(tglUploadWorkspace);
		ActionListener ActionListenerTglUpld = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				System.out.println("Upload Workspace =" + selected + "\n");

				// save the prefs
				if (tglUploadWorkspace.isSelected()) {
					saveWSPToggled = "true";
				} else {
					saveWSPToggled = "false";
				}

			}
		};
		tglUploadWorkspace.addActionListener(ActionListenerTglUpld);

		// create toggle -> tglGetLatest
		tglGetLatest = new JToggleButton("");
		tglGetLatest.setFocusPainted(false);
		tglGetLatest.setIcon(new ImageIcon(AWSS3GUI.class.getResource("/iconsandimages/offToggle.png")));
		tglGetLatest.setSelectedIcon(new ImageIcon(AWSS3GUI.class.getResource("/iconsandimages/onToggle.png")));
		tglGetLatest.setContentAreaFilled(false);
		tglGetLatest.setBorderPainted(false);
		tglGetLatest.setBounds(141, 193, 81, 39);
		pnlSettings.add(tglGetLatest);
		ActionListener ActionListenerTglGetLatest = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				System.out.println("Get Latest =" + selected + "\n");

				// save the prefs
				if (tglGetLatest.isSelected()) {
					downloadLatestToggled = "true";
				} else {
					downloadLatestToggled = "false";
				}

			}
		};
		tglGetLatest.addActionListener(ActionListenerTglGetLatest);

		// create button -> btnChangeDir
		JButton btnChangeDir = new JButton("change workspace S3 destination");
		btnChangeDir.setBounds(180, 285, 263, 25);
		pnlSettings.add(btnChangeDir);
		btnChangeDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pnlSettings.setVisible(false);
				pnlLoading.setVisible(true);
				showBucketCreatorDialog();
			}
		});

		// create button -> btnChangeAuthenticationInfo
		JButton btnChangeAuthenticationInfo = new JButton("change authentication info");
		btnChangeAuthenticationInfo.setBounds(180, 332, 263, 25);
		pnlSettings.add(btnChangeAuthenticationInfo);
		btnChangeAuthenticationInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pnlSettings.setVisible(false);
				pnlAuthentication.setVisible(true);
			}
		});

	}

	/*
	 * this function is used to load the previously saved authentication
	 * credentials into the text fields on the authentication panel
	 *
	 */
	private void GetSavedAuthenticationInfo() {

		// get the saved access key
		m_AWSManager.set_accessKey(savedAccessKey);

		// get the save secret key
		m_AWSManager.set_secretKey(savedSecretKey);

		// reload the saved bucket name
		m_AWSManager.set_userUploadBucket(savedSelectedBucketName);

		// update these items in the GUI if the pref has been set
		if (saveUserCreds.equals("true")) {
			fldAccessKey.setText(savedAccessKey);
			pwdFldSecretKey.setText(savedSecretKey);
		}

	}

	/**
	 * this function is called when the Authenticate button is pressed. It
	 * checks if the authentication information was valid as well as saves the
	 * login information for later use if the user applies this option in the
	 * check box
	 *
	 */
	private void AuthenticateButtonPressed() {

		// check if the user wants to save the creds if he or she chooses to
		// reauthenticate later
		if (chckbxSaveCredentials.isSelected()) {
			saveUserCreds = "true";

			System.out.println("creds fields saved");
		} else {
			saveUserCreds = "false";
		}

		pnlAuthentication.setVisible(false);
		pnlLoading.setVisible(true);

		// timer used for the loading screen
		Timer loadTime = new Timer();
		// timer thread
		TimerTask task = new TimerTask() {
			public void run() {
				// play the loading animation for 3 seconds
				// Authenticate the login information
				checkAuthenticationInfo();
			}
		};
		loadTime.schedule(task, 1000);
	}

	/**
	 * this function is called when the Authenticate button is pressed. It
	 * checks if the authentication information is valid.
	 *
	 */
	private void checkAuthenticationInfo() {

		// set the keys based on the text fields
		m_AWSManager.set_accessKey(fldAccessKey.getText());
		m_AWSManager.set_secretKey(pwdFldSecretKey.getText());

		savedAccessKey = fldAccessKey.getText();
		savedSecretKey = pwdFldSecretKey.getText();

		// Authenticate the login information
		try {
			m_AWSManager.Authenticate();
			// show bucket creator dialog
			showBucketCreatorDialog();
			System.out.println("Authentication Successful");

		} catch (AmazonS3Exception exception) {
			pnlLoading.setVisible(false);
			pnlFailedLogin.setVisible(true);
			System.out.println(exception.toString());
			System.out.println("Authentication Failed");
		}
	}

	/**
	 * method called when a Successful authentication occurs to prompt user to
	 * either create a bucket to upload to or not
	 */
	public void showBucketCreatorDialog() {
		try {
			bucketCreatorDialog = new S3BucketCreator();
			bucketCreatorDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			bucketCreatorDialog.setLocationRelativeTo(null);
			bucketCreatorDialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * this function is called when the "Where do I find my AWS Credentials"
	 * button is pressed. it opens the users browser to the official amazon
	 * documentation on how to manage AWS access keys and secret keys
	 */
	private void AWSHelpWebPage() {
		// open the users default web browser
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(
						new URL("http://docs.aws.amazon.com/general/latest/gr/managing-aws-access-keys.html").toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get a list of buckets and add all of those buckets to the Jtree
	 */
	private void populateS3FileTree() {
		// load the list of s3 buckets into the list
		ArrayList<String> bucketNames = new ArrayList<String>(m_AWSManager.getS3Buckets());
		DefaultMutableTreeNode m_s3BucketTree = new DefaultMutableTreeNode("Root");

		for (int i = 0; i < bucketNames.size(); i++) {
			// check if an exception is thrown when trying to access the items
			// within a bucket
			if (m_AWSManager.checkIfAccessible(bucketNames.get(i))) {
				String nodeName = new String(bucketNames.get(i));
				DefaultMutableTreeNode bucketNode = new DefaultMutableTreeNode(nodeName);
				m_s3BucketTree.add(bucketNode);
			}
		}

		// create a tree adding m_s3BucketTree
		treeS3 = new JTree(m_s3BucketTree);

		// set the icons to buckets
		ImageIcon bucketIcon = new ImageIcon(AWSS3GUI.class.getResource("/iconsandimages/bucketIcon.png"));
		if (bucketIcon != null) {
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			renderer.setLeafIcon(bucketIcon);
			treeS3.setCellRenderer(renderer);
		}

		// make only one node selectable at a time
		treeS3.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// hide the root
		treeS3.setRootVisible(false);
		// show the bucket tree in the treeScrollPane
		treeScrollPane.setViewportView(treeS3);

		// Listen for when the selection changes in the treeS3
		treeS3.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				// Returns the last path element of the selection.
				savedSelectedBucketName = "";

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeS3.getLastSelectedPathComponent();

				// Nothing is selected.
				if (node == null)
					return;

				// get the current selected object
				Object nodeInfo = node.getUserObject();

				savedSelectedBucketName = nodeInfo.toString();
				System.out.println(savedSelectedBucketName);

			}
		});

	}

	/**
	 * S3BucketCreator Dialog GUI for creating buckets on the AWS S3 Server
	 */
	public class S3BucketCreator extends JDialog {

		private JTextField txtBucketName;

		private JPanel pnlBucketNamer;
		private JPanel pnlBucketCreatePrompt;
		private JPanel pnlError;

		/**
		 * Create the dialog.
		 */
		public S3BucketCreator() {

			setModal(true);
			setTitle("AWS S3 Bucket Setup");

			setBounds(100, 100, 339, 204);
			getContentPane().setLayout(new CardLayout(0, 0));

			pnlBucketCreatePrompt = new JPanel();
			getContentPane().add(pnlBucketCreatePrompt, "name_148067784857692");
			pnlBucketCreatePrompt.setLayout(null);

			final JTextPane txtPnPrompt = new JTextPane();
			txtPnPrompt.setEditable(false);
			txtPnPrompt.setBounds(31, 13, 259, 56);
			txtPnPrompt.setText(
					"Would you like the FlowJo AWS S3 plugin to create a bucket on the S3 server for your workspace?");
			txtPnPrompt.setBackground(SystemColor.menu);
			pnlBucketCreatePrompt.add(txtPnPrompt);

			final JButton btnYes = new JButton("Yes");
			btnYes.setBounds(190, 101, 70, 25);
			pnlBucketCreatePrompt.add(btnYes);
			btnYes.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pnlBucketNamer.setVisible(true);
					pnlBucketCreatePrompt.setVisible(false);
				}
			});

			final JButton btnNo = new JButton("No");
			btnNo.setBounds(60, 101, 70, 25);
			pnlBucketCreatePrompt.add(btnNo);
			btnNo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					// show fetching buckets prompt
					txtPnPrompt.setText("Fetching Buckets");
					// hide buttons
					btnNo.setVisible(false);
					btnYes.setVisible(false);					
										
					pnlLoading.setVisible(false);
					pnlS3Tree.setVisible(true);
				
					// timer used for the loading screen
					Timer loadTime = new Timer();
					// timer thread
					TimerTask task = new TimerTask() {
						public void run() {
							// 1 second wait time before fetching buckets
							// get the list of buckets
							populateS3FileTree();
							bucketCreatorDialog.dispose();

						}
					};
					loadTime.schedule(task, 1000);

				}
			});

			// create panel -> pnlBucketNamer
			pnlBucketNamer = new JPanel();
			getContentPane().add(pnlBucketNamer, "name_148326075721802");
			pnlBucketNamer.setLayout(null);

			// create label -> lblPleaseGiveBucketName
			final JLabel lblPleaseGiveBucketName = new JLabel("please give the bucket a name");
			lblPleaseGiveBucketName.setBounds(73, 23, 174, 16);
			pnlBucketNamer.add(lblPleaseGiveBucketName);

			// create text fiel -> txtBucketName
			txtBucketName = new JTextField();
			txtBucketName.setBounds(70, 62, 180, 22);
			pnlBucketNamer.add(txtBucketName);
			txtBucketName.setColumns(10);

			// create button -> btnOk
			final JButton btnOk = new JButton("ok");
			btnOk.setBounds(134, 107, 53, 25);
			pnlBucketNamer.add(btnOk);
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean bucketCreationSuccess = false;
					bucketCreationSuccess = m_AWSManager.createABucket(txtBucketName.getText().toLowerCase());
					// check if bucket was created successfully
					if (!bucketCreationSuccess) {
						pnlBucketNamer.setVisible(false);
						pnlError.setVisible(true);
					} else {

						btnOk.setVisible(false);
						txtBucketName.setVisible(false);
						lblPleaseGiveBucketName.setText("Fetching Buckets");

						// timer used for the loading screen
						Timer loadTime = new Timer();
						// timer thread
						TimerTask task = new TimerTask() {
							public void run() {
								// 1 second wait time before fetching buckets
								// (thread)
								// get the list of buckets
								populateS3FileTree();
								bucketCreatorDialog.dispose();
							}
						};
						loadTime.schedule(task, 1000);
					}
				}
			});

			// create panel -> pnlError
			pnlError = new JPanel();
			getContentPane().add(pnlError, "name_148578912977897");
			pnlError.setLayout(null);

			// create button -> btnOk2
			JButton btnOk2 = new JButton("ok");
			btnOk2.setBounds(134, 119, 53, 25);
			pnlError.add(btnOk2);
			btnOk2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pnlError.setVisible(false);
					pnlBucketCreatePrompt.setVisible(true);
				}
			});

			// create text pane -> txtpnInvalidBucketName
			JTextPane txtpnInvalidBucketName = new JTextPane();
			txtpnInvalidBucketName.setEditable(false);
			txtpnInvalidBucketName.setBackground(SystemColor.menu);
			txtpnInvalidBucketName.setText(
					"invalid bucket name\r\n\r\neither a bucket with that name exists already,\r\nor you do not have permission to create buckets on this S3 server");
			txtpnInvalidBucketName.setBounds(16, 13, 293, 92);
			pnlError.add(txtpnInvalidBucketName);
		}
	}
}