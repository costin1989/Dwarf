package ro.ase.ism;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;




import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class App extends JApplet implements ActionListener, ExceptionListener, MessageListener {

    private JPanel mainPanel;
    private StatusPanel statusBar;
    private JTextArea txArea, sxArea;
    JButton connectButton, exitButton, subjectClearButton, taskClearButton;
    private boolean createExitButton = false;

    @Override
    public void init() {
        initGUI();
        //    initJMS();
    }

    @Override
    public void destroy() {
        shutdownJMS();
        shutdownGUI();
    }

    private void enableExit() {
        createExitButton = true;
    }

    private void initGUI() {
        // The application window contains the 'mainPanel' container
        // and a status bar.
        Container content = getContentPane();

        // Create the mainPanel container. It holds all the UI
        // components...
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        content.add("Center", mainPanel);

        // Create the status bar..
        statusBar = new StatusPanel();
        content.add("South", statusBar);

        //
        // Now start populating mainPanel...
        //

        // dialogPanel contains JMS configuration and the connect
        // button.
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(createMyBorder("Applet control pannel"));

        JPanel dummyPanel;


        dummyPanel = new JPanel();
        dummyPanel.setLayout(new BoxLayout(dummyPanel, BoxLayout.X_AXIS));

        connectButton = new JButton();
        connectButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 0, 3, 3),
                connectButton.getBorder()));
        connectButton.addActionListener(this);
        setConnectButton("Connect");
        dummyPanel.add(connectButton);

        if (createExitButton) {
            exitButton = new JButton("Exit");
            exitButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(3, 3, 3, 3),
                    exitButton.getBorder()));
            exitButton.addActionListener(this);
            dummyPanel.add(exitButton);
        }

        dialogPanel.add(dummyPanel);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new GridLayout(2, 1));

        dummyPanel = new JPanel();
        dummyPanel.setLayout(new BoxLayout(dummyPanel, BoxLayout.Y_AXIS));
        dummyPanel.setBorder(createMyBorder("Received subject "));

        sxArea = new JTextArea();
        sxArea.setEditable(false);
        JScrollPane spane = new JScrollPane(sxArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dummyPanel.add(spane);

        subjectClearButton = new JButton("Clear Subject");
        subjectClearButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3),
                subjectClearButton.getBorder()));
        subjectClearButton.addActionListener(this);
        dummyPanel.add(subjectClearButton);
        messagePanel.add(dummyPanel);

        dummyPanel = new JPanel();
        dummyPanel.setLayout(new BoxLayout(dummyPanel, BoxLayout.Y_AXIS));
        dummyPanel.setBorder(createMyBorder("Received passwords "));

        txArea = new JTextArea();
        txArea.setEditable(false);
        spane = new JScrollPane(txArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dummyPanel.add(spane);

        taskClearButton = new JButton("Clear Tasks");
        taskClearButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3),
                taskClearButton.getBorder()));
        taskClearButton.addActionListener(this);
        dummyPanel.add(taskClearButton);
        messagePanel.add(dummyPanel);

        mainPanel.add("North", dialogPanel);
        mainPanel.add("Center", messagePanel);
    }

    private void initJMS() {
        Context jndiContext;
        try {
            System.out.println("DoConnect Method!");
            System.out.println("Host: " + JMSHost + " Port:" + JMSPort);
            Properties prop = new Properties();
            prop.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
            prop.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
            prop.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

            System.setProperty("org.omg.CORBA.ORBInitialHost", JMSHost);
            System.setProperty("org.omg.CORBA.ORBInitialPort", JMSPort);

            jndiContext = new InitialContext(prop);
            System.out.println("Context created!");
            connectionFactory = (ConnectionFactory) jndiContext.lookup("jms/connectionFactory");
            System.out.println("ConnectionFactory found");
            responseQueue = (Queue) jndiContext.lookup("jms/responseQueue");
            tasksQueue = (Queue) jndiContext.lookup("jms/taskQueue");
            subjectQueue = (Topic) jndiContext.lookup("jms/subjectQueue");
            System.out.println("Destinations found!");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
        }
    }

    private void shutdownGUI() {
        remove(mainPanel);
        mainPanel = null;
    }

    private void shutdownJMS() {
        doDisconnect();
    }

    @Override
    public void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Connect")) {
            doConnect();
        }

        if (e.getActionCommand().equals("Disconnect")) {
            statusBar.setStatusLine("Disconnecting...");
            doDisconnect();
            statusBar.setStatusLine("Connection closed.");
        }

        if (e.getActionCommand().equals("Clear Subject")) {
            sxArea.setText(null);
        }

        if (e.getActionCommand().equals("Clear Tasks")) {
            txArea.setText(null);
        }

        if (e.getActionCommand().equals("Exit")) {
            doDisconnect();
            System.exit(0);
        }
    }

    public void updateRxArea(String s) {
        sxArea.setText(s);
        sxArea.append("\n");
    }

    public void updateTxArea(String s) {
        txArea.setText(s);
        txArea.append("\n");
    }

    public void enableConnectButton() {
        setConnectButton("Connect");
    }

    public void enableDisconnectButton() {
        setConnectButton("Disconnect");
    }
    String JMSHost = "remotehost";
    String JMSPort = "3700";
    ConnectionFactory connectionFactory = null;
    Connection connection = null;
    Session session = null;
    Topic subjectQueue = null;
    TopicSubscriber topicSubscriber = null;
    Queue responseQueue = null;
    Queue tasksQueue = null;
    MessageConsumer taskConsumer = null;
    MessageConsumer subjectConsumer = null;
    MessageProducer responseProducer = null;
    TextMessage textMessage = null;
    String subject = null;
    static long passwordCounter;
    String clientId = null;

    public void doConnect() {
        initJMS();
        try {
            connection = connectionFactory.createConnection();
            clientId = InetAddress.getLocalHost().getHostAddress() + "/" + InetAddress.getLocalHost().getHostName();
            connection.setClientID(clientId);
            connection.setExceptionListener(this);
            connection.start();
            System.out.println("Connection approved");

            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            System.out.println("Session approved");

            topicSubscriber = session.createDurableSubscriber(subjectQueue, "subjectSubscriber");
            topicSubscriber.setMessageListener(this);
            System.out.println("Subject queue approved");


            statusBar.setStatusLine("Connected");
            enableDisconnectButton();
        } catch (JMSException e) {
            updateRxArea("doConnect : " + e.toString() + "\n");
            statusBar.setStatusLine("Unable to connect.");
            System.err.println(e.getMessage());
            if (e.getLinkedException() != null) {
                System.err.println(e.getLinkedException().getMessage());
            }
        } catch (UnknownHostException ex) {
            updateRxArea("doConnect : " + ex.toString() + "\n");
            statusBar.setStatusLine("Unable to connect.");
            System.err.println(ex.getMessage());
        }
    }

    private String doDecrypt(String password) {
        return DecryptFile.getDecryptFile(subject, password);
    }

    public void doSend(String s) {
        if (responseProducer == null) {
            statusBar.setStatusLine("Not connected.");
            return;
        }

        try {
            textMessage.setText(s);
            responseProducer.send(textMessage);
            System.out.println("Message sent:" + s);
        } catch (JMSException e) {
            updateRxArea("doSend : " + e.toString() + "\n");
            System.err.println(e.getMessage());
        }
    }

    public void doDisconnect() {
        try {
            topicSubscriber.close();
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        connection = null;
        session = null;
        subjectQueue = null;
        taskConsumer = null;
        subjectConsumer = null;
        responseProducer = null;
        textMessage = null;
        subject = null;

        enableConnectButton();
    }

    @Override
    public void onException(JMSException e) {
        statusBar.setStatusLine("Connection lost : " + e.toString());
        doDisconnect();
    }

    @Override
    public void onMessage(Message m) {
        try {
            if (m instanceof TextMessage) {
                String s = ((TextMessage) m).getText();
                if (m.getJMSDestination() instanceof Topic) {
                    //subject queue
                    boolean connected = false;
                    if (subject != null) {
                       connected = true;
                    }
                    updateRxArea(s);
                    subject = s;
                    m.acknowledge();
                    if (!connected) {
                        startProcessing();
                    }
                } else {
                    if (subject != null && s != null) {
                        String[] task = s.split(",");
                        for (int i = 0; i < task.length; i++) {
                            passwordCounter++;
                            updateTxArea(passwordCounter + " passwords");
                            String decrypted = doDecrypt(task[i]);
                            boolean isText = checkText(decrypted);
                            if (isText) {
                                doSend(clientId + "," + Calendar.getInstance().getTime() + "," + task[i] + "," + decrypted);
                            }
                        }
                        m.acknowledge();
                    }
                }
            }
        } catch (JMSException e) {
            System.err.println(e.getMessage());
            updateRxArea("onMessage : " + e.toString() + "\n");
        }
    }

    private void setConnectButton(String text) {
        connectButton.setText(text);
        connectButton.setActionCommand(text);
        connectButton.invalidate();
        connectButton.validate();
        mainPanel.repaint();
    }

    private javax.swing.border.Border createMyBorder(String title) {
        javax.swing.border.Border inner = BorderFactory.createLineBorder(Color.black);
        if (title != null) {
            inner = BorderFactory.createTitledBorder(inner, title);
        }
        javax.swing.border.Border outer = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        return BorderFactory.createCompoundBorder(outer, inner);
    }

    public void cleanupAndExit() {
        destroy();
        System.exit(0);
    }
    public static App mq = null;

    public static void mainWindowClosed() {
        mq.cleanupAndExit();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("MQApplet");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindowClosed();
            }
        });

        mq = new App();
        mq.enableExit();

        if (args.length > 1) {
            mq.JMSHost = args[0];
            mq.JMSPort = args[1];
        }
        mq.init();
        mq.start();
        f.getContentPane().add("Center", mq);
        f.setSize(600, 600);
        f.show();
    }

    private boolean checkText(String decrypted) {
        if (decrypted == null) {
            return false;
        } else if (!decrypted.matches("^\\p{ASCII}*$")) {
            return false;
        }
        return true;


    }

    private void startProcessing() throws JMSException {
        taskConsumer = session.createConsumer(tasksQueue);
        taskConsumer.setMessageListener(this);
        System.out.println("Task queue approved");

        responseProducer = session.createProducer(responseQueue);
        System.out.println("Response queue approved");

        textMessage = session.createTextMessage();

        statusBar.setStatusLine("Running");
    }

    class StatusPanel extends JPanel {

        private JLabel label = null;

        public StatusPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLoweredBevelBorder());
            label = new JLabel();

            int size = label.getFont().getSize();
            label.setFont(new Font("Serif", Font.PLAIN, size));
            add("West", label);

            setStatusLine("Ready");
        }

        private void setStatusLine(String statusLine) {
            if (statusLine == null) {
                statusLine = "";
            }

            label.setText(statusLine);
            invalidate();
            validate();
            repaint();
        }
    }
}

/*
 * EOF
 */
