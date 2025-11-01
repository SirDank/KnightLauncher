package com.lucasluqui.launcher;

import com.google.inject.*;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.editor.EditorsGUI;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Status;
import com.lucasluqui.launcher.mod.ModListGUI;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsGUI;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.util.*;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.lucasluqui.launcher.Log.log;

@Singleton
public class LauncherApp
{
  @Inject protected LauncherContext _launcherCtx;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected LocaleManager _localeManager;
  @Inject protected ModManager _modManager;
  @Inject protected FlamingoManager _flamingoManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;
  @Inject protected ModuleManager _moduleManager;
  @Inject protected CacheManager _cacheManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected KeyboardController _keyboardController;

  private final String[] args;
  private final Injector injector;

  public LauncherApp ()
  {
    this.args = null;
    this.injector = null;
  }

  public LauncherApp (String[] args, Injector injector)
  {
    this.args = args;
    this.injector = injector;
  }

  public static void main (String[] args)
  {
    Injector injector = Guice.createInjector(new LauncherModule());
    LauncherApp app = new LauncherApp(args, injector);
    injector.injectMembers(app);

    app.init();
  }

  @SuppressWarnings("ConstantConditions")
  private void init ()
  {
    setupLogging();
    log.info(BuildConfig.getName() + " started. Running version: " + BuildConfig.getVersion());

    logVMInfo();
    logGameVMInfo();
    logHostInfo();
    setupHTTPSProtocol();
    checkTempDir();
    checkDirectories();

    initManagers();

    Fonts.setup();
    Stylesheet.setup();

    checkStartLocation();
    if (SystemUtil.isWindows() || SystemUtil.isUnix()) checkShortcut();

    initInterfaces();

    if (!this.requiresJVMPatch() && !this.requiresUpdate()) {
      initFinished();
      ThreadingUtil.executeWithDelay(_launcherCtx.launcherGUI::switchVisibility, 200);
    }
  }

  private void initManagers ()
  {
    _launcherCtx.init();
    _settingsManager.init();
    _localeManager.init();
    _modManager.init();
    _flamingoManager.init();
    _moduleManager.init();
    _cacheManager.init();
    _downloadManager.init();
    _keyboardController.init();

    if (SystemUtil.isARM() || SystemUtil.isMac()) {
      _discordPresenceClient.init("0", true);
    } else {
      _discordPresenceClient.init(LauncherGlobals.RPC_CLIENT_ID, false);
    }
  }

  private void initInterfaces ()
  {
    if (this.requiresJVMPatch()) {
      this.initJVMPatcher();
    } else if (this.requiresUpdate()) {
      this.initUpdater();
    } else {
      this.initLauncherGUI();
      this.initSettingsGUI();
      this.initModListGUI();
      this.initEditorsGUI();
    }
  }

  private void initFinished ()
  {
    _moduleManager.loadModules();

    checkGetdown();

    // if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) {
    //   _modManager.extractSafeguard();
    // }

    _launcherCtx.launcherGUI.eventHandler.updateServerList(null);

    loadOnlineAssets();

    // Only re-check installed mods if we got a different selected server.
    if (!_flamingoManager.getSelectedServer().isOfficial())
      new Thread(_modManager::checkInstalled).start();

    _discordPresenceClient.setDetails(_localeManager.getValue("presence.ready"));
  }

  private void initLauncherGUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.launcherGUI = injector.getInstance(LauncherGUI.class);
          _launcherCtx.launcherGUI.init();
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initSettingsGUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.settingsGUI = injector.getInstance(SettingsGUI.class);
          _launcherCtx.settingsGUI.init();
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initModListGUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.modListGUI = injector.getInstance(ModListGUI.class);
          _launcherCtx.modListGUI.init();
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initEditorsGUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.editorsGUI = injector.getInstance(EditorsGUI.class);
          _launcherCtx.editorsGUI.init();
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initJVMPatcher ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.jvmPatcher = injector.getInstance(JVMPatcher.class);

          final String path;
          final boolean legacy;

