package com.adamsresearch.jarview;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class JarView extends JPanel
{
  protected final String SETTINGS_FILENAME = "jarview.settings";
  protected final int FORWARD_SEARCH = 0;
  protected final int BACKWARD_SEARCH = 1;
  protected JarView thisPanel;
  protected ResourceBundle bundle;
  protected Properties settings;
  protected String jarFileName;
  protected JarFile jarFile;
  
  protected JMenuBar menuBar;
  protected JMenu fileMenu;
  protected JMenuItem fileOpenMenuItem;
  protected JMenuItem viewEntryMenuItem;
  protected JMenuItem extractEntryMenuItem;
  protected JMenuItem fileCloseMenuItem;
  protected JMenuItem fileExitMenuItem;
  protected JMenu helpMenu;
  protected JMenuItem aboutMenuItem;
  protected JToolBar toolBar;
  protected JButton openJarButton;
  protected JButton viewEntryButton;
  protected JButton extractEntryButton;
  protected JButton aboutButton;
  protected JTabbedPane tabbedPane;
  protected JPanel viewPanel;
  protected JPanel searchPanel;
  protected JLabel findLabel;
  protected JButton findNextButton;
  protected JButton findPreviousButton;
  protected JTextField findTextField;
  protected JTable fileTable;
  protected JScrollPane fileScrollPane;
  protected DefaultTableModel tableModel;
  protected Vector columnNames;
  protected JLabel rootDirectoryLabel;
  protected JTextField rootDirectoryTextField;
  protected JButton rootDirectoryBrowseButton;
  protected JCheckBox searchSubDirsCheckBox;
  protected JLabel searchForLabel;
  protected JTextField searchForTextField;
  protected JButton searchButton;
  protected JLabel searchResultsLabel;
  protected JTable searchResultsTable;
  protected DefaultTableModel resultsTableModel;
  protected JScrollPane searchResultsScrollPane;
  protected JProgressBar progressBar;
  protected int archiveFileVectorSize;
  protected int fileSeqNo;
  
  public static void main(String args[])
  {
    JFrame mainFrame = new JFrame();
    final JarView jv = new JarView();
    mainFrame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent we)
      {
        jv.storeAppLocationAndSize();
        System.exit(0);
      }
    });
    mainFrame.getContentPane().add(jv);
    jv.updateApplicationTitle(null, 0L, 0);
    mainFrame.setSize(800, 800);
    jv.setApplicationLocationAndSize();
    mainFrame.setVisible(true);
    if (args.length == 1)
    {
      jv.setJarFile(args[0]);
    }
  }
  
  public JarView()
  {
    thisPanel = this;
    bundle = ResourceBundle.getBundle("JarView", Locale.getDefault());
    settings = new Properties();
    try
    {
      settings.load(new FileInputStream(SETTINGS_FILENAME));
    }
    catch (IOException ioe)
    {
      // doesn't matter; we'll just create
      // a settings file later
    }
    createComponents();
    layOutComponents();
    createListeners();
  }
  
  protected void createComponents()
  {
    menuBar = new JMenuBar();
    fileMenu = new JMenu(bundle.getString("fileMenu"));
    fileOpenMenuItem = new JMenuItem(bundle.getString("fileOpenMenuItem"));
    viewEntryMenuItem = new JMenuItem(bundle.getString("viewEntryMenuItem"));
    viewEntryMenuItem.setEnabled(false);
    extractEntryMenuItem = new JMenuItem(bundle.getString("extractEntryMenuItem"));
    extractEntryMenuItem.setEnabled(false);
    fileCloseMenuItem = new JMenuItem(bundle.getString("fileCloseMenuItem"));
    fileCloseMenuItem.setEnabled(false);
    fileExitMenuItem = new JMenuItem(bundle.getString("fileExitMenuItem"));
    fileMenu.add(fileOpenMenuItem);
    fileMenu.add(viewEntryMenuItem);
    fileMenu.add(extractEntryMenuItem);
    fileMenu.add(fileCloseMenuItem);
    fileCloseMenuItem.setEnabled(false);
    fileMenu.addSeparator();
    fileMenu.add(fileExitMenuItem);
    helpMenu = new JMenu(bundle.getString("helpMenu"));
    aboutMenuItem = new JMenuItem(bundle.getString("aboutMenuItem"));
    helpMenu.add(aboutMenuItem);
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);
    toolBar = new JToolBar();
    toolBar.setFloatable(false);
    openJarButton = new JButton(new ImageIcon(JarView.class.getResource("images/Jar24.gif")));
    openJarButton.setToolTipText(bundle.getString("fileOpenMenuItem"));
    viewEntryButton = new JButton(new ImageIcon(JarView.class.getResource("images/Open24.gif")));
    viewEntryButton.setToolTipText(bundle.getString("viewEntryMenuItem"));
    viewEntryButton.setEnabled(false);
    extractEntryButton = new JButton(new ImageIcon(JarView.class.getResource("images/Extract24.gif")));
    extractEntryButton.setToolTipText(bundle.getString("extractEntryMenuItem"));
    extractEntryButton.setEnabled(false);
    aboutButton = new JButton(new ImageIcon(JarView.class.getResource("images/About24.gif")));
    aboutButton.setToolTipText(bundle.getString("aboutMenuItem"));
    toolBar.add(openJarButton);
    toolBar.add(viewEntryButton);
    toolBar.add(extractEntryButton);
    toolBar.addSeparator();
    toolBar.add(aboutButton);
    tabbedPane = new JTabbedPane();
    
    viewPanel = new JPanel();
    findLabel = new JLabel(bundle.getString("findLabel"));
    findTextField = new JTextField();
    findNextButton = new JButton(bundle.getString("findNextButton"));
    findPreviousButton = new JButton(bundle.getString("findPreviousButton"));
    findNextButton.setEnabled(false);
    findPreviousButton.setEnabled(false);
    columnNames = new Vector();
    columnNames.add(bundle.getString("fileNameColumn"));
    columnNames.add(bundle.getString("directoryNameColumn"));
    columnNames.add(bundle.getString("sizeColumn"));
    columnNames.add(bundle.getString("dateColumn"));
    tableModel = new DefaultTableModel();
    fileTable = new JTable(tableModel);
    fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fileScrollPane = new JScrollPane(fileTable,
                                     ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                     ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    searchPanel = new JPanel();
    rootDirectoryLabel = new JLabel(bundle.getString("rootDirectoryLabel"));
    rootDirectoryTextField = new JTextField();
    rootDirectoryTextField.setText(settings.getProperty("searchRootDirectory"));
    rootDirectoryBrowseButton = new JButton(bundle.getString("rootDirectoryBrowseButton"));
    searchSubDirsCheckBox = new JCheckBox(bundle.getString("searchSubDirsCheckBox"));
    searchForLabel = new JLabel(bundle.getString("searchForLabel"));
    searchForTextField = new JTextField();
    searchButton = new JButton(bundle.getString("searchButton"));
    searchResultsLabel = new JLabel(bundle.getString("searchResultsLabel"));
    searchResultsTable = new JTable();
    searchResultsScrollPane = new JScrollPane(searchResultsTable,
                                              ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    progressBar = new JProgressBar();
    progressBar.setIndeterminate(false);
    progressBar.setStringPainted(true);
    progressBar.setString(null);
  }
  
  protected void layOutComponents()
  {
    GridBagConstraints gbc = new GridBagConstraints();
    GridBagLayout gbl = new GridBagLayout();
    thisPanel.setLayout(gbl);
    
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 7; gbc.gridheight = 1;
    gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
    thisPanel.add(menuBar, gbc);
    
    gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 7; gbc.gridheight = 1;
    gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
    thisPanel.add(toolBar, gbc);
    
    // the view panel, used for viewing the contents of a .jar, etc.
    GridBagConstraints vpGbc = new GridBagConstraints();
    GridBagLayout vpGbl = new GridBagLayout();
    viewPanel.setLayout(vpGbl);
    
    vpGbc.gridx = 0; vpGbc.gridy = 0; vpGbc.gridwidth = 1; vpGbc.gridheight = 1;
    vpGbc.weightx = 0.0; vpGbc.weighty = 0.0; vpGbc.fill = GridBagConstraints.NONE;
    vpGbc.insets = new Insets(10, 5, 10, 5);
    viewPanel.add(findLabel, vpGbc);
    
    vpGbc.gridx = 1; vpGbc.gridy = 0; vpGbc.gridwidth = 2; vpGbc.gridheight = 1;
    vpGbc.weightx = 1.0; vpGbc.weighty = 0.0; vpGbc.fill = GridBagConstraints.HORIZONTAL;
    vpGbc.insets = new Insets(5, 5, 5, 5);
    viewPanel.add(findTextField, vpGbc);
    
    vpGbc.gridx = 3; vpGbc.gridy = 0; vpGbc.gridwidth = 1; vpGbc.gridheight = 1;
    vpGbc.weightx = 0.0; vpGbc.weighty = 0.0; vpGbc.fill = GridBagConstraints.NONE;
    viewPanel.add(findNextButton, vpGbc);
    
    vpGbc.gridx = 4; vpGbc.gridy = 0; vpGbc.gridwidth = 1; vpGbc.gridheight = 1;
    vpGbc.weightx = 0.0; vpGbc.weighty = 0.0; vpGbc.fill = GridBagConstraints.NONE;
    viewPanel.add(findPreviousButton, vpGbc);
    
    vpGbc.gridx = 0; vpGbc.gridy = 1; vpGbc.gridwidth = 7; vpGbc.gridheight = 2;
    vpGbc.weightx = 1.0; vpGbc.weighty = 1.0; vpGbc.fill = GridBagConstraints.BOTH;
    viewPanel.add(fileScrollPane, vpGbc);
    
    // the search panel, used for searching a directory structure of .jar, etc.
    // files for a specific entry:
    GridBagConstraints spGbc = new GridBagConstraints();
    GridBagLayout spGbl = new GridBagLayout();
    searchPanel.setLayout(spGbl);
    
    spGbc.gridx = 0; spGbc.gridy = 0; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    spGbc.insets = new Insets(5, 5, 1, 5); spGbc.anchor = GridBagConstraints.WEST;
    searchPanel.add(rootDirectoryLabel, spGbc);

    spGbc.gridx = 1; spGbc.gridy = 0; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 1.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.HORIZONTAL;
    searchPanel.add(rootDirectoryTextField, spGbc);

    spGbc.gridx = 2; spGbc.gridy = 0; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    searchPanel.add(rootDirectoryBrowseButton, spGbc);

    spGbc.gridx = 1; spGbc.gridy = 1; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    spGbc.insets = new Insets(1, 5, 1, 5);
    searchPanel.add(searchSubDirsCheckBox, spGbc);

    spGbc.gridx = 0; spGbc.gridy = 2; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    spGbc.insets = new Insets(5, 5, 1, 5); spGbc.anchor = GridBagConstraints.WEST;
    searchPanel.add(searchForLabel, spGbc);

    spGbc.gridx = 1; spGbc.gridy = 2; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 1.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.HORIZONTAL;
    searchPanel.add(searchForTextField, spGbc);

    spGbc.gridx = 2; spGbc.gridy = 2; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    searchPanel.add(searchButton, spGbc);

    spGbc.gridx = 0; spGbc.gridy = 3; spGbc.gridwidth = 3; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    searchPanel.add(searchResultsLabel, spGbc);

    spGbc.gridx = 0; spGbc.gridy = 4; spGbc.gridwidth = 7; spGbc.gridheight = 2;
    spGbc.weightx = 1.0; spGbc.weighty = 1.0; spGbc.fill = GridBagConstraints.BOTH;
    searchPanel.add(searchResultsScrollPane, spGbc);

    spGbc.gridx = 0; spGbc.gridy = 11; spGbc.gridwidth = 1; spGbc.gridheight = 1;
    spGbc.weightx = 0.0; spGbc.weighty = 0.0; spGbc.fill = GridBagConstraints.NONE;
    searchPanel.add(progressBar, spGbc);

    // add the view and search panels to the tabbed pane; then add
    // it to the main panel:
    tabbedPane.addTab(bundle.getString("viewPanel"), viewPanel);
    tabbedPane.addTab(bundle.getString("searchPanel"), searchPanel);
    
    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 7; gbc.gridheight = 4;
    gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5, 5, 5, 5);
    thisPanel.add(tabbedPane, gbc);
  }
  
  protected void createListeners()
  {
    fileOpenMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.selectJarFile();
      }
    });
    openJarButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.selectJarFile();
      }
    });
    viewEntryMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.viewSelectedEntry();
      }
    });
    viewEntryButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.viewSelectedEntry();
      }
    });
    extractEntryMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.extractSelectedEntry();
      }
    });
    extractEntryButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.extractSelectedEntry();
      }
    });
    fileCloseMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.closeJarFile();
      }
    });
    fileExitMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.storeAppLocationAndSize();
        System.exit(0);
      }
    });
    aboutMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.showAboutBox();
      }
    });
    aboutButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.showAboutBox();
      }
    });
    fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent lse)
      {
        boolean entrySelected = (fileTable.getSelectedRow() > -1);
        viewEntryMenuItem.setEnabled(entrySelected);
        viewEntryButton.setEnabled(entrySelected);
        extractEntryMenuItem.setEnabled(entrySelected);
        extractEntryButton.setEnabled(entrySelected);
      }
    });
    findTextField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void changedUpdate(DocumentEvent de)
      {
      }
      public void insertUpdate(DocumentEvent de)
      {
        thisPanel.searchFileNamesForString(findTextField.getText(), false, thisPanel.FORWARD_SEARCH);
      }
      public void removeUpdate(DocumentEvent de)
      {
        thisPanel.searchFileNamesForString(findTextField.getText(), false, thisPanel.FORWARD_SEARCH);
      }
    });
    findNextButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.searchFileNamesForString(findTextField.getText(), true, thisPanel.FORWARD_SEARCH);
      }
    });
    findPreviousButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.searchFileNamesForString(findTextField.getText(), true, thisPanel.BACKWARD_SEARCH);
      }
    });
    rootDirectoryBrowseButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        JFileChooser chooser = new JFileChooser();
        javax.swing.filechooser.FileFilter dirFilter = new javax.swing.filechooser.FileFilter()
        {
          public boolean accept(File file)
          {
           return file.isDirectory();
          }
          public String getDescription()
          {
            return "Directories";
          }
        };
        chooser.setFileFilter(dirFilter);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String searchRootDir = rootDirectoryTextField.getText();
        if (searchRootDir != null)
        {
          chooser.setCurrentDirectory(new File(searchRootDir));
        }
        int returnValue = chooser.showOpenDialog(thisPanel);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
          String rootDirectoryAbsolutePath = chooser.getSelectedFile().getAbsolutePath();
          rootDirectoryTextField.setText(rootDirectoryAbsolutePath);
          settings.setProperty("searchRootDirectory", rootDirectoryAbsolutePath);
          try
          {
            settings.store(new FileOutputStream(SETTINGS_FILENAME), "JarView application settings file");
          }
          catch (IOException ioe)
          {
            System.err.println("IOException storing application settings: '" + ioe.getMessage() + "'");
          }
        }
      }
    });
    searchForTextField.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent ke)
      {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER)
        {
          // need to set Cursor on the JTextField to see the change:
          searchForTextField.setCursor(new Cursor(Cursor.WAIT_CURSOR));
          progressBar.setIndeterminate(true);
          Runnable searchThread = new Runnable()
          {
            public void run()
            {
              thisPanel.performArchiveSearch();
            }
          };
          new Thread(searchThread).start();
          searchForTextField.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    searchButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        thisPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
          progressBar.setIndeterminate(true);
          Runnable searchThread = new Runnable()
          {
            public void run()
            {
              thisPanel.performArchiveSearch();
            }
          };
          new Thread(searchThread).start();
        thisPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });
    searchResultsTable.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent me)
      {
        final javax.swing.JPopupMenu openMenu = new javax.swing.JPopupMenu("huh?");
        JMenuItem contextOpenMenuItem = new JMenuItem("Open");
        contextOpenMenuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae)
          {
            boolean entrySelected = (searchResultsTable.getSelectedRow() > -1);
            if (entrySelected)
            {
              String jarFileAbsolutePath = (String)tableModel.getValueAt(searchResultsTable.getSelectedRow(), 0);
              tabbedPane.setSelectedIndex(0);
              thisPanel.setJarFile(jarFileAbsolutePath);
              openMenu.setVisible(false);
            }
          }
        });
        openMenu.add(contextOpenMenuItem);
        openMenu.setSize(200,200);
        openMenu.setLocation(searchResultsTable.getLocationOnScreen().x + me.getX(), 
                             searchResultsTable.getLocationOnScreen().y + me.getY());
        openMenu.setVisible(true);
      }
    });
  }
  
  public void storeAppLocationAndSize()
  {
    // store off to settings
    try
    {
      int xLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().x;
      int yLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().y;
      int xWidth = thisPanel.getTopLevelAncestor().getSize().width;
      int xHeight = thisPanel.getTopLevelAncestor().getSize().height;
      String appLocationAndSize = "" + xLoc + "," + yLoc + "," + xWidth + "," + xHeight;
      settings.setProperty("appLocationAndSize", appLocationAndSize);
      try
      {
        settings.store(new FileOutputStream(SETTINGS_FILENAME), "JarView application settings file");
      }
      catch (IOException ioe)
      {
        System.err.println("IOException storing application settings: '" + ioe.getMessage() + "'");
      }
    }
    catch (IllegalComponentStateException icse)
    {
      // no-op; this is a side-effect of the order in which
      // the listeners are registered and the application is
      // made visible on the screen and is harmless
    }
  }
  
  public void setApplicationLocationAndSize()
  {
    String appLocationAndSize = settings.getProperty("appLocationAndSize");
    if (appLocationAndSize != null)
    {
      try
      {
        StringTokenizer st = new StringTokenizer(appLocationAndSize, ",");
        int xLoc = Integer.parseInt(st.nextToken());
        int yLoc = Integer.parseInt(st.nextToken());
        int xWidth = Integer.parseInt(st.nextToken());
        int xHeight = Integer.parseInt(st.nextToken());
        thisPanel.getTopLevelAncestor().setLocation(new Point(xLoc, yLoc));
        thisPanel.getTopLevelAncestor().setSize(new Dimension(xWidth, xHeight));
      }
      catch (Exception exc)
      {
        System.err.println("Exception trying to parse application location and size string: '" +
                           appLocationAndSize + "'");
      }
    }
  }
  
  protected void selectJarFile()
  {
    tabbedPane.setSelectedIndex(0);
    JFileChooser chooser = new JFileChooser();
    javax.swing.filechooser.FileFilter jarFileFilter = new javax.swing.filechooser.FileFilter()
    {
      public boolean accept(File file)
      {
        return (file.isDirectory() ||
                (file.isFile() &&
                 (file.getName().endsWith(".jar") ||
                  file.getName().endsWith(".war") ||
                  file.getName().endsWith(".ear") ||
                  file.getName().endsWith(".zip") ||
                  file.getName().endsWith(".tar"))));
      }
      public String getDescription()
      {
        return "Archive Files (.jar, .war, .ear, .zip, .tar)";
      }
    };
    chooser.setFileFilter(jarFileFilter);
    String chooserDir = settings.getProperty("fileChooserDirectory");
    if (chooserDir != null)
    {
      chooser.setCurrentDirectory(new File(chooserDir));
    }
    int returnValue = chooser.showOpenDialog(thisPanel);
    if (returnValue == JFileChooser.APPROVE_OPTION)
    {
      thisPanel.setJarFile(chooser.getSelectedFile().getAbsolutePath());
    }
  }
  
  protected void setJarFile(String jarFileAbsolutePath)
  {
    try
    {
      String directory = jarFileAbsolutePath.substring(0, jarFileAbsolutePath.lastIndexOf(File.separator));
      settings.setProperty("fileChooserDirectory", directory);
      try
      {
        settings.store(new FileOutputStream(SETTINGS_FILENAME), "JarView application settings file");
      }
      catch (IOException ioe)
      {
        System.err.println("IOException storing application settings: '" + ioe.getMessage() + "'");
      }
      // since it is possible to arrive here from the command line
      // and not just from our JFileChooser with its FileFilter, we
      // have to check the File to see that is has the correct extension:
      File newJarFile = new File(jarFileAbsolutePath);
      boolean qualifyingFile = (newJarFile.isDirectory() ||
                                (newJarFile.isFile() &&
                                 (newJarFile.getName().endsWith(".jar") ||
                                  newJarFile.getName().endsWith(".war") ||
                                  newJarFile.getName().endsWith(".ear") ||
                                  newJarFile.getName().endsWith(".zip") ||
                                  newJarFile.getName().endsWith(".tar"))));
      if (!qualifyingFile)
      {
        return;
      }
      jarFile = new JarFile(newJarFile);
      jarFileName = newJarFile.getAbsolutePath();
      fileCloseMenuItem.setEnabled(true);
      findNextButton.setEnabled(true);
      findPreviousButton.setEnabled(true);
    
      Enumeration fileEntries = jarFile.entries();
      Vector rowData = new Vector();
      long archiveFileSize = 0;
      int numberOfEntries = 0;
      while (fileEntries.hasMoreElements())
      {
        Vector nextRow = new Vector();
        JarEntry je = (JarEntry)fileEntries.nextElement();
        if (!je.isDirectory())
        {
          String fullName = je.getName();
          String directoryName = "";
          String fileName = "";
          if (fullName.lastIndexOf("/") > -1)
          {
            directoryName = fullName.substring(0, fullName.lastIndexOf("/"));
            fileName = fullName.substring(directoryName.length()+1, fullName.length());
          }
          else
          {
            fileName = fullName;
          }
          Long fileSize = new Long(je.getSize());
          Date fileDate = new Date(je.getTime());
          archiveFileSize += fileSize.longValue();
          numberOfEntries++;
          nextRow.add(fileName);
          nextRow.add(directoryName);
          nextRow.add(fileSize);
          nextRow.add(fileDate);
          rowData.add(nextRow);
        }
      }
      tableModel = new DefaultTableModel(rowData, columnNames);
      fileTable.setModel(tableModel);
      fileScrollPane.setViewportView(fileTable);
      thisPanel.updateApplicationTitle(jarFileName, archiveFileSize, numberOfEntries);
    }
    catch (IOException ioe)
    {
      System.err.println(ioe.getMessage());
      Object args[] = new Object[] {jarFileAbsolutePath, ioe.getMessage()};
      String msg = MessageFormat.format(bundle.getString("errorOpeningJarFileMessage"), args);
      JOptionPane.showMessageDialog(null,
                                    msg,
                                    bundle.getString("errorOpeningJarFileTitle"),
                                    JOptionPane.ERROR_MESSAGE);
    }
  }
  
  protected void closeJarFile()
  {
    jarFileName = null;
    try
    {
      jarFile.close();
    }
    catch (IOException ioe)
    {
      // no-op; will just set reference to null anyway
    }
    jarFile = null;
    updateApplicationTitle(jarFileName, 0L, 0);
    fileCloseMenuItem.setEnabled(false);
    tableModel = new DefaultTableModel(0, 0);
    fileTable.setModel(tableModel);
    fileScrollPane.setViewportView(fileTable);
    findTextField.setBackground(Color.WHITE);
    findNextButton.setEnabled(false);
    findPreviousButton.setEnabled(false);
  }

  protected void updateApplicationTitle(String newFilename, long archiveFileSize, int numberOfEntries)
  {
    String newApplicationTitle = bundle.getString("applicationTitle");
    if (newFilename != null)
    {
      // more info if there's a file open:
      Object args[] = new Object[] {newFilename, new Integer(numberOfEntries), new Long(archiveFileSize)};
      newApplicationTitle = MessageFormat.format(bundle.getString("applicationTitleWithFile"), args);
    }
    ((JFrame)thisPanel.getTopLevelAncestor()).setTitle(newApplicationTitle);
  }
  
  protected void performArchiveSearch()
  {
    long startTime = System.currentTimeMillis();
    Vector archiveFiles = new Vector();
    Vector directories = new Vector();
    File rootDir = new File(rootDirectoryTextField.getText());
    File dirList[] = rootDir.listFiles(new java.io.FileFilter()
    {
      public boolean accept(File pathname)
      {
        return  (pathname.isDirectory() ||
                 (pathname.isFile() &&
                  (pathname.getName().endsWith(".jar") ||
                   pathname.getName().endsWith(".war") ||
                   pathname.getName().endsWith(".ear") ||
                   pathname.getName().endsWith(".zip") ||
                   pathname.getName().endsWith(".tar"))));
      }
    });
    for (int i=0; i<dirList.length; i++)
    {
      if (dirList[i].isDirectory())
      {
        if (searchSubDirsCheckBox.isSelected())
        {
          // save these and drill down later
          directories.add(dirList[i]);
        }
      }
      else
      {
        archiveFiles.add(dirList[i]);
      }
    }
    if (searchSubDirsCheckBox.isSelected())
    {
      // recursively drill down:
      thisPanel.addRemainingArchives(archiveFiles, directories);
    }
   // now that we have the full set of files to inspect,
    // get a JarFile object for each one and search the
    // entries for the supplied fragment:
    Vector rowData = new Vector();
    // update the progress bar to reflect the number of archives
    // that will be searched:
    archiveFileVectorSize = archiveFiles.size();
    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        progressBar.setMinimum(0);
        progressBar.setMaximum(archiveFileVectorSize - 1);
        progressBar.setIndeterminate(false);
      }
    });
    Vector problemFiles = new Vector();
    for (fileSeqNo=0; fileSeqNo<archiveFileVectorSize; fileSeqNo++)
    {
      SwingUtilities.invokeLater(new Runnable() {
        public void run()
        {
          progressBar.setValue(fileSeqNo);
        }
      });
      File archive = null;
      try
      {
        archive = (File)archiveFiles.elementAt(fileSeqNo);
        JarFile nextJarFile = new JarFile(archive);
        Enumeration archiveFileEntries = nextJarFile.entries();
        while (archiveFileEntries.hasMoreElements())
        {
          JarEntry je = (JarEntry)archiveFileEntries.nextElement();
          if (!je.isDirectory())
          {
            String fullName = je.getName();
            String directoryName = "";
            String fileName = "";
            if (fullName.lastIndexOf("/") > -1)
            {
              directoryName = fullName.substring(0, fullName.lastIndexOf("/"));
              fileName = fullName.substring(directoryName.length()+1, fullName.length());
            }
            else
            {
              fileName = fullName;
            }
            if (fileName.indexOf(searchForTextField.getText()) > -1)
            {
              Vector nextRow = new Vector();
              nextRow.add(archive.getAbsolutePath());
              nextRow.add(fileName);
              rowData.add(nextRow);
            }
          }
        }
      }
      catch (IOException ioe)
      {
        problemFiles.addElement(archive.getAbsolutePath());
      }
    }
    
    if (problemFiles.size() > 0)
    {
      String fileList = "";
      for (int i=0; i<problemFiles.size(); i++)
      {
        fileList += problemFiles.elementAt(i) + "\n";
      }
      Object args[] = new Object[] {fileList};
      String msg = MessageFormat.format(bundle.getString("errorOpeningArchiveMessage"), args);
      JOptionPane.showMessageDialog(null, 
                                    msg,
                                    bundle.getString("errorOpeningArchiveTitle"),
                                    JOptionPane.ERROR_MESSAGE);
    }
    
    // now update the results table:
    Vector resultsColumnNames = new Vector();
    resultsColumnNames.add(bundle.getString("resultsFileNameColumn"));
    resultsColumnNames.add(bundle.getString("resultsEntryNameColumn"));
    resultsTableModel = new DefaultTableModel(rowData, resultsColumnNames);
    searchResultsTable.setModel(resultsTableModel);
    searchResultsScrollPane.setViewportView(searchResultsTable);
    long stopTime = System.currentTimeMillis();
    long elapsedTime = (stopTime - startTime)/1000;
    Object args[] = new Object[] {new Integer(archiveFiles.size()), new Long(elapsedTime)};
    String stats = MessageFormat.format(bundle.getString("searchResultsWithStatisticsLabel"), args);
    searchResultsLabel.setText(stats);

    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        progressBar.setIndeterminate(false);
      }
    });
  }

  protected void addRemainingArchives(Vector archives, Vector directories)
  {
    if (directories.size() == 0)
    {
      return;
    }
    else
    {
      for (int i=0; i<directories.size(); i++)
      {
        Vector newDirectories = new Vector();
        File rootDir = (File)directories.elementAt(i);
        File dirList[] = rootDir.listFiles(new java.io.FileFilter()
        {
          public boolean accept(File pathname)
          {
            return  (pathname.isDirectory() ||
                     (pathname.isFile() &&
                      (pathname.getName().endsWith(".jar") ||
                       pathname.getName().endsWith(".war") ||
                       pathname.getName().endsWith(".ear") ||
                       pathname.getName().endsWith(".zip") ||
                       pathname.getName().endsWith(".tar"))));
          }
        });
        for (int j=0; j<dirList.length; j++)
        {
          if (dirList[j].isDirectory())
          {
            // save these and drill down later
            newDirectories.add(dirList[j]);
          }
          else
          {
            archives.add(dirList[j]);
          }
        }
        if (newDirectories.size() > 0)
        {
          addRemainingArchives(archives, newDirectories);
        }
      }
    }
  }

  public void viewSelectedEntry()
  {
    JDialog viewDialog = new JDialog();
    JEditorPane editorPane = new JEditorPane();
    editorPane.setEditable(false);
    JScrollPane viewScroll = new JScrollPane(editorPane,
                                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    viewDialog.getContentPane().add(viewScroll);
    String archiveEntryName = "";
    // prepend path if it's not at root level:
    String entryDir = (String)tableModel.getValueAt(fileTable.getSelectedRow(), 1);
    if (entryDir != null && entryDir.trim().length() > 0)
    {
      archiveEntryName += (String)tableModel.getValueAt(fileTable.getSelectedRow(), 1) + "/";
    }
    archiveEntryName += (String)tableModel.getValueAt(fileTable.getSelectedRow(), 0);
    if (archiveEntryName.endsWith("html"))
    {
      editorPane.setContentType("text/html");
    }
    else
    {
      editorPane.setContentType("text/plain");
    }
    try
    {
      editorPane.read(jarFile.getInputStream(jarFile.getEntry(archiveEntryName)), null);
      viewDialog.setTitle(archiveEntryName);
      viewDialog.setSize(400, 400);
      int xLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().x;
      int yLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().y;
      int xWidth = thisPanel.getTopLevelAncestor().getSize().width;
      int xHeight = thisPanel.getTopLevelAncestor().getSize().height;
      viewDialog.setLocation(xLoc + viewDialog.getSize().width/2,
                             yLoc + viewDialog.getSize().height/2);
      viewDialog.setVisible(true);
    }
    catch (IOException ioe)
    {
      System.err.println("IOException viewing entry: '" + ioe.getMessage() + "'");
    }
  }
  
  public void extractSelectedEntry()
  {
    // pop up a quick dialog for the location to which to extract the entry:
    ExtractSelectedEntryDialog extractDialog = new ExtractSelectedEntryDialog(bundle, settings);
    extractDialog.setVisible(true);
  }
  
  protected void showAboutBox()
  {
    JOptionPane.showMessageDialog(thisPanel,
                                  bundle.getString("aboutMessage"),
                                  bundle.getString("aboutTitle"),
                                  JOptionPane.INFORMATION_MESSAGE);
  }
  
  protected void searchFileNamesForString(String fragment, boolean incrementRow, int searchOrder)
  {
    if (fragment == null ||
        fragment.trim().length() == 0)
    {
      findTextField.setBackground(Color.WHITE);
      return;
    }
    int rowCount = tableModel.getRowCount();
    boolean fragmentNotFound = true;
    boolean endOfList = false;
    int row = fileTable.getSelectedRow();
    if (row == -1)
    {
      row = 0;
    }
    // 'incrementRow' is false for text-field change events, because you
    // may still find a match in the same row.  set it to 'true' when you're
    // specifically wanting to find the next occurrence.
    if (incrementRow)
    {
      if (searchOrder == FORWARD_SEARCH)
      {
        row++;
        if (row == rowCount)
        {
          endOfList = true;
        }
      }
      else if (searchOrder == BACKWARD_SEARCH)
      {
        row--;
        if (row < 0)
        {
          endOfList = true;
        }
      }
    }
    
    while (fragmentNotFound && !endOfList)
    {
      String filename = (String)tableModel.getValueAt(row, 0);
      if (filename.indexOf(fragment) > -1)
      {
        // a match:
        fragmentNotFound = false;
        fileTable.setRowSelectionInterval(row, row);
        // only JTree has a command to scroll the selected item to visible
        // when in a JScrollPane, unfortunately.  and the JScrollBar 'value'
        // is *not* the row; must scale between number of rows and 'size' of
        // the vertical scrollbar to scroll the selected row to visible:
        int scrollBarValue = row*(fileScrollPane.getVerticalScrollBar().getMaximum() -
                                  fileScrollPane.getVerticalScrollBar().getMinimum())/rowCount;
        fileScrollPane.getVerticalScrollBar().setValue(scrollBarValue);
      }
      else
      {
        if (searchOrder == FORWARD_SEARCH)
        {
          row++;
          if (row == rowCount)
          {
            endOfList = true;
          }
        }
        else if (searchOrder == BACKWARD_SEARCH)
        {
          row--;
          if (row < 0)
          {
            endOfList = true;
          }
        }
      }
    }
    if (fragmentNotFound)
    {
      findTextField.setBackground(Color.RED);
    }
    else
    {
      findTextField.setBackground(Color.WHITE);
    }
  }
  
  public class ExtractSelectedEntryDialog extends JDialog
  {
    protected ResourceBundle bundle;
    protected Properties settings;
    protected JLabel extractToLabel;
    protected JTextField extractToField;
    protected JButton browseButton;
    protected JCheckBox expandPathCheckBox;
    protected JButton extractButton;
    protected GridBagLayout xseGbl;
    protected GridBagConstraints xseGbc;
    protected JDialog extractDialog;
    protected Container contentPane;
    
    public ExtractSelectedEntryDialog(ResourceBundle parentBundle, Properties parentSettings)
    {
      bundle = parentBundle;
      settings = parentSettings;
      extractDialog = this;
      
      extractToLabel = new JLabel(bundle.getString("extractToLabel"));
      extractToField = new JTextField();
      extractToField.setText(settings.getProperty("extractEntryDirectory"));
      browseButton = new JButton(bundle.getString("extractToBrowseButton"));
      expandPathCheckBox = new JCheckBox(bundle.getString("expandPathCheckBox"));
      expandPathCheckBox.setSelected((new Boolean(settings.getProperty("expandExtractionDirectory"))).booleanValue());
      extractButton = new JButton(bundle.getString("extractButton"));
      extractDialog.setTitle(bundle.getString("extractTitle"));
      extractDialog.setModal(true);
      contentPane = extractDialog.getContentPane();

      layOutComponents();
      createListeners();
      extractDialog.setSize(525, 125);
      extractDialog.setResizable(false);
      extractDialog.setModal(true);
      int xLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().x;
      int yLoc = thisPanel.getTopLevelAncestor().getLocationOnScreen().y;
      int width = thisPanel.getTopLevelAncestor().getSize().width;
      int height = thisPanel.getTopLevelAncestor().getSize().height;
      extractDialog.setLocation(xLoc + width/2 - extractDialog.getSize().width/2,
                                yLoc + height/2 - extractDialog.getSize().height/2);
    }
  
    protected void createListeners()
    {
      extractToField.getDocument().addDocumentListener(new DocumentListener()
      {
        public void changedUpdate(DocumentEvent de)
        {
          extractButton.setEnabled(extractToField.getText().trim().length() > 0);
        }
        public void insertUpdate(DocumentEvent de)
        {
          extractButton.setEnabled(extractToField.getText().trim().length() > 0);
        }
        public void removeUpdate(DocumentEvent de)
        {
          extractButton.setEnabled(extractToField.getText().trim().length() > 0);
        }
      });
      browseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          JFileChooser chooser = new JFileChooser();
          javax.swing.filechooser.FileFilter dirFilter = new javax.swing.filechooser.FileFilter()
          {
            public boolean accept(File file)
            {
             return file.isDirectory();
            }
            public String getDescription()
            {
              return "Directories";
            }
          };
          chooser.setFileFilter(dirFilter);
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          String extractDir = extractToField.getText();
          if (extractDir != null)
          {
            chooser.setCurrentDirectory(new File(extractDir));
          }
          int returnValue = chooser.showOpenDialog(thisPanel);
          if (returnValue == JFileChooser.APPROVE_OPTION)
          {
            String extractDirAbsolutePath = chooser.getSelectedFile().getAbsolutePath();
            extractToField.setText(extractDirAbsolutePath);
            settings.setProperty("extractEntryDirectory", extractDirAbsolutePath);
            try
            {
              settings.store(new FileOutputStream(SETTINGS_FILENAME), "JarView application settings file");
            }
            catch (IOException ioe)
            {
              System.err.println("IOException storing application settings: '" + ioe.getMessage() + "'");
            }
          }
        }
      });
      extractButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          boolean extractSucceeded = false;
          String archiveEntryPath = (String)tableModel.getValueAt(fileTable.getSelectedRow(), 1);
          String archiveEntryFilename = (String)tableModel.getValueAt(fileTable.getSelectedRow(), 0);
          String archiveEntry = "";
          if (archiveEntryPath.trim().length() > 0)
          {
            archiveEntry = archiveEntryPath + "/" + archiveEntryFilename;
          }
          else
          {
            archiveEntry = archiveEntryFilename;
          }
          String outputFilename = null;
          try
          {
            File topLevelDir = new File(extractToField.getText());
            if (!topLevelDir.exists())
            {
              boolean createdTopLevelDir = topLevelDir.mkdir();
              if (!createdTopLevelDir)
              {
                Object args[] = new Object[] {topLevelDir.getAbsolutePath()};
                String errMsg = MessageFormat.format(bundle.getString("dirCreateFailure"), args);
                throw new IOException(errMsg);
              }
            }
            // if the "expand" box is checked, create the path directories if necessary
            if (expandPathCheckBox.isSelected())
            {
              File expandedDir = new File(topLevelDir + File.separator + archiveEntryPath.replace('/', File.separatorChar));
              if (!expandedDir.exists())
              {
                boolean createdExpandedDir = expandedDir.mkdirs();
                if (!createdExpandedDir)
                {
                  Object args[] = new Object[] {expandedDir.getAbsolutePath()};
                  String errMsg = MessageFormat.format(bundle.getString("dirCreateFailure"), args);
                  throw new IOException(errMsg);
                }
              }
              outputFilename = expandedDir.getAbsolutePath() + File.separator + archiveEntryFilename;
            }
            else
            {
              outputFilename = topLevelDir + File.separator + archiveEntryFilename;
            }
            ZipEntry ze = jarFile.getEntry(archiveEntry);
            if (ze != null)
            {
              InputStream is = jarFile.getInputStream(ze);
              if (is != null)
              {
                // branch if entry is compressed:
//                if (ze.getMethod() == ZipEntry.STORED)
//                {
                  InputStreamReader isr = new InputStreamReader(is);
                  java.io.FileWriter fw = new java.io.FileWriter(new File(outputFilename));
                  int c;
                  while ((c = isr.read()) != -1)
                  {
                    fw.write(c);
                  }
                  isr.close();
                  fw.close();
                  extractSucceeded = true;
/*                }
                else if (ze.getMethod() == ZipEntry.DEFLATED)
                {
                  int compressedDataLength = (new Long(ze.getCompressedSize())).intValue();
                  int uncompressedDataLength = (new Long(ze.getSize())).intValue();
                  int resultLength = 0;
                  byte[] compressedBytes = new byte[compressedDataLength];
                  byte[] uncompressedBytes = new byte[uncompressedDataLength];
                  java.util.zip.Inflater decompresser = new java.util.zip.Inflater();
                  decompresser.setInput(compressedBytes, 0, compressedDataLength);
                  try
                  {
                    resultLength = decompresser.inflate(uncompressedBytes);
                  }
                  catch (DataFormatException dfe)
                  {
                    Object[] args = new Object[] {ze.getName(), dfe.getMessage()};
                    String errMsg = MessageFormat.format(bundle.getString("dataFormatException"), args);
                    throw new JarEntryExtractionException(errMsg);
                  }
                  decompresser.end();
                  java.io.FileWriter fw = new java.io.FileWriter(new File(outputFilename));
                  for (int i=0; i<resultLength; i++)
                  {
                    fw.write(uncompressedBytes[i]);
                  }
                  fw.close();
/*
                  ZipInputStream zis = new ZipInputStream(is);
                  FileOutputStream fos = new FileOutputStream(outputFilename);
                  int count;
                  byte[] byteArray = new byte[1024];
count = zis.read(byteArray, 0, 1024);
System.out.println("  attempted to read 1024 bytes from ZipInputStream; got " + count);
                  while ((count = zis.read(byteArray, 0, 1024)) != -1)
                  {
System.out.println("read " + count + " bytes from ZipInputStream");
                    fos.write(byteArray, 0, count);
                  }
                  zis.close();
                  fos.close(); 
                  if (resultLength == uncompressedDataLength)
                  {
                    extractSucceeded = true;
                  }
                  else
                  {
                    Object args[] = new Object[] {new Integer(uncompressedDataLength), new Integer(resultLength)};
                    String errMsg = MessageFormat.format(bundle.getString("wrongNumberOfBytesReadMessage"), args);
                    throw new JarEntryExtractionException(errMsg);
                  }
                }
                else
                {
System.out.println("\nZipEntry method unknown; method result = " + ze.getMethod());
                } */
              }
              else
              {
                Object args[] = new Object[] {ze.getName()};
                String errMsg = MessageFormat.format(bundle.getString("nullInputStreamMessage"), args);
                throw new JarEntryExtractionException(errMsg);
              }
            }
            else
            {
              Object args[] = new Object[] {archiveEntry};
              String errMsg = MessageFormat.format(bundle.getString("nullZipEntryMessage"), args);
              throw new JarEntryExtractionException(errMsg);
            }
                      
          }
          catch (IOException ioe)
          {
            System.err.println("IOException extracting entry: '" + ioe.getMessage() + "'");
          }
          catch (JarEntryExtractionException jeee)
          {
            JOptionPane jop = new JOptionPane(jeee.getMessage(), JOptionPane.ERROR_MESSAGE, 
                                              JOptionPane.DEFAULT_OPTION);
            JDialog dialog = jop.createDialog(extractDialog, bundle.getString("exceptionExtractingEntryTitle"));
            dialog.setLocationRelativeTo(extractDialog);
            dialog.show();
//            JOptionPane.showMessageDialog(null,
  //                                        jeee.getMessage(),
    //                                      bundle.getString("exceptionExtractingEntryTitle"),
      //                                    JOptionPane.ERROR_MESSAGE);
          }
          finally
          {
            extractDialog.setVisible(false);
          }

          if (extractSucceeded)
          {
            extractDialog.setVisible(false);
            Object args[] = new Object[] {outputFilename};
            String msg = MessageFormat.format(bundle.getString("createdFileMessage"), args);
            JOptionPane.showMessageDialog(thisPanel,
                                          msg,
                                          bundle.getString("createdFileTitle"),
                                          JOptionPane.INFORMATION_MESSAGE);
          }
          settings.setProperty("extractEntryDirectory", extractToField.getText());
          settings.setProperty("expandExtractionDirectory", (new Boolean(expandPathCheckBox.isSelected())).toString());
          try
          {
            settings.store(new FileOutputStream(SETTINGS_FILENAME), "JarView application settings file");
          }
          catch (IOException ioe)
          {
            System.err.println("IOException storing application settings: '" + ioe.getMessage() + "'");
          }
        }
      });
    }

    protected void layOutComponents()
    {
      xseGbl = new GridBagLayout();
      xseGbc = new GridBagConstraints();
      contentPane.setLayout(xseGbl);

      xseGbc.gridx = 0; xseGbc.gridy = 0; xseGbc.gridwidth = 1; xseGbc.gridheight = 1;
      xseGbc.anchor = GridBagConstraints.WEST; xseGbc.fill = GridBagConstraints.NONE;
      xseGbc.weightx = 0.0; xseGbc.weighty = 0.0; xseGbc.insets = new Insets(5, 5, 5, 5);
      contentPane.add(extractToLabel, xseGbc);

      xseGbc.gridx = 1; xseGbc.gridy = 0; xseGbc.gridwidth = 1; xseGbc.gridheight = 1;
      xseGbc.anchor = GridBagConstraints.WEST; xseGbc.fill = GridBagConstraints.HORIZONTAL;
      xseGbc.weightx = 1.0; xseGbc.weighty = 0.0; xseGbc.insets = new Insets(5, 5, 5, 5);
      contentPane.add(extractToField, xseGbc);

      xseGbc.gridx = 2; xseGbc.gridy = 0; xseGbc.gridwidth = 1; xseGbc.gridheight = 1;
      xseGbc.anchor = GridBagConstraints.WEST; xseGbc.fill = GridBagConstraints.NONE;
      xseGbc.weightx = 0.0; xseGbc.weighty = 0.0; xseGbc.insets = new Insets(5, 5, 5, 5);
      contentPane.add(browseButton, xseGbc);

      xseGbc.gridx = 1; xseGbc.gridy = 1; xseGbc.gridwidth = 1; xseGbc.gridheight = 1;
      xseGbc.anchor = GridBagConstraints.WEST; xseGbc.fill = GridBagConstraints.HORIZONTAL;
      xseGbc.weightx = 1.0; xseGbc.weighty = 0.0; xseGbc.insets = new Insets(5, 5, 5, 5);
      contentPane.add(expandPathCheckBox, xseGbc);

      xseGbc.gridx = 2; xseGbc.gridy = 1; xseGbc.gridwidth = 1; xseGbc.gridheight = 1;
      xseGbc.anchor = GridBagConstraints.WEST; xseGbc.fill = GridBagConstraints.NONE;
      xseGbc.weightx = 0.0; xseGbc.weighty = 0.0; xseGbc.insets = new Insets(5, 5, 5, 5);
      contentPane.add(extractButton, xseGbc);
    }
  }
}
