package scrabblet;

import java.awt.* ;
import java.awt.event.* ;

class IntroCanvas extends Canvas {
    private Color pink = new Color(255, 200, 200);
    private Color blue = new Color(150, 200, 255);
    private Color yellow = new Color(250, 220, 100);
    
    private int w, h;
    private int edge = 16;
    private static final String title = "Scrabblet";
    private static final String name = "Copyright 2016 - Saransh Gupta";
    private Font namefont, titlefont;
    
    IntroCanvas() {
        setBackground(yellow);
        titlefont = new Font("SansSerif", Font.BOLD, 58);
        namefont = new Font("SansSerif", Font.BOLD, 18);
        addMouseListener(new MyMouseAdapter());
    }
    
    private void d(Graphics g, String s, Color c, Font f, int y, int off) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(c);
        g.drawString(s, (w - fm.stringWidth(s)) / 2 + off, y + off);
    }
    
    public void paint(Graphics g) {
        Dimension d = getSize();
        w = d.width;
        h = d.height;
        g.setColor(blue);
        g.fill3DRect(edge, edge, w - 2 * edge, h - 2 * edge, true);
        d(g, title, Color.black, titlefont, h / 2, 1);
        d(g, title, Color.white, titlefont, h / 2, -1);
        d(g, title, pink, titlefont, h / 2, 0);
        d(g, name, Color.black, namefont, h * 3 / 4, 0);
    }
    
    class MyMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            ((Frame)getParent()).setVisible(false);
        }
    }
}