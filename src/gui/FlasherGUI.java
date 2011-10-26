package gui;

import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.IOException;
import java.util.*;
import java.net.URI;
import foxtrot.Job;
import foxtrot.Worker;
import javax.swing.JButton;
import org.adb.APKUtility;
import org.adb.AdbUtility;
//import org.apache.commons.io.IOUtils;
import org.logger.MyLogger;
import org.plugins.PluginActionListener;
import org.plugins.PluginInterface;
import org.system.AdbShell;
import org.system.ClassPath;
import org.system.CommentedPropertiesFile;
import org.system.Device;
import org.system.DeviceEntry;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.OsRun;
import org.system.PropertiesFile;
import org.system.RunStack;
import org.system.Shell;
import org.system.TextFile;
import java.util.Iterator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextPane;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.lang.Language;
import flashsystem.Bundle;
import flashsystem.BundleException;
import flashsystem.SeusSinTool;
import flashsystem.X10flash;
import gui.EncDecGUI.MyFile;
import javax.swing.JProgressBar;
import java.awt.SystemColor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;

public class FlasherGUI extends JFrame {

	/**
	 * 
	 */
	private static String fsep = OS.getFileSeparator();
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	static JTextPane textArea = new JTextPane();
	private Bundle bundle;
	private final ButtonGroup buttonGroupLog = new ButtonGroup();
	private final ButtonGroup buttonGroupLang = new ButtonGroup();
	private static JButton flashBtn;
	private static JButton btnRoot;
	private static JButton btnAskRootPerms;
	private static JButton btnCleanroot;
	private static JButton custBtn;
	private static JButton btnXrecovery;
	private static JButton btnKernel;
	private static JMenuItem mntmInstallBusybox;
	private static JMenuItem mntmDumpProperties;
	private static JMenuItem mntmClearCache;
	private static JMenuItem mntmBuildpropEditor;
	private static JMenuItem mntmBuildpropRebrand;

	private static JMenuItem mntmSetDefaultRecovery;
	private static JMenuItem mntmRebootDefaultRecovery;
	private static JMenuItem mntmRebootIntoRecoveryT;
	
	private static JMenuItem mntmSetDefaultKernel;
	private static JMenuItem mntmRebootCustomKernel;
	private static JMenuItem mntmRebootDefaultKernel;
	
	private static JMenuItem mntmRootPsneuter;
	private static JMenuItem mntmRootzergRush;
	//private static JMenuItem mntmCleanUninstalled;
	//private static JMenuItem mntmRecoveryControler;
	private static JMenuItem mntmBackupSystemApps;
	//private static JMenuItem mntmInstallBootkit;
	private String lang;
	public static FlasherGUI _root;
	private static AdbShell adb;
	private static Thread adbWatchdog;
	private static JMenu mnPlugins;
	/**
	 * Launch the application.
	 */
	
