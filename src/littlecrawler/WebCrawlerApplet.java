package littlecrawler;

import utils.Log;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * User: gcavalli
 * Date: Mar 8, 2007
 * Time: 2:27:51 PM
 * Last Edition Date: Jan 6, 2009
 */
public class WebCrawlerApplet
        extends JPanel // Used to be a JApplet. The Applet API was removed in JDK 26.
        implements ActionListener
{
    private WebCrawlerProperties webCrawlerProperties;

    private JTextArea listMatches;
    private JLabel labelStatus;
    private JTextField textURL;
    private JTextField textHostAndPath;
    private JTextField textDestinationFolder;
    // Choice choiceType;

    private Log log;
    private SearchThread searchThread;
    private boolean stopping = false;
    private java.util.List<JButton> initButtons = new ArrayList<JButton>();
    private java.util.List<JButton> progressButtons = new ArrayList<JButton>();

    public WebCrawlerApplet()
            throws IOException
    {
        this(new File(WebCrawlerProperties.DEFAULT_CONFIG_FILE));
    }

    public WebCrawlerApplet(File configFile)
            throws IOException
    {
        super();
        webCrawlerProperties = new WebCrawlerProperties(configFile);
    }

    public void init()
    {
        // set up the main UI panel
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout(5, 5));

        // text entry components
        JPanel panelEntry = new JPanel();
        panelEntry.setLayout(new BorderLayout(5, 5));

        JPanel panelURL = new JPanel();
        panelURL.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel labelURL = new JLabel("Starting Location: ", Label.RIGHT);
        panelURL.add(labelURL);
        textURL = new JTextField(webCrawlerProperties.getStartingLocation(), 40);
        panelURL.add(textURL);

        JLabel labelURL2 = new JLabel("Search Within Path: ", Label.RIGHT);
        panelURL.add(labelURL2);
        textHostAndPath = new JTextField(webCrawlerProperties.getDefaultHostAndPath(), 40); // (includedHostsAndPaths[0], 40);
        panelURL.add(textHostAndPath);

        Panel panelDestinationFolder = new Panel();
        panelDestinationFolder.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel labelDestinationFolder = new JLabel("Destination Folder: ", Label.RIGHT);
        panelDestinationFolder.add(labelDestinationFolder);
        textDestinationFolder = new JTextField(webCrawlerProperties.getDestinationFolderString(), 90);
        panelDestinationFolder.add(textDestinationFolder);

        Panel panelNorth = new Panel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.add(panelURL);
        panelNorth.add(panelDestinationFolder);

        panelEntry.add(BorderLayout.NORTH, panelNorth);
        panelMain.add(BorderLayout.NORTH, panelEntry);

        // list of result URLs
        JPanel panelListButtons = new JPanel();
        panelListButtons.setLayout(new BorderLayout(5, 5));

        JPanel panelList = new JPanel();
        panelList.setLayout(new BorderLayout(5, 5));
        JLabel labelResults = new JLabel("Progress Log:");
        panelList.add(BorderLayout.NORTH, labelResults);
        JPanel panelListCurrent = new JPanel();
        panelListCurrent.setLayout(new BorderLayout(5, 5));
        listMatches = new JTextArea(15, 80);
        listMatches.setEditable(false);
        ((DefaultCaret) listMatches.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // Keeps the last line in view, also when it is added by a worker thread.
        panelListCurrent.add(BorderLayout.NORTH, new JScrollPane(listMatches));
        labelStatus = new JLabel(" "); // Not empty, so the window reserves the height of the status line from the start.
        panelListCurrent.add(BorderLayout.SOUTH, labelStatus);
        panelList.add(BorderLayout.SOUTH, panelListCurrent);
        panelListButtons.add(BorderLayout.NORTH, panelList);

        // control buttons
        JPanel panelButtons = new JPanel();
        initButtons.add(addButton(panelButtons, DOWNLOAD));
        progressButtons.add(addButton(panelButtons, SHOW_STATUS));
        progressButtons.add(addButton(panelButtons, STOP));
        changeEnabledButtons(false);

        panelListButtons.add(BorderLayout.SOUTH, panelButtons);

        panelMain.add(BorderLayout.SOUTH, panelListButtons);

        setLayout(new BorderLayout()); // Same layout as the content pane of the former JApplet.
        add(panelMain, BorderLayout.CENTER);
        setVisible(true);

        repaint();

        // set default for URL access
        URLConnection.setDefaultAllowUserInteraction(false);
    }

    private void updateProperties()
    {
        webCrawlerProperties.setStartingLocation(textURL.getText());
        webCrawlerProperties.setDefaultHostAndPath(textHostAndPath.getText());
        webCrawlerProperties.setDestinationFolder(textDestinationFolder.getText());
    }

    private JButton addButton(Container c, String name)
    {
        JButton button = new JButton(name);
        button.addActionListener(this);
        c.add(button);
        c.add(new JSeparator());
        return button;
    }

    public boolean isStopping()
    {
//        return Thread.currentThread() != searchThread;
        return stopping;
    }

    public void setStopping()
    {
        /*
        // searchThread.stop();
        searchThread = null;
        */
        stopping = true;
        changeEnabledButtons(false);
    }

    public synchronized void addMatch(String strLink)
    {
        appendToListMatches("+ Found: " + strLink);
    }

    public void stop()
    {
        if (!stopping) {  // TODO: Used to be: searchThread != null 
            setStatus("Stopping..."); // At this point the searchThread is not null
            log.flush();
            setStopping(); // searchThread is set as null here (this has been changed now)
            synchronized (searchThread) {
                searchThread.notify(); // The thread might be waiting for more links at this point...
            }
        }
    }

    /** TODO What for? */
    public void destroy()
    {
    }

    /*
    public void paint(Graphics g) {
        //Draw a Rectangle around the applet's display area.
        g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);

        panelMain.paint(g);
        panelMain.paintComponents(g);
        // update(g);
        // panelMain.update(g);
    }
    */

//    private static final int HREF = 0;

    private synchronized void appendToListMatches(String str)
    {
        listMatches.append(str + '\n');
    }

    public synchronized void setStatus(String status)
    {
        if (searchThread != null) { // This shows the number of pending items (as long as the object reference is not null)
            status += " [pending items: " + searchThread.getNbrPendingItems() + "]";
        }
        if (log != null) log.add(Thread.currentThread(), "!!! Status: " + status);
        appendToListMatches("* Status: " + status);
        labelStatus.setText(status);
    }

    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();

        if (command.compareTo(DOWNLOAD) == 0) {
            stopping = false;
            updateProperties();
            log = new Log("crawler_", webCrawlerProperties.getDestinationFolder(), true);  // TODO: Debug: paranoid: true.
            searchThread = new SearchThread(this, webCrawlerProperties, log);
            searchThread.start();
            changeEnabledButtons(true);
        } else if (command.compareTo(SHOW_STATUS) == 0) {
            if (searchThread != null) {
                JOptionPane.showMessageDialog(this, searchThread.getStatus());
            }
        } else if (command.compareTo(STOP) == 0) {
            if (searchThread != null) {
                stop();
            }
        }
    }

    private void changeEnabledButtons(boolean inProgress)
    {
        for (JButton b: initButtons) {
            b.setEnabled(!inProgress);
        }

        for (JButton b: progressButtons) {
            b.setEnabled(inProgress);
        }
    }

    public void closeApplet()
    {
        setStatus("Exit.");
        if (log != null) log.close();
    }

    public static void main(String argv[])
    {
        try {
            Frame f = new Frame("Site Downloader");
            // The first argument, when present, is the path of the properties file to use.
            String configPath = (argv.length > 0) ? argv[0] : WebCrawlerProperties.DEFAULT_CONFIG_FILE;
            final WebCrawlerApplet applet = new WebCrawlerApplet(new File(configPath));
            f.add("Center", applet);
            f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    applet.closeApplet();
                    System.exit(0);
                }
            });

            applet.init();
            f.pack();
            f.setResizable(false);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private static final String DOWNLOAD = "Download";
    private static final String SHOW_STATUS = "Show Detailed Status";
    private static final String STOP = "Stop";
//    private static final String DISALLOW = "Disallow:";
}