          if (args.length > 1) {
            // If there are more than one argument, it means this is a forced JVM patch, and there's extra info we should parse.
            // Set the path dir to wherever we're being forced to patch to, and set legacy to only allow legacy JVMs.
            // This is primarily used for patching when a third party server was selected.
            path = args[1];
            legacy = Boolean.parseBoolean(args[2]);
          } else {
            // Organic JVM patch.
            path = LauncherGlobals.USER_DIR;

            // If the game's Java VM version is 8 or lower, mark it as legacy so that only
            // legacy Java VMs are offered to patch.
            legacy = JavaUtil.isLegacy();
          }
          _launcherCtx.jvmPatcher.init(path, legacy);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initUpdater ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          _launcherCtx.updater = injector.getInstance(Updater.class);
          _launcherCtx.updater.init(this.args[1]);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void checkDirectories ()
  {
    // Stores all mods.
    FileUtil.createDir("mods");

    // Stores clients for third-party servers.
    FileUtil.createDir("thirdparty");

    // Miscellaneous image assets for the launcher to use.
    FileUtil.createDir("KnightLauncher/images/");

    // External modules necessary for extra functionality (e.g., RPC)
    FileUtil.createDir("KnightLauncher/modules/");

    // Check if the deprecated "code-mods" folder exists, in that case, start migrating.
    if (FileUtil.fileExists("code-mods")) migrateLegacyCodeModsFolder();
  }

  private void migrateLegacyCodeModsFolder ()
  {
    File oldCodeModsFolder = new File("code-mods");
    File[] oldCodeModsFolderFiles = oldCodeModsFolder.listFiles();
    if (oldCodeModsFolderFiles == null) {
      oldCodeModsFolder.delete();
    } else {
      for (File file : oldCodeModsFolderFiles) {
        try {
          FileUtils.moveFile(file, new File(LauncherGlobals.USER_DIR + "/mods/" + file.getName()));
        } catch (IOException e) {
          log.error(e);
        }
      }
      oldCodeModsFolder.delete();
    }
  }

  // Checking if we're being run inside the game's directory, "getdown-pro.jar" should always be present if so.
  private void checkStartLocation ()
  {
    if (!FileUtil.fileExists("./getdown-pro.jar")) {
      String pathWarning = _localeManager.getValue("error.start_location");
      if (SystemUtil.isWindows()) {
        String steamGamePath = SteamUtil.getGamePathWindows();
        if (steamGamePath != null)
          pathWarning += _localeManager.getValue("error.start_location_steam_path", steamGamePath);
      }
      log.warning(pathWarning);
      Dialog.push(pathWarning, JOptionPane.WARNING_MESSAGE);
      //if (SystemUtil.isWindows()) DesktopUtil.openDir(SteamUtil.getGamePathWindows());
    }
  }