	private static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {}
	}

	private static void initLogger() throws FileNotFoundException {
		MyLogger.appendTextArea(textArea);
		MyLogger.setLevel(GlobalConfig.getProperty("loglevel").toUpperCase());
	}

	static public void runAdb() throws Exception {
		if (OS.getName().equals("linux")) {
			OsRun giveRights = new OsRun("chmod 755 ./x10flasher_lib/adb");
			giveRights.run();
		}
		killAdb();
		adbWatchdog = new Thread() {
			public void run() {
				try {
					adb = new AdbShell();
					adb.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		adbWatchdog.start();
	}

	public static void main(String[] args) throws Exception {
		String userdir = System.getProperty("user.dir");
		String pathsep = System.getProperty("path.separator");
		System.setProperty("java.library.path", OS.getWinDir()+pathsep+OS.getSystem32Dir()+pathsep+userdir+fsep+"x10flasher_lib");
		initLogger();
		setSystemLookAndFeel();
		Language.Init(GlobalConfig.getProperty("language").toLowerCase());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FlasherGUI frame = new FlasherGUI();
					runAdb();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public FlasherGUI() {
		_root=this;
		setName("FlasherGUI");
		setTitle("SonyEricsson X10 Flasher by Bin4ry & Androxyde");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 832, 480);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitProgram();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setName("mnFile");
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setName("mntmExit");
		mnFile.add(mntmExit);

		JMenu mnAdvanced = new JMenu("Advanced");
		mnAdvanced.setName("mnAdvanced");
		menuBar.add(mnAdvanced);

		JMenu mnLang = new JMenu("Language");
		mnLang.setName("mnLang");
		menuBar.add(mnLang);

		Enumeration<String> listlang = Language.getLanguages();
		while (listlang.hasMoreElements()) {
			lang = listlang.nextElement();
			PropertiesFile plang = Language.getProperties(lang);
			JRadioButtonMenuItem menu = new JRadioButtonMenuItem(plang.getProperty("rdbtnmntm"+lang));
			menu.setName("rdbtnmntm"+lang);
			menu.setText(Language.getMenuItem("rdbtnmntm"+lang));
			buttonGroupLang.add(menu);
			mnLang.add(menu);
			menu.setSelected(GlobalConfig.getProperty("language").equals(lang));
			menu.addActionListener(new LangActionListener(lang,buttonGroupLang,_root));
		}

		JMenuItem mntmEncryptDecrypt = new JMenuItem("Decrypt Files");
		mntmEncryptDecrypt.setName("mntmEncryptDecrypt");
		mntmEncryptDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEncDec();
			}
		});

		mntmInstallBusybox = new JMenuItem("Install BusyBox");
		mntmInstallBusybox.setName("mntmInstallBusybox");
		mntmInstallBusybox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInstallBusyBox();
			}
		});

		mntmDumpProperties = new JMenuItem("Dump Properties");
		mntmDumpProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doDumpProperties();
				}
				catch (Exception e1) {}
			}
		});
		
		JMenu mnRoot = new JMenu("Root");
		mnAdvanced.add(mnRoot);

		mntmRootPsneuter = new JMenuItem("Force psneuter");
		mntmRootPsneuter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRootpsneuter();
			}
		});

		mntmRootzergRush = new JMenuItem("Force zergRush");
		mntmRootzergRush.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRootzergRush();
			}
		});

		mnRoot.add(mntmRootPsneuter);
		mnRoot.add(mntmRootzergRush);
		
		JMenu mnClean = new JMenu("Clean");
		mnClean.setName("mnClean");
		mnAdvanced.add(mnClean);

		mntmClearCache = new JMenuItem("Clear cache");
		mnClean.add(mntmClearCache);
		mntmClearCache.setName("mntmClearCache");

		mntmClearCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doClearCache();
			}
		});
		mntmClearCache.setEnabled(false);

		//mntmCleanUninstalled = new JMenuItem("Clean Uninstalled");
		//mnClean.add(mntmCleanUninstalled);
		//mntmCleanUninstalled.setName("mntmCleanUninstalled");
		//mntmCleanUninstalled.setEnabled(false);

		/*mntmCleanUninstalled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doCleanUninstall();
			}
		});*/

		JMenu mnXrecovery = new JMenu("xRecovery");
		mnAdvanced.add(mnXrecovery);
		
		
		mntmSetDefaultRecovery = new JMenuItem("Set default recovery");
		mntmSetDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSetDefaultRecovery();
			}
		});
		mnXrecovery.add(mntmSetDefaultRecovery);
		

		/*mntmRecoveryControler = new JMenuItem("Recovery Controler");
		mntmRecoveryControler.setName("mntmRecoveryControler");
		mntmRecoveryControler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RecovControl control = new RecovControl();
				control.setVisible(true);
			}
		});
		mnXrecovery.add(mntmRecoveryControler);
		mntmRecoveryControler.setEnabled(false);*/
		

		JMenu mnKernel = new JMenu("Kernel");
		mnAdvanced.add(mnKernel);

		//mntmInstallBootkit = new JMenuItem("Install bootkit");
		//mntmInstallBootkit.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		doInstallBootKit();
		//	}
		//});
		//mntmInstallBootkit.setEnabled(false);
		//mnKernel.add(mntmInstallBootkit);
		
		mntmBackupSystemApps = new JMenuItem("Backup System Apps");
		mntmBackupSystemApps.setName("mntmBackupSystemApps");
		mnAdvanced.add(mntmBackupSystemApps);
		mntmBackupSystemApps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBackupSystem();
			}
		});
		mntmBackupSystemApps.setEnabled(false);


		/*JMenuItem mntmInstallOnline = new JMenuItem("Download latest version");
		mntmInstallOnline.setName("mntmInstallOnline");
		mntmInstallOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doDownloadXRecovery();
			}
		});
		mnXrecovery.add(mntmInstallOnline);*/
		mnAdvanced.add(mntmInstallBusybox);
		mnAdvanced.add(mntmDumpProperties);

		mntmBuildpropEditor = new JMenuItem("Build.prop Editor");
		mntmBuildpropEditor.setName("mntmBuildpropEditor");
		mntmBuildpropEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BuildPropGUI propsEdit = new BuildPropGUI();
				propsEdit.setVisible(true);
			}
		});
		mnAdvanced.add(mntmBuildpropEditor);

		mntmBuildpropRebrand = new JMenuItem("Rebrand");
		//mntmBuildpropRebrand.setName("mntmBuildpropEditor");
		mntmBuildpropRebrand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebrand();
			}
		});
		mnAdvanced.add(mntmBuildpropRebrand);
		
		mnAdvanced.add(mntmEncryptDecrypt);

		/*JMenuItem mntmFilemanager = new JMenuItem("FileManager");
		mntmFilemanager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileManager manager = new FileManager();
					manager.setVisible(true);
				}
				catch (Exception emanager) {}
			}
		});
		mnAdvanced.add(mntmFilemanager);*/

		JMenuItem mntmBundleCreation = new JMenuItem("Bundle Creation");
		mntmBundleCreation.setName("mntmBundleCreation");
		mntmBundleCreation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBundle();
			}
		});
		mnAdvanced.add(mntmBundleCreation);

		JMenu mnReboot = new JMenu("Reboot");
		mnAdvanced.add(mnReboot);
		
		JMenu mnRRecovery = new JMenu("Recovery");
		JMenu mnRKernel = new JMenu("Kernel");
		mnReboot.add(mnRRecovery);
		mnReboot.add(mnRKernel);

		mntmRebootDefaultRecovery = new JMenuItem("Reboot default version");
		mntmRebootDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecovery();
			}
		});
		mnRRecovery.add(mntmRebootDefaultRecovery);

		mntmRebootIntoRecoveryT = new JMenuItem("Reboot specific version");
		mntmRebootIntoRecoveryT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecoveryT();
			}
		});
		mnRRecovery.add(mntmRebootIntoRecoveryT);

		mntmRebootIntoRecoveryT.setEnabled(false);
		mntmRebootDefaultRecovery.setEnabled(false);
		mntmSetDefaultRecovery.setEnabled(false);

		mntmSetDefaultKernel = new JMenuItem("Set default kernel");
		mntmSetDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSetDefaultKernel();
			}
		});
		mnKernel.add(mntmSetDefaultKernel);
		mntmSetDefaultKernel.setEnabled(false);

		mntmRebootDefaultKernel = new JMenuItem("Reboot default version");
		mntmRebootDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doReboot();
			}
		});
		mnRKernel.add(mntmRebootDefaultKernel);

		mntmRebootDefaultKernel.setEnabled(false);
		mntmRebootCustomKernel = new JMenuItem("Reboot specific version");
		mntmRebootCustomKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebootKexec();
			}
		});
		mnRKernel.add(mntmRebootCustomKernel);
		mntmRebootCustomKernel.setEnabled(false);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setName("mnHelp");
		menuBar.add(mnHelp);
		mnPlugins = new JMenu("Plugins");
		menuBar.add(mnPlugins);

		JMenu mnLoglevel = new JMenu("Loglevel");
		mnLoglevel.setName("mnLoglevel");
		mnHelp.add(mnLoglevel);

		/*JMenuItem mntmTestFlashMode = new JMenuItem("Test Flash Mode");
		mntmTestFlashMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doTestFlash();
			}
		});
		mnHelp.add(mntmTestFlashMode);*/

		JMenuItem mntmCheckDrivers = new JMenuItem("Check Drivers");
		mntmCheckDrivers.setName("mntmCheckDrivers");
		mntmCheckDrivers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Device.CheckAdbDrivers();
			}
		});
		mnHelp.add(mntmCheckDrivers);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setName("mntmAbout");
		mnHelp.add(mntmAbout);	

		JRadioButtonMenuItem rdbtnmntmError = new JRadioButtonMenuItem("errors");
		rdbtnmntmError.setName("mntmError");
		buttonGroupLog.add(rdbtnmntmError);
		mnLoglevel.add(rdbtnmntmError);
		rdbtnmntmError.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("ERROR"));

		JRadioButtonMenuItem rdbtnmntmWarnings = new JRadioButtonMenuItem("warnings");
		rdbtnmntmWarnings.setName("mntmWarnings");
		buttonGroupLog.add(rdbtnmntmWarnings);
		mnLoglevel.add(rdbtnmntmWarnings);
		rdbtnmntmWarnings.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("WARN"));

		JRadioButtonMenuItem rdbtnmntmInfos = new JRadioButtonMenuItem("infos");
		rdbtnmntmInfos.setName("mntmInfos");
		buttonGroupLog.add(rdbtnmntmInfos);
		mnLoglevel.add(rdbtnmntmInfos);
		rdbtnmntmInfos.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("INFO"));

		JRadioButtonMenuItem rdbtnmntmDebug = new JRadioButtonMenuItem("debug");
		rdbtnmntmDebug.setName("mntmDebug");
		buttonGroupLog.add(rdbtnmntmDebug);
		mnLoglevel.add(rdbtnmntmDebug);
		rdbtnmntmDebug.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("DEBUG"));

		rdbtnmntmError.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("ERR");
			}
		});
		
		rdbtnmntmWarnings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("WARN");
			}
		});
		
		rdbtnmntmInfos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("INFO");
			}
		});

		rdbtnmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("DEBUG");
			}
		});

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RunStack.killAll();
				exitProgram();
			}
		});

		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				About about = new About();
				about.setVisible(true);
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(290dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(33dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(75dlu;default)"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));						
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, "2, 2, 17, 1");

		flashBtn = new JButton("");
		flashBtn.setToolTipText("Flash");
		flashBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/lighting_icon&32.png")));
		toolBar.add(flashBtn);
		
				btnRoot = new JButton("");
				btnRoot.setToolTipText("Root");
				btnRoot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/lock_open_icon&32.png")));
				toolBar.add(btnRoot);
				btnRoot.setEnabled(false);
				
						btnAskRootPerms = new JButton("");
						btnAskRootPerms.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/key_icon&32.png")));
						btnAskRootPerms.setToolTipText("Ask Root Perms");
						toolBar.add(btnAskRootPerms);
						btnAskRootPerms.setBackground(SystemColor.control);
						btnAskRootPerms.setEnabled(false);
						
								btnCleanroot = new JButton("");
								btnCleanroot.setToolTipText("Clean (Root Needed)");
								btnCleanroot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/trash_icon&32.png")));
								toolBar.add(btnCleanroot);
								
										btnCleanroot.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent arg0) {
												doCleanRoot();
											}
										});
										btnCleanroot.setEnabled(false);
										custBtn = new JButton("");
										custBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/redo_icon&32.png")));
										custBtn.setToolTipText("APK Installer");
										toolBar.add(custBtn);
										custBtn.setEnabled(false);
														
														btnXrecovery = new JButton("");
														btnXrecovery.setToolTipText("Recovery Installer");
														btnXrecovery.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/sq_plus_icon&32.png")));
														toolBar.add(btnXrecovery);
														btnXrecovery.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallCustKit();
																doInstallXRecovery();
															}
														});
														btnXrecovery.setEnabled(false);
														
														btnKernel = new JButton("");
														btnKernel.setToolTipText("Kernel Installer");
														btnKernel.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/wrench_icon&32.png")));
														toolBar.add(btnKernel);
														btnKernel.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallCustKit();
																doInstallKernel();
															}
														});
														btnKernel.setEnabled(false);
										
												custBtn.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent arg0) {
														doCustomize();
													}
												});
						btnAskRootPerms.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								doAskRoot();
							}
						});
				
						btnRoot.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								doRoot();
							}
						});
		flashBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					doFlash();
				}
				catch (Exception eflash) {}
			}
		});
		
		JToolBar toolBar_1 = new JToolBar();
		contentPane.add(toolBar_1, "22, 2");
		
		JButton btnDonate = new JButton("");
		toolBar_1.add(btnDonate);
		btnDonate.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/paypal_icon.png")));
		btnDonate.setToolTipText("Donate");
		//btnDonate.setName("btnDonate");
		btnDonate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doConnectPaypal();
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "2, 8, 21, 1, fill, fill");
		
		scrollPane.setViewportView(textArea);
		
		JButton btnSaveLog = new JButton("Save log");
		btnSaveLog.setName("btnSaveLog");
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyLogger.writeFile();
			}
		});
		contentPane.add(btnSaveLog, "22, 10, right, default");
		
		JProgressBar progressBar = new JProgressBar();
		MyLogger.registerProgressBar(progressBar);
		contentPane.add(progressBar, "2, 12, 21, 1");
		setLanguage();
		mntmInstallBusybox.setEnabled(false);
		mntmBuildpropEditor.setEnabled(false);
		mntmBuildpropRebrand.setEnabled(false);
	}


	public void setLanguage() {
		Language.translate(this);
	}
		
	public void exitProgram() {
		try {
			if (GlobalConfig.getProperty("killadbonexit").equals("yes")) {
				killAdb();
			}
			System.exit(0);
		}
		catch (Exception e) {}		
	}

	public static void killAdb() {
		try {
			if (OS.getName().equals("linux")) {
				OsRun cmd = new OsRun("/usr/bin/killall adb");
				cmd.run();				
			}
			else {
				OsRun cmd = new OsRun("taskkill /F /T /IM adb*");
				cmd.run();
			}
		}
		catch (Exception e) {}
	}

	public void doCleanUninstall() {
		Worker.post(new Job() {
			public Object run() {
				try {
						PropertiesFile safeList = new PropertiesFile("org/adb/config/safelist.properties","."+fsep+"custom"+fsep+"clean"+fsep+"safelist.properties");
						HashSet<String> set = AdbUtility.listSysApps();
						Iterator<Object> keys = safeList.keySet().iterator();
						while (keys.hasNext()) {
							String key = (String)keys.next();
							if (safeList.getProperty(key).equals("safe") && !set.contains(key)) {
								MyLogger.debug(key);
								if (TextFile.exists("."+fsep+"custom"+fsep+"apps_saved"+fsep+key)) {
									String packageName = APKUtility.getPackageName("."+fsep+"custom"+fsep+"apps_saved"+fsep+key);
									MyLogger.debug(packageName);
									AdbUtility.uninstall(packageName,false);
								}
							}
						}
						MyLogger.info("Clean Finished");
				} catch (Exception e) {}
				return null;
			}
		});
	}

