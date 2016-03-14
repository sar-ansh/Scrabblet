package scrabblet;

/**
 *
 * @author sar-ansh
 */

import java.io.* ;
import java.net.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.applet.* ;

/*
<applet code=Scrabblet.class width=400 height=450>
</applet>
*/

public class Scrabblet extends Applet implements ActionListener {
    private ServerConnection server;
    private String serverName;
    private Bag bag;
    private Board board;
    private boolean single = false;
    private boolean ourturn;
    private boolean seen_pass = false;
    private Letter theirs[] = new Letter[7];
    private String name;
    private String others_name;
    
    private Panel topPanel;
    private Label prompt;
    private TextField namefield;
    private Button done;
    private TextField chat;
    
    private List idList;
    private Button challenge;
    private Canvas ican;
    
    public void init(){
        setLayout(new BorderLayout());
        serverName = getCodeBase().getHost();
        if (serverName.equals(""))
            serverName = "localhost";
        ican = new IntroCanvas();
    }
    
    public void start(){
        try{
            showStatus("Connecting to " + serverName);
            server = new ServerConnection(this, serverName);
            server.start();
            showStatus("Connected: " + serverName);
            
            if (name == null){
                prompt = new Label("Enter your name here: ");
                namefield = new TextField(20);
                namefield.addActionListener(this);
                topPanel = new Panel();
                topPanel.setBackground(new Color(255, 255, 200));
                topPanel.add(prompt);
                topPanel.add(namefield);
                add("North", topPanel);
                add("Center", ican);
            }
            else{
                if (chat != null){
                    remove(chat);
                    remove(board);
                    remove(done);
                }
                nameEntered(name);
            }
            validate();
        }
        catch (Exception e) {
            single = true;
            start_Game((int)(0x7fffffff * Math.random()));
        }
    }
    
    public void stop() {
        if (!single)
            server.quit();
    }
    
    void add(String id, String hostname, String name) {
        delete(id); // in case it's already there.
        idList.add("(" + id + ") " + name + "@" + hostname);
        showStatus("Choose a player from the list");
    }
    
    void delete(String id) {
        for (int i = 0; i < idList.getItemCount(); i++) {
            String s = idList.getItem(i);
            s = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
            if (s.equals(id)){
                idList.remove(i);
                break;
            }
        }
        if (idList.getItemCount() == 0 && bag == null)
            showStatus("Wait for other players to arrive.");
    }
    
    // we've been challenged to a game by "id".
    void challenge(String id) {
        ourturn = false;
        int seed = (int)(0x7fffffff * Math.random());
        others_name = getName(id); // who was it?
        showStatus("challenged by " + others_name);
        
        // put some confirmation here...
        
        server.accept(id, seed);
        server.delete();
        start_Game(seed);
    }
    
    // our challenge as accepted.
    void accept(String id, int seed) {
        ourturn = true;
        others_name = getName(id);
        server.delete();
        start_Game(seed);
    }
    
    void chat(String id, String a) {
        showStatus(others_name + ": " + s);
    }
    
    // the other guy moved, and placed 'letter' at (x, y).
    void move(String letter, int x, int y) {
        for(int i = 0; i < 7; i++) {
            if (theirs[i] != null && theirs[i].getSymbol().equals(letter)) {
                Letter already = board.getLetter(x, y);
                if (already != null) {
                    board.moveLetter(already, 15, 15); // on the tray.
                }
                board.moveLetter(theirs[i], x, y);
                board.commitLetter(theirs[i]);
                theirs[i] = bag.takeOut();
                if (theirs[i] == null)
                    showStatus("No more letters");
                break;
            }
        }
        board.repaint();
    }
    
    void turn(int score, String words) {
        showStatus(others_name + " played: " + words + " worth " + score);
        done.setEnabled(true);
        board.othersTurn(score);
    }
    
    void quit(String id) {
        showStatus(others_name + " just quit.");
        remove(chat);
        remove(board);
        remove(done);
        nameEntered(name);
    }
    
    private void nameEntered(String s) {
        if (s.equals(""))
            return;
        name = s;
        if (ican != null)
            remove(ican);
        if (idList != null)
            remove(idList);
        if (challenge != null)
            remove(challenge);
        idList = new List(10, false);
        add("Center", idList);
        challenge = new Button("Challenge");
        challenge.addActionListener(this);
        add("North", challenge);
        validate();
        server.setName(name);
        showStatus("Wait for other players to arrive.");
        if (topPael != null)
            remove(topPanel);
    }
    
    private void wepick() {
        for (int i = 0; i < 7; i++) {
            Letter l = bag.takeOut();
            board.addLetter(1);
        }
    }
    
    private void theypick() {
        for (int i = 0; i < 7; i++) {
            Letter l = bag.takeOut();
            theirs[i] = l;
        }
    }
    
    private void start_Game(int seed) {
        if (single) {
            Frame popup = new Frame("Scrabblet");
            popup.setSize(400, 300);
            popup.add("Center", ican);
            popup.setResizable(false);
            popup.show();
            board = new Board();
            showStatus("no server found, playing solo");
            ourturn = true;
        }
        else{
            remove(idList);
            remove(challenge);
            board = new Board(name, others_name);
            chat = new TextField();
            chat.addActionListener(this);
            add("North", chat);
            showStatus("playing against " + others_name);
        }
        
        add("Center", board);
        done = newButton("Done");
        done.addActionListener(this);
        add("South", done);
        validate();
        
        bag = new Bag(seed);
        if (ourturn) {
            wepick();
            if (!single)
                theypick();
        }
        else {
            done.setEnabled(false);
            theypick();
            wepick();
        }
        board.repaint();
    }
    
    private void challenge_them() {
        String s = idList.getSelectedItem();
        if (s == null) {
            showStatus("Choose a player from the list then press Challenge");
        }
        else {
            remove(challenge);
            remove(idList);
            String destid = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
            showStatus("challenging: " + destid);
            server.challenge(destid); // accept will get called if they accept.
            validate();
        }
    }
    
    private void our_turn() {
        String word = board.findwords();
        if (word == null)
            showStatus("Illegal letter positions");
        else{
            if ("".equals(word)) {
                if (single)
                    return;
                if (seen_pass) {
                    done.setEnabled(false);
                    server.turn("pass", 0);
                    showStatus("You passed");
                    seen_pass = false;
                }
                else {
                    showStatus("Press done again to pass");
                    seen_pass = true;
                    return;
                }
            }
            else
                seen_pass = false;
            showStatus(word);
            board.commit(server);
            for( int i = 0; i < 7; i++) {
                if (board.getTray(i) == null) {
                    Letter l = bag.takeOut();
                    if (l == null)
                        showStatus("No more letters");
                    else
                        board.addLetter(l);
                }
            }
            if (!single) {
                done.setEnabled(false);
                server.turn(word, board.getTurnSore());
            }
            board.repaint();
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == chat) {
            server.chat(chat.getText());
            chat.setText("");
        }
        else if (source == challenge)
            challenge_them();
        else if (source == done)
            our_turn();
        else if (source == namefield) {
            TextComponent tc = (TextComponent)source;
            nameEntered(tc.getText());
        }
    }
}