  // Create a shortcut to the application if there's none.
  private void checkShortcut ()
  {
    if (Settings.createShortcut
      && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + BuildConfig.getName())
      && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + BuildConfig.getName() + ".desktop")) {
      BufferedImage bimg = ImageUtil.loadImageWithinJar("/rsrc/img/icon-512.png");
      try {
        if (SystemUtil.isWindows()) {
          ICOEncoder.write(bimg, new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.ico"));
        } else {
          File outputfile = new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png");
          ImageIO.write(bimg, "png", outputfile);
        }
      } catch (IOException e) {
        log.error(e);
      }

      if (SystemUtil.isWindows()) {
        makeShellLink();
      } else {
        makeDesktopFile();
      }
    }
  }

  private void makeShellLink ()
  {
    try {
      DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe",
        "-jar \"" + LauncherGlobals.USER_DIR + "\\KnightLauncher.jar\"",
        LauncherGlobals.USER_DIR,
        LauncherGlobals.USER_DIR + "\\KnightLauncher\\images\\icon-512.ico",
        "Start " + BuildConfig.getName(),
        BuildConfig.getName()
      );
    } catch (Exception e) {
      log.error("Failed to create shell link");
      log.error(e);
    }
  }

  private void makeDesktopFile ()
  {
    File desktopFile = new File(DesktopUtil.getPathToDesktop(), BuildConfig.getName() + ".desktop");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(desktopFile));
      out.write("[Desktop Entry]\n");
      out.write("Version=1.5\n");
      out.write("Name=" + BuildConfig.getName() + "\n");
      out.write("Comment=Open source game launcher for a certain game\n");
      out.write("Exec=java -jar KnightLauncher.jar\n");
      out.write("Icon=" + LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png\n");
      out.write("Path=" + LauncherGlobals.USER_DIR + "/\n");
      out.write("Type=Application\n");
      out.write("Categories=Game;\n");
      out.close();
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void setupHTTPSProtocol ()
  {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    System.setProperty("http.agent", "Mozilla/5.0");
    System.setProperty("https.agent", "Mozilla/5.0");
  }

  private void setupLogging ()
  {
    File logFile = new File(LauncherGlobals.USER_DIR + File.separator + "knightlauncher.log");
    File oldLogFile = new File(LauncherGlobals.USER_DIR + File.separator + "old-knightlauncher.log");

    // delete the current old log file if it exists.
    if (FileUtil.fileExists(oldLogFile.getAbsolutePath())) {
      FileUtil.deleteFile(oldLogFile.getAbsolutePath());
    }

    // rename the last log file to old.
    if (FileUtil.fileExists(logFile.getAbsolutePath())) {
      FileUtil.rename(logFile, oldLogFile);
    }

    if (DeployConfig.isDev()) {
      setupDebugLogging();
    }

    try {
      PrintStream printStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(logFile.toPath())), true);
      System.setOut(printStream);
      System.setErr(printStream);
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void setupDebugLogging ()
  {
    Logger rootLogger = LogManager
      .getLogManager()
      .getLogger("");
    rootLogger.setLevel(Level.ALL);
    for (Handler handler : rootLogger.getHandlers()) {
      handler.setLevel(Level.ALL);
    }
    log.debug("Enabled debug logging");
  }

  private void logVMInfo ()
  {
    log.info("------------ VM Info ------------");
    log.info("OS Name: " + System.getProperty("os.name"));
    log.info("OS Arch: " + System.getProperty("os.arch"));
    log.info("OS Vers: " + System.getProperty("os.version"));
    log.info("Java Home: " + System.getProperty("java.home"));
    log.info("Java Vers: " + System.getProperty("java.version"));
    log.info("User Name: " + System.getProperty("user.name"));
    log.info("User Home: " + System.getProperty("user.home"));
    log.info("Current Directory: " + System.getProperty("user.dir"));
    log.info("---------------------------------");
  }

  private void logGameVMInfo ()
  {
    log.info("--------- Game VM Info ----------");
    log.info("Directory: " + JavaUtil.getGameJVMDirPath());
    log.info("Executable: " + JavaUtil.getGameJVMExePath());
    log.info("Data: " + JavaUtil.getGameJVMData());
    log.info("Version: " + JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath()));
    log.info("Arch: " + JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()));
    log.info("---------------------------------");
  }

  private void logHostInfo ()
  {
    SystemInfo systemInfo = new SystemInfo();
    HardwareAbstractionLayer hardwareAbstractLayer = systemInfo.getHardware();

    log.info("----------- Host Info -----------");

    OperatingSystem os = systemInfo.getOperatingSystem();
    log.info("OS Version: " + os.getVersionInfo());

    CentralProcessor cpu = hardwareAbstractLayer.getProcessor();
    CentralProcessor.ProcessorIdentifier cpuProcId = cpu.getProcessorIdentifier();
    log.info("CPU: " + cpuProcId.getName());

    List<GraphicsCard> gpus = hardwareAbstractLayer.getGraphicsCards();
    for (int i = 0; i < gpus.size(); i++) {
      GraphicsCard gpu = gpus.get(i);
      log.info("GPU " + i + ": " + gpu.getVendor() + ", " + gpu.getName());
      log.info("GPU " + i + " Driver: " + gpu.getVersionInfo());
    }

    GlobalMemory memory = hardwareAbstractLayer.getMemory();
    log.info("Memory: " + FileUtils.byteCountToDisplaySize(memory.getTotal()));

    Baseboard baseboard = hardwareAbstractLayer.getComputerSystem().getBaseboard();
    log.info("Motherboard: " + baseboard.getManufacturer() + ", " + baseboard.getModel());
    log.info("Motherboard Version: " + baseboard.getVersion());

    List<Display> displays = hardwareAbstractLayer.getDisplays();
    for (int i = 0; i < displays.size(); i++) {
      Display display = displays.get(i);
      log.info("Display " + i + ": " + display.toString()
        .trim()
        .replace("\n", ", ")
        .replace("\r", "")
        .replaceAll(" +", " ")
      );
    }

    log.info("---------------------------------");
  }

  private boolean requiresJVMPatch ()
  {
    // First see if we're being forced to patch.
    if (this.args.length > 0 && this.args[0].equals("forceJVMPatch")) {
      return true;
    }

    // You need a 64-bit system to begin with.
    if (!SystemUtil.is64Bit()) return false;

    // Currently, Java VM patching is only supported on Windows systems and Linux installs through Steam.
    if (!SystemUtil.isWindows() && !(SystemUtil.isUnix() && Settings.gamePlatform.startsWith("Steam"))) return false;

    // Check if there's already a 64-bit Java VM in the game's directory or if it already has been installed by the launcher.
    String javaVMVersion = JavaUtil.getGameJVMData();

    if ((JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 && !javaVMVersion.contains("1.7")) || Settings.jvmPatched) {
      Settings.jvmPatched = true;
      _settingsManager.setValue("launcher.jvm_patched", "true");
      return false;
    }

    return true;
  }

  private boolean requiresUpdate ()
  {
    return this.args.length > 0 && this.args[0].equals("update");
  }

  private void loadOnlineAssets ()
  {
    new Thread(() -> {

      Status flamingoStatus = _flamingoManager.getStatus();
      if (flamingoStatus.version != null) _flamingoManager.setOnline(true);
      _launcherCtx.launcherGUI.eventHandler.updateServerList(_flamingoManager.fetchServerList());
      _launcherCtx.settingsGUI.eventHandler.updateAboutTab(flamingoStatus);
      _launcherCtx.settingsGUI.eventHandler.updateActiveBetaCodes();

    }).start();

    ThreadingUtil.executeWithDelay(this::checkVersion, 3000);
    ThreadingUtil.executeWithDelay(this::checkFlamingoStatus, 10000);
  }

  private void checkFlamingoStatus ()
  {
    if (!_flamingoManager.getOnline()) {
      _launcherCtx.launcherGUI.showWarning(_localeManager.getValue("error.flamingo_offline"));
    }
  }

  protected static int getOfficialApproxPlayerCount ()
  {
    int steamPlayers = SteamUtil.getCurrentPlayers("99900");
    if (steamPlayers == 0) {
      return 0;
    } else {
      return Math.round(steamPlayers * 1.6f);
    }
  }

  @SuppressWarnings("all")
  private void checkVersion ()
  {
    String rawResponseReleases = INetUtil.getWebpageContent(
      LauncherGlobals.GITHUB_API
        + "repos/"
        + LauncherGlobals.GITHUB_AUTHOR + "/"
        + LauncherGlobals.GITHUB_REPO + "/"
        + "releases/"
        + "latest"
    );

    if (rawResponseReleases != null) {
      JSONObject jsonReleases = new JSONObject(rawResponseReleases);
      String latestRelease = jsonReleases.getString("tag_name");
      String latestChangelog = jsonReleases.getString("body");

      _launcherCtx.launcherGUI.eventHandler.latestRelease = latestRelease;
      _launcherCtx.launcherGUI.eventHandler.latestChangelog = latestChangelog;

      String currentVersion = BuildConfig.getVersion();

      if (!latestRelease.equalsIgnoreCase(currentVersion)) {
        if (Settings.autoUpdate && !currentVersion.contains("SNAPSHOT")) {
          // Check if we're coming from a failed update, in that case do not autoupdate even if all other conditions matched.
          if (!(this.args.length > 0 && this.args[0].equals("updateFailed"))) {
            _launcherCtx.launcherGUI.eventHandler.updateLauncher(latestRelease);
          }
        }
        Settings.isOutdated = true;
        _launcherCtx.launcherGUI.updateButton.setVisible(true);
      }
    } else {
      log.error("Received no response from GitHub. Possible downtime?");
    }
  }

  public void checkGetdown ()
  {
    try {
      BufferedReader br = new BufferedReader(new FileReader(LauncherGlobals.USER_DIR + File.separator + "getdown.txt"));
      String firstLine = br.readLine();
      br.close();

      if (firstLine.contains("Customized")) {
        log.info("Detected a legacy modified Getdown file. Resetting");
        resetGetdown();
      }
    } catch (IOException e) {
      log.error(e);
      resetGetdown();
    }
  }

  private void resetGetdown ()
  {
    log.info("Resetting Getdown...");
    URLDownloadQueue downloadQueue = null;
    try {
      downloadQueue = new URLDownloadQueue(
        "Getdown Reset",
        new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/getdown.txt"),
        new File(LauncherGlobals.USER_DIR, "getdown.txt")
      );
    } catch (MalformedURLException e) {
      log.error(e);
    }

    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();
  }

  /**
   * Sometimes OS usernames that have cyrillic characters in them can make Java have a
   * bad time trying to read them for locating their TEMP path.
   * Let's give our friend a hand and store the temp files ourselves.
   */
  protected void checkTempDir ()
  {
    boolean containsCyrillic = System.getProperty("user.name").codePoints()
      .mapToObj(Character.UnicodeScript::of)
      .anyMatch(Character.UnicodeScript.CYRILLIC::equals);
    if (containsCyrillic) SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
  }

  public static class LauncherModule extends AbstractModule
  {
    @Override protected void configure ()
    {
      super.configure();
    }
  }
}