/*	public void doTestFlash() {
			Worker.post(new Job() {
				public Object run() {
					try {
						X10flash flash = new X10flash();
						if (flash.openDevice(false))
							MyLogger.info("Phone successfully turned to flash mode");
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
	}*/

	public void doDumpProperties() throws Exception {
		
		//firmSelect sel = new firmSelect(config);
		//bundle = sel.getBundle();
		bundle = new Bundle();
		if (bundle!=null) {
				Worker.post(new Job() {
					public Object run() {
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							if (!OS.getName().equals("windows")) bundle.setSimulate(true);
							X10flash flash = new X10flash(bundle);
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.dumpProperties();
							}
						}
						catch (Exception e) {
							MyLogger.error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}

	public void doFlash() throws Exception {
		firmSelect sel = new firmSelect();
		bundle = sel.getBundle();
		if (bundle!=null) {
			if (!bundle.hasLoader())
				bundle.setLoader(new File(Devices.getCurrent().getLoader()));
				Worker.post(new Job() {
					public Object run() {
						try {
				    		MyLogger.info("Preparing files for flashing");
				    		bundle.open();
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							if (!OS.getName().equals("windows")) bundle.setSimulate(true);
							X10flash flash = new X10flash(bundle);
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.flashDevice();
								MyLogger.info("Now unplug the device and power it on");
								MyLogger.info("Then go to application settings");
								MyLogger.info("turn on Unknown Sources and Debugging");
								doDisableIdent();
							}
						}
						catch (BundleException ioe) {
							MyLogger.error("Error preparing files");
						}
						catch (Exception e) {
							MyLogger.error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}

	public void doRoot() {
		if (Devices.getCurrent().getVersion().contains("2.3")) doRootzergRush();
		else 
			doRootpsneuter();
	}
	
	public void doRootzergRush() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"zergrush.tar",GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.info("Running part1 of Root Exploit, please wait");
					shell.run(true);
					Thread.sleep(10000);
					MyLogger.info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.info("Finished!.");
					MyLogger.info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.error(e.getMessage());}
				return null;
			}
		});
	}
	
	public void doRootpsneuter() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"psneuter.tar",GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.info("Running part1 of Root Exploit, please wait");
					shell.run(false);
					Thread.sleep(10000);
					MyLogger.info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.info("Finished!.");
					MyLogger.info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.error(e.getMessage());}
				return null;
			}
		});
	}

	public void doCustomize() {
		Worker.post(new Job() {
			public Object run() {
				try {
						ApkInstallGUI instgui = new ApkInstallGUI("."+fsep+"custom"+fsep+"apps");
						String folder = instgui.getFolder();
						if (folder.length()>0) {
							File files = new File(folder);
							File[] chld = files.listFiles();
							for(int i = 0; i < chld.length; i++){
								if (chld[i].getName().endsWith(".apk"))
									org.adb.AdbUtility.install(chld[i].getPath());
							}
							MyLogger.info("APK Installation finished");
						}
						else MyLogger.info("APK Installation canceled");
					}
				catch (Exception e) {}
				return null;
			}
		});
	}

	public void doRebrand() {
		Worker.post(new Job() {
			public Object run() {
			try {
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.pull("/system/build.prop", Devices.getCurrent().getWorkDir()+fsep+".");
							CommentedPropertiesFile build = new CommentedPropertiesFile();
							build.load(new File(Devices.getCurrent().getWorkDir()+fsep+"build.prop"));
							String current = build.getProperty("ro.semc.version.cust");
							if (current!=null) {
								rebrandGUI gui = new rebrandGUI(current);
								String newid = gui.getId();
								if (newid.length()>0) {							
									build.store(new FileOutputStream(new File(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop")), "");
									TextFile tf = new TextFile(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop","ISO-8859-1");
									tf.setProperty(current,newid);
									AdbUtility.push(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop",GlobalConfig.getProperty("deviceworkdir")+"/build.prop");
									Shell shell = new Shell("rebrand");
									shell.runRoot();
									MyLogger.info("Rebrand finished. Rebooting phone ...");
								}
							}
							else {MyLogger.error("You are not on a stock ROM");}
						}
						else MyLogger.error("Error mounting /system rw");
				}
				catch (Exception e) {MyLogger.error(e.getMessage());e.printStackTrace();}
				return null;
			}
		});
	}
	
	public void doCleanRoot() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().doBusyboxHelper();
					if (AdbUtility.Sysremountrw()) {
					apkClean sel = new apkClean();
					sel.setVisible(true);
					boolean somethingdone = false;
						if (TextFile.exists("."+fsep+"custom"+fsep+"clean"+fsep+"listappsadd")) {
							AdbUtility.push("."+fsep+"custom"+fsep+"clean"+fsep+"listappsadd", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile("."+fsep+"custom"+fsep+"clean"+fsep+"listappsadd","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.push("."+fsep+"custom"+fsep+"apps_saved"+fsep+""+i.next(), GlobalConfig.getProperty("deviceworkdir"));
							}
							t.delete();
							Shell shell1 = new Shell("sysadd");
							shell1.runRoot();
							somethingdone = true;
						}
						if (TextFile.exists("."+fsep+"custom"+fsep+"clean"+fsep+"listappsremove")) {
							AdbUtility.push("."+fsep+"custom"+fsep+"clean"+fsep+"listappsremove", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile("."+fsep+"custom"+fsep+"clean"+fsep+"listappsremove","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.pull("/system/app/"+i.next(),"."+fsep+"custom"+fsep+"apps_saved");
							}
							Shell shell2 = new Shell("sysremove");
							shell2.runRoot();
							t.delete();
							somethingdone = true;
							
						}
						if (somethingdone) {
							AdbUtility.clearcache();
							MyLogger.info("Clean finished. Rebooting phone ...");
						}
						else MyLogger.info("Clean canceled");
					}
					else MyLogger.info("Error mounting /system rw");
				} catch (Exception e) {}
				return null;
			}
		});
	}
	
	public void doRebootRecoveryT() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().rebootSelectedRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().setDefaultRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI rsel = new KernelBootSelectGUI();
					String current = rsel.getVersion();
					if (current.length()>0) {
						if (AdbUtility.Sysremountrw()) {
						MyLogger.info("Setting default kernel");
						Shell shell = new Shell("setdefaultkernel");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.info("Done");
						}
					}
				}
				catch (Exception e) {
					MyLogger.error(e.getMessage());
				}
				return null;
			}
		});		
	}

	public void doRebootRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					MyLogger.info("Rebooting into recovery mode");
					Shell shell = new Shell("rebootrecovery");
					shell.runRoot();
					MyLogger.info("Phone will reboot into recovery mode");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doRebootKexec() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI ksel = new KernelBootSelectGUI();
					String current = ksel.getVersion();
					if (current.length()>0) {
						MyLogger.info("Rebooting into kexec mode");
						Shell shell = new Shell("rebootkexect");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.info("Phone will reboot into kexec mode");
					}
					else {
						MyLogger.info("Reboot canceled");
					}
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doReboot() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.info("Rebooting into stock mode");
						Shell shell = new Shell("reboot");
						shell.runRoot();
						MyLogger.info("Phone will reboot now");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doInstallXRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.info("Installing xRecovery to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
						RecoverySelectGUI sel = new RecoverySelectGUI(Devices.getCurrent().getId());
						String selVersion = sel.getVersion();
						if (selVersion.length()>0) {
							AdbUtility.push("./devices/"+Devices.getCurrent().getId()+"/recovery/"+selVersion+"/recovery.tar",GlobalConfig.getProperty("deviceworkdir")+"/recovery.tar");
							Shell shell = new Shell("installrecovery");
							shell.runRoot();
							MyLogger.info("xRecovery successfully installed");
						}
						}
						else MyLogger.error("Error mounting /system rw");
					}
				catch (Exception e) {MyLogger.error(e.getMessage());}
				return null;
			}
		});
	}	

    public void doEncDec() {
		Worker.post(new Job() {
			public Object run() {
	        	EncDecGUI encdec = new EncDecGUI();
	        	encdec.setVisible(true);
	        	Object[] list = encdec.getList();
	        	if (list!=null) {
	        		String folder=null;
    				for (int i=0;i<list.length;i++) {
    					MyLogger.info("Decrypting "+list[i]);
    					folder = ((MyFile)list[i]).getParent();
    	        		SeusSinTool.decrypt(((MyFile)list[i]).getAbsolutePath());
    				}

    				MyLogger.info("Decryption finished");
    				try {
					BundleGUI bcre = new BundleGUI(folder);
					Bundle b = bcre.getBundle();
					if (b!=null) {
    					MyLogger.info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.info("Finished bundle creation");
					}
    				}
    				catch (Exception e) {}
	        	}
	 			return null;
			}
		});
   }

    public void doInstallBusyBox() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
	        		String busybox = Devices.getCurrent().getBusybox(true);
	        		if (busybox.length()>0) {
		        		AdbUtility.push(busybox, GlobalConfig.getProperty("deviceworkdir"));
		        		Shell shell = new Shell("busyhelper");
		        		shell.run(false);
		        		shell = new Shell("instbusybox");
						shell.setProperty("BUSYBOXINSTALLPATH", Devices.getCurrent().getBusyBoxInstallPath());
						shell.runRoot();
				        MyLogger.info("Installed version of busybox : " + AdbUtility.getBusyboxVersion(Devices.getCurrent().getBusyBoxInstallPath()));
				        MyLogger.info("Finished");
	        		}
	        		else {
	        			MyLogger.info("Busybox installation canceled");
	        		}
		        }
	        	catch (Exception e) {MyLogger.error(e.getMessage());}
	 			return null;
			}
		});
    }

    public void doClearCache() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
						AdbUtility.clearcache();
						MyLogger.info("Finished");
				}
				catch (Exception e) {}
	 			return null;
			}
		});
	}

    public void doConnectPaypal() {
    	showInBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PPWH7M9MNCEPA");
    }

    private static boolean showInBrowser(String url){
		try {
			  Desktop.getDesktop().browse(new URI(url));
		} 
		catch (Exception e) {
		} 
        return true;
        // some mod here
	}

    public static void doDisableIdent() {
		btnCleanroot.setEnabled(false);
		mntmInstallBusybox.setEnabled(false);
		mntmClearCache.setEnabled(false);
		mntmRootzergRush.setEnabled(false);
		mntmRootPsneuter.setEnabled(false);
		mntmBuildpropEditor.setEnabled(false);
		mntmBuildpropRebrand.setEnabled(false);
		mntmRebootIntoRecoveryT.setEnabled(false);
		mntmRebootDefaultRecovery.setEnabled(false);
		mntmSetDefaultRecovery.setEnabled(false);
		mntmSetDefaultKernel.setEnabled(false);
		mntmRebootCustomKernel.setEnabled(false);
		mntmRebootDefaultKernel.setEnabled(false);
		//mntmInstallBootkit.setEnabled(false);
		btnRoot.setEnabled(false);
		btnXrecovery.setEnabled(false);
		btnKernel.setEnabled(false);
		btnAskRootPerms.setEnabled(false);
		custBtn.setEnabled(false);
		//mntmCleanUninstalled.setEnabled(false);
    		mntmBackupSystemApps.setEnabled(false);
        }
 
        public static void doIdent() {
        	Enumeration e = Devices.listDevices(true);
        	boolean found = false;
        	while (e.hasMoreElements() && !found) {
        		DeviceEntry current = Devices.getDevice((String)e.nextElement());
        		String dev = AdbUtility.getProperty(current.getBuildProp());
        		Iterator<String> i = current.getRecognitionList().iterator();
        		while (i.hasNext() && !found) {
        			if (dev.toUpperCase().contains(i.next().toUpperCase())) {
        				dev = current.getId();
        				found = true;
        				Devices.setCurrent(current.getId());
        				MyLogger.info("Connected device : " + Devices.getCurrent().getId());
        			}
        		}
        	}
        	if (!found) {
        		MyLogger.error("Cannot identify your device.");
        		if (Devices.listDevices(false).hasMoreElements()) {
        		MyLogger.info("Selecting from user input");
        		deviceSelectGui devsel = new deviceSelectGui(null);
        		String dev = devsel.getDevice();
        		if (dev.length()>0) {
        			found = true;
        			Devices.setCurrent(dev);
        			MyLogger.info("Connected device : " + Devices.getCurrent().getId());
        		}
        		}
        	}
    	if (found) {
    		MyLogger.info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion());
    		MyLogger.info("Android version : "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion());
			if (Devices.getCurrent().hasRoot()) doGiveRoot();
    		if (Devices.getCurrent().isRecovery()) MyLogger.info("Phone in recovery mode");
    		btnRoot.setEnabled(true);
    		mntmRootzergRush.setEnabled(true);
    		mntmRootPsneuter.setEnabled(true);
    		if (Devices.getCurrent().getVersion().startsWith("2.3") && !Devices.getCurrent().getKernelVersion().contains("2.6.32"))
    			btnRoot.setEnabled(false);
    		flashBtn.setEnabled(Devices.getCurrent().canFlash());
    		btnAskRootPerms.setEnabled(!Devices.getCurrent().hasRoot());
    		custBtn.setEnabled(true);
    		//mntmCleanUninstalled.setEnabled(true);
        	Devices.stopWaitForReboot();
        	addPlugins();
    	}
	}

    public static void doGiveRoot() {
		btnCleanroot.setEnabled(true);
		mntmInstallBusybox.setEnabled(true);
		mntmClearCache.setEnabled(true);
		mntmBuildpropEditor.setEnabled(true);
		if (new File("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"rebrand").isDirectory())
			mntmBuildpropRebrand.setEnabled(true);
		mntmRebootIntoRecoveryT.setEnabled(Devices.getCurrent().canRecovery());
		mntmRebootDefaultRecovery.setEnabled(Devices.getCurrent().canRecovery());
		mntmSetDefaultRecovery.setEnabled(Devices.getCurrent().canRecovery());
		mntmSetDefaultKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootCustomKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootDefaultKernel.setEnabled(Devices.getCurrent().canKernel());
		//mntmInstallBootkit.setEnabled(true);
		//mntmRecoveryControler.setEnabled(true);
		mntmBackupSystemApps.setEnabled(true);
		btnXrecovery.setEnabled(Devices.getCurrent().canRecovery());
		btnKernel.setEnabled(Devices.getCurrent().canKernel());
		MyLogger.info("Root Access Allowed");    	
    }
    
    public static void doAskRoot() {
		Worker.post(new Job() {
			public Object run() {
				MyLogger.warn("Please check your Phone and 'ALLOW' Superuseraccess!");
        		if (!AdbUtility.hasRootPerms()) {
        			MyLogger.error("Please Accept root permissions on the phone");
        		}
        		else {
        			doGiveRoot();
        		}
        		return null;
			}
		});
	}

    public void doBundle() {
		Worker.post(new Job() {
			public Object run() {
				try {
					BundleGUI bcre = new BundleGUI();
					Bundle b = bcre.getBundle();
					if (b!=null) {
    					MyLogger.info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.info("Finished bundle creation");
					}
				}
				catch (Exception e) {MyLogger.error(e.getMessage());}
				return null;
			}
		});
    }


    public void doBackupSystem() {
		Worker.post(new Job() {
			public Object run() {
				try {
					X10Apps apps = new X10Apps();
					Iterator<String> ic = apps.getCurrent().iterator();
					while (ic.hasNext()) {
						String app = ic.next();
						try {
							AdbUtility.pull("/system/app/"+app, "."+fsep+"custom"+fsep+"apps_saved");
						}
						catch (Exception e) {}
					}
					MyLogger.info("Backup Finished");
				}
				catch (Exception e) {MyLogger.error(e.getMessage());}
				return null;
			}
		});
	}

    public void doInstallCustKit() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.info("Installing chargemon feature / kernel bootkit to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"bootkit"+fsep+"bootkit.tar",GlobalConfig.getProperty("deviceworkdir"));
							Shell shell = new Shell("installbootkit");
							shell.runRoot();
							MyLogger.info("bootkit successfully installed");
						}
						else MyLogger.error("Error mounting /system rw");
					}
				catch (Exception e) {MyLogger.error(e.getMessage());}
				return null;
			}
		});
    }

    public void doInstallKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.info("Installing kernel to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							KernelSelectGUI sel = new KernelSelectGUI(Devices.getCurrent().getId());
							String selVersion = sel.getVersion();
							if (selVersion.length()>0) {
								AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"kernel"+fsep+selVersion+fsep+"kernel.tar",GlobalConfig.getProperty("deviceworkdir"));
								Shell shell = new Shell("installkernel");
								shell.runRoot();
								MyLogger.info("kernel successfully installed");
							}
						}
						else MyLogger.error("Error mounting /system rw");
					}
				catch (Exception e) {MyLogger.error(e.getMessage());}
				return null;
			}
		});
    }
    
    public static void addPlugins() {
    	mnPlugins.removeAll();
    	File dir = new File("./devices/"+Devices.getCurrent().getId()+"/features");
	    File[] chld = dir.listFiles();
	    for(int i = 0; i < chld.length; i++){
	    	if (chld[i].isDirectory()) {
	    		try {
	    			Properties p = new Properties();
	    			p.load(new FileInputStream(new File(chld[i].getPath()+fsep+"feature.properties")));
	    			ClassPath.addFile(chld[i].getPath()+fsep+p.getProperty("plugin"));
	    			registerPlugin(p.getProperty("classname"),chld[i].getPath());
	    		}
	    		catch (IOException ioe) {
	    		}
	    	}
	    }
    }
    
    public static void registerPlugin(String classname,String workdir) {
	    try {
	    	Class pluginClass = Class.forName(classname);
            Constructor constr = pluginClass.getConstructor();
            PluginInterface pluginObject = (PluginInterface)constr.newInstance();
            JMenuItem item = new JMenuItem(pluginObject.getName());
            pluginObject.setWorkdir(workdir);
            PluginActionListener p =  new PluginActionListener(pluginObject);
            item.addActionListener(p);
            mnPlugins.add(item);
	    }
	    catch (Exception e) {
	    	MyLogger.error(e.getMessage());
	    }
    }
}