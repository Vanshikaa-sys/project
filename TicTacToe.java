import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;

public class TicTacToe extends JFrame {

    private static final int WINDOW_W = 520;
    private static final int WINDOW_H = 680;
    private static final Color COL_BG1    = new Color(26, 26, 46);
    private static final Color COL_BG2    = new Color(22, 33, 62);
    private static final Color COL_BG3    = new Color(15, 52, 96);
    private static final Color COL_X      = new Color(255, 107, 157);
    private static final Color COL_O      = new Color(103, 212, 247);
    private static final Color COL_GOLD   = new Color(255, 215, 0);
    private static final Color COL_PURPLE = new Color(102, 126, 234);
    private static final Color COL_WHITE  = new Color(255, 255, 255, 220);
    private static final Color COL_DIM    = new Color(255, 255, 255, 80);
    private static final Color COL_CELL   = new Color(255, 255, 255, 25);
    private static final Color COL_CELL_H = new Color(255, 255, 255, 55);
    private static final Color COL_WIN    = new Color(255, 215, 0, 60);

    private static final int[][] WIN_COMBOS = {
        {0,1,2},{3,4,5},{6,7,8},
        {0,3,6},{1,4,7},{2,5,8},
        {0,4,8},{2,4,6}
    };

    private String[] board = new String[9];
    private String currentPlayer = "X";
    private boolean gameOver = false;
    private String mode = "2p";       
    private String difficulty = "easy";
    private int scoreX = 0, scoreO = 0, scoreD = 0;
    private List<String> history = new ArrayList<>();
    private int[] winCells = null;
    private boolean soundOn = true;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private AnimatedBgPanel rulesPanel;
    private AnimatedBgPanel gamePanel;

    private JLabel lblScoreX, lblScoreO, lblScoreD;
    private JLabel lblTurnSymbol, lblTurnName, lblResult, lblMode;
    private JLabel lblThinking;
    private CellButton[] cells = new CellButton[9];
    private JLabel lblHistX, lblHistO, lblHistD;
    private JButton btnMode2p, btnModeAi;
    private JButton btnDiffE, btnDiffM, btnDiffH;
    private JPanel diffPanel;
    private ScoreCard scX, scO, scD;


    private Timer bgTimer, confettiTimer;
    private List<Particle> particles = new ArrayList<>();
    private List<Confetti> confettiList = new ArrayList<>();
    private Random rng = new Random();

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(WINDOW_W, WINDOW_H);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        rulesPanel = buildRulesPanel();
        gamePanel  = buildGamePanel();

        mainPanel.add(rulesPanel, "rules");
        mainPanel.add(gamePanel,  "game");

        add(mainPanel);

        initParticles();
        startBgTimer();

        cardLayout.show(mainPanel, "rules");
        setVisible(true);
    }

    private AnimatedBgPanel buildRulesPanel() {
        AnimatedBgPanel p = new AnimatedBgPanel();
        p.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(30, 30, 20, 30));

       
        JLabel title = makeLabel("X · O", 38, Font.BOLD, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tag = makeLabel("TIC TAC TOE", 11, Font.PLAIN, COL_DIM);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(title); inner.add(gap(4)); inner.add(tag); inner.add(gap(20));

        GlassCard rulesCard = new GlassCard();
        rulesCard.setLayout(new BoxLayout(rulesCard, BoxLayout.Y_AXIS));
        rulesCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        rulesCard.add(makeLabel("HOW TO PLAY", 10, Font.BOLD, COL_GOLD));
        rulesCard.add(gap(10));
        String[] rules = {
            "1.  Two players take turns marking X and O on a 3×3 grid.",
            "2.  Get 3 symbols in a row — horizontal, vertical or diagonal — to win.",
            "3.  If all 9 cells fill up with no winner, the match is a Draw.",
            "4.  Score persists across rounds. Restart only resets the board.",
            "5.  In AI mode, choose Easy, Medium, or Hard difficulty."
        };
        for (String r : rules) {
            JLabel rl = makeLabel("<html><body style='width:360px'>" + r + "</body></html>",
                    13, Font.PLAIN, COL_WHITE);
            rl.setBorder(new EmptyBorder(0, 0, 6, 0));
            rulesCard.add(rl);
        }
        inner.add(rulesCard); inner.add(gap(18));

        JLabel modeLabel = makeLabel("GAME MODE", 10, Font.BOLD, COL_DIM);
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(modeLabel); inner.add(gap(8));

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        modeRow.setOpaque(false);
        btnMode2p = modeBtn("👥  2 Players", true);
        btnModeAi = modeBtn("🤖  vs AI", false);
        btnMode2p.addActionListener(e -> selectMode("2p"));
        btnModeAi.addActionListener(e -> selectMode("ai"));
        modeRow.add(btnMode2p); modeRow.add(btnModeAi);
        inner.add(modeRow); inner.add(gap(14));

       
        diffPanel = new JPanel();
        diffPanel.setOpaque(false);
        diffPanel.setLayout(new BoxLayout(diffPanel, BoxLayout.Y_AXIS));
        JLabel diffLabel = makeLabel("AI DIFFICULTY", 10, Font.BOLD, COL_DIM);
        diffLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffPanel.add(diffLabel); diffPanel.add(gap(6));
        JPanel diffRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        diffRow.setOpaque(false);
        btnDiffE = diffBtn("Easy",   true);
        btnDiffM = diffBtn("Medium", false);
        btnDiffH = diffBtn("Hard",   false);
        btnDiffE.addActionListener(e -> selectDiff("easy"));
        btnDiffM.addActionListener(e -> selectDiff("medium"));
        btnDiffH.addActionListener(e -> selectDiff("hard"));
        diffRow.add(btnDiffE); diffRow.add(btnDiffM); diffRow.add(btnDiffH);
        diffPanel.add(diffRow);
        diffPanel.setVisible(false);
        inner.add(diffPanel); inner.add(gap(20));

      
        RoundButton startBtn = new RoundButton("Start Game");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> startGame());
        inner.add(startBtn);

        p.add(inner, BorderLayout.CENTER);
        return p;
    }


    private AnimatedBgPanel buildGamePanel() {
        AnimatedBgPanel p = new AnimatedBgPanel();
        p.setLayout(new BorderLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(20, 24, 16, 24));

      
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel logo = makeLabel("X · O", 18, Font.BOLD, Color.WHITE);
        lblMode = makeLabel("2 PLAYERS", 10, Font.PLAIN, COL_DIM);
        topRow.add(logo, BorderLayout.WEST);
        topRow.add(lblMode, BorderLayout.EAST);
        inner.add(topRow); inner.add(gap(14));

       
        JPanel scoreRow = new JPanel(new GridLayout(1, 3, 10, 0));
        scoreRow.setOpaque(false);
        scX = new ScoreCard("X Wins", "X", COL_X);
        scD = new ScoreCard("Draws",  "—", COL_GOLD);
        scO = new ScoreCard("O Wins", "O", COL_O);
        lblScoreX = scX.numLabel;
        lblScoreO = scO.numLabel;
        lblScoreD = scD.numLabel;
        scoreRow.add(scX); scoreRow.add(scD); scoreRow.add(scO);
        inner.add(scoreRow); inner.add(gap(14));

        JPanel turnBar = new TurnBar();
        lblTurnSymbol = makeLabel("X", 20, Font.BOLD, COL_X);
        lblTurnName   = makeLabel("Player 1", 13, Font.PLAIN, COL_WHITE);
        ((TurnBar)turnBar).setLabels(lblTurnSymbol, lblTurnName);
        inner.add(turnBar); inner.add(gap(6));

        
        lblThinking = makeLabel("AI is thinking…", 11, Font.PLAIN, COL_DIM);
        lblThinking.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblThinking.setVisible(false);
        inner.add(lblThinking); inner.add(gap(6));

       
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        boardPanel.setOpaque(false);
        boardPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        boardPanel.setMaximumSize(new Dimension(320, 320));
        boardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            cells[i] = new CellButton();
            cells[i].addActionListener(e -> handleClick(idx));
            boardPanel.add(cells[i]);
        }
        inner.add(boardPanel); inner.add(gap(12));

      
        lblResult = makeLabel(" ", 17, Font.BOLD, Color.WHITE);
        lblResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(lblResult); inner.add(gap(10));

       
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        RoundButton restartBtn = new RoundButton("Restart Round");
        RoundButton menuBtn    = new RoundButton("Main Menu");
        menuBtn.setColors(new Color(255,255,255,40), new Color(255,255,255,70));
        restartBtn.addActionListener(e -> restartRound());
        menuBtn.addActionListener(e -> showMenu());
        btnRow.add(restartBtn); btnRow.add(menuBtn);
        inner.add(btnRow); inner.add(gap(12));

        
        JPanel histRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        histRow.setOpaque(false);
        lblHistX = histBadge("", COL_X,    new Color(255,107,157,40));
        lblHistO = histBadge("", COL_O,    new Color(103,212,247,40));
        lblHistD = histBadge("", COL_GOLD, new Color(255,215,0,40));
        histRow.add(lblHistX); histRow.add(lblHistO); histRow.add(lblHistD);
        inner.add(histRow);

        p.add(inner, BorderLayout.CENTER);
        return p;
    }

  
    private void startGame() {
        for (int i = 0; i < 9; i++) board[i] = "";
        gameOver = false; currentPlayer = "X"; winCells = null;
        scoreX = scoreO = scoreD = 0; history.clear();
        lblScoreX.setText("0"); lblScoreO.setText("0"); lblScoreD.setText("0");
        lblHistX.setText(""); lblHistO.setText(""); lblHistD.setText("");
        lblResult.setText(" ");
        lblMode.setText(mode.equals("ai") ? "VS AI · " + difficulty.toUpperCase() : "2 PLAYERS");
        renderBoard(); updateTurnUI();
        cardLayout.show(mainPanel, "game");
    }

    private void restartRound() {
        for (int i = 0; i < 9; i++) board[i] = "";
        gameOver = false; currentPlayer = "X"; winCells = null;
        confettiList.clear();
        lblResult.setText(" "); lblThinking.setVisible(false);
        renderBoard(); updateTurnUI();
    }

    private void showMenu() {
        confettiList.clear();
        cardLayout.show(mainPanel, "rules");
    }

    private void handleClick(int idx) {
        if (board[idx] != null && !board[idx].isEmpty()) return;
        if (gameOver) return;
        if (mode.equals("ai") && currentPlayer.equals("O")) return;
        placeSymbol(idx);
    }

    private void placeSymbol(int idx) {
        board[idx] = currentPlayer;
        cells[idx].setValue(currentPlayer);
        playSound("click");

        int[] result = checkWin(board);
        if (result != null) {
            gameOver = true;
            if (result[0] == -1) {
                scoreD++; lblScoreD.setText(String.valueOf(scoreD));
               lblResult.setText("🤝  It's a Draw!");
                history.add("D"); playSound("draw");
            } else {
                winCells = result;
                String winner = board[result[0]];
                for (int w : winCells) cells[w].setWin(true);
                if (winner.equals("X")) { scoreX++; lblScoreX.setText(String.valueOf(scoreX)); }
                else { scoreO++; lblScoreO.setText(String.valueOf(scoreO)); }
                String msg;
                if (mode.equals("ai") && winner.equals("O")) msg = "🤖  AI Wins!";
                else if (mode.equals("ai") && winner.equals("X")) msg = "🎉  You Win!";
                else msg = "🎉  Player " + winner + " Wins!";
                lblResult.setText(msg);
                history.add(winner);
                if (mode.equals("ai") && winner.equals("O")) playSound("lose");
                else { spawnConfetti(); playSound("win"); }
            }
            updateHistory(); return;
        }

        currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        updateTurnUI();

        if (mode.equals("ai") && currentPlayer.equals("O") && !gameOver) {
            lblThinking.setVisible(true);
            int delay = difficulty.equals("hard") ? 600 : 350;
            Timer t = new Timer(delay, e -> { lblThinking.setVisible(false); aiMove(); });
            t.setRepeats(false); t.start();
        }
    }

    private void aiMove() {
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (board[i] == null || board[i].isEmpty()) empty.add(i);
        if (empty.isEmpty()) return;
        int idx;
        if (difficulty.equals("easy")) {
            idx = empty.get(rng.nextInt(empty.size()));
        } else if (difficulty.equals("medium")) {
            if (rng.nextDouble() < 0.45) idx = empty.get(rng.nextInt(empty.size()));
            else idx = bestMove();
        } else {
            idx = bestMove();
        }
        placeSymbol(idx);
    }

    private int bestMove() {
        int best = Integer.MIN_VALUE, bestIdx = -1;
        for (int i = 0; i < 9; i++) {
            if (board[i] == null || board[i].isEmpty()) {
                board[i] = "O";
                int score = minimax(board, false, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board[i] = "";
                if (score > best) { best = score; bestIdx = i; }
            }
        }
        return bestIdx;
    }

    private int minimax(String[] b, boolean isMax, int depth, int alpha, int beta) {
        int[] r = checkWin(b);
        if (r != null) {
            if (r[0] == -1) return 0;
            String winner = b[r[0]];
            return winner.equals("O") ? 10 - depth : depth - 10;
        }
        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == null || b[i].isEmpty()) {
                    b[i] = "O";
                    best = Math.max(best, minimax(b, false, depth+1, alpha, beta));
                    b[i] = "";
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break;
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == null || b[i].isEmpty()) {
                    b[i] = "X";
                    best = Math.min(best, minimax(b, true, depth+1, alpha, beta));
                    b[i] = "";
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break;
                }
            }
            return best;
        }
    }

       private int[] checkWin(String[] b) {
        for (int[] combo : WIN_COMBOS) {
            String a = b[combo[0]];
            if (a != null && !a.isEmpty() && a.equals(b[combo[1]]) && a.equals(b[combo[2]]))
                return combo;
        }
        boolean full = true;
        for (String s : b) if (s == null || s.isEmpty()) { full = false; break; }
        if (full) return new int[]{-1};
        return null;
    }

    private void renderBoard() {
        for (int i = 0; i < 9; i++) {
            cells[i].setValue(board[i] == null ? "" : board[i]);
            cells[i].setWin(false);
        }
    }

    private void updateTurnUI() {
        lblTurnSymbol.setText(currentPlayer);
        lblTurnSymbol.setForeground(currentPlayer.equals("X") ? COL_X : COL_O);
        if (mode.equals("ai") && currentPlayer.equals("O")) lblTurnName.setText("AI");
        else lblTurnName.setText(currentPlayer.equals("X") ? "Player 1" : "Player 2");
        scX.setActive(currentPlayer.equals("X") && !gameOver);
        scO.setActive(currentPlayer.equals("O") && !gameOver);
    }

    private void updateHistory() {
        int x=0, o=0, d=0;
        for (String h : history) { if(h.equals("X"))x++; else if(h.equals("O"))o++; else d++; }
        lblHistX.setText(x>0 ? "X Win: "+x : "");
        lblHistO.setText(o>0 ? "O Win: "+o : "");
        lblHistD.setText(d>0 ? "Draw: "+d  : "");
    }

    private void selectMode(String m) {
        mode = m;
        btnMode2p.setBackground(m.equals("2p") ? new Color(102,126,234,80) : new Color(255,255,255,20));
        btnModeAi.setBackground(m.equals("ai") ? new Color(102,126,234,80) : new Color(255,255,255,20));
        diffPanel.setVisible(m.equals("ai"));
    }

    private void selectDiff(String d) {
        difficulty = d;
        btnDiffE.setBackground(d.equals("easy")   ? new Color(255,215,0,60) : new Color(255,255,255,20));
        btnDiffM.setBackground(d.equals("medium")  ? new Color(255,215,0,60) : new Color(255,255,255,20));
        btnDiffH.setBackground(d.equals("hard")    ? new Color(255,215,0,60) : new Color(255,255,255,20));
        btnDiffE.setForeground(d.equals("easy")   ? COL_GOLD : COL_WHITE);
        btnDiffM.setForeground(d.equals("medium")  ? COL_GOLD : COL_WHITE);
        btnDiffH.setForeground(d.equals("hard")    ? COL_GOLD : COL_WHITE);
    }

   
    private void initParticles() {
        particles.clear();
        for (int i = 0; i < 55; i++) particles.add(new Particle());
    }

    private void startBgTimer() {
        bgTimer = new Timer(30, e -> {
            for (Particle p : particles) p.update(WINDOW_W, WINDOW_H);
            rulesPanel.repaint();
            gamePanel.repaint();
            if (!confettiList.isEmpty()) confettiList.removeIf(c -> c.life <= 0.01);
        });
        bgTimer.start();
    }

    private void spawnConfetti() {
        for (int i = 0; i < 80; i++) confettiList.add(new Confetti(WINDOW_W/2, WINDOW_H/3));
    }

   
    private void playSound(String type) {
        if (!soundOn) return;
        new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt); line.start();
                byte[] buf = generateTone(type, fmt);
                line.write(buf, 0, buf.length);
                line.drain(); line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private byte[] generateTone(String type, AudioFormat fmt) {
        float rate = fmt.getSampleRate();
        if (type.equals("click")) {
            int frames = (int)(rate * 0.12);
            byte[] buf = new byte[frames * 2];
            for (int i = 0; i < frames; i++) {
                float f = 480 + 200f * i / frames;
                float env = 1f - (float)i/frames;
                short s = (short)(env * 8000 * Math.sin(2 * Math.PI * f * i / rate));
                buf[i*2] = (byte)(s & 0xFF); buf[i*2+1] = (byte)((s>>8) & 0xFF);
            }
            return buf;
        } else if (type.equals("win")) {
            int[] freqs = {523,659,784,1047};
            int framesPer = (int)(rate * 0.18);
            byte[] buf = new byte[freqs.length * framesPer * 2];
            for (int n=0;n<freqs.length;n++) for (int i=0;i<framesPer;i++) {
                float env = 1f - (float)i/framesPer;
                short s = (short)(env * 9000 * Math.sin(2*Math.PI*freqs[n]*i/rate));
                int off = (n*framesPer+i)*2;
                buf[off]=(byte)(s&0xFF); buf[off+1]=(byte)((s>>8)&0xFF);
            }
            return buf;
        } else if (type.equals("draw")) {
            int frames = (int)(rate * 0.3);
            byte[] buf = new byte[frames * 2];
            for (int i=0;i<frames;i++) {
                float f = 320 - 160f*i/frames;
                float env = 1f-(float)i/frames;
                short s = (short)(env*6000*(Math.random()-.5)*2);
                buf[i*2]=(byte)(s&0xFF); buf[i*2+1]=(byte)((s>>8)&0xFF);
            }
            return buf;
        } else { 
            int[] freqs = {400,320,260};
            int fp = (int)(rate*0.14);
            byte[] buf = new byte[freqs.length*fp*2];
            for (int n=0;n<freqs.length;n++) for (int i=0;i<fp;i++) {
                float env=1f-(float)i/fp;
                short s=(short)(env*7000*Math.sin(2*Math.PI*freqs[n]*i/rate));
                int off=(n*fp+i)*2; buf[off]=(byte)(s&0xFF); buf[off+1]=(byte)((s>>8)&0xFF);
            }
            return buf;
        }
    }

    
    private JLabel makeLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        return l;
    }

    private Component gap(int h) { return Box.createVerticalStrut(h); }

    private JButton modeBtn(String text, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(selected ? new Color(102,126,234,80) : new Color(255,255,255,20));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(140, 54));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton diffBtn(String text, boolean selected) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(selected ? COL_GOLD : COL_WHITE);
        b.setBackground(selected ? new Color(255,215,0,60) : new Color(255,255,255,20));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(90, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel histBadge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

   
    class AnimatedBgPanel extends JPanel {
        AnimatedBgPanel() { setOpaque(true); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            
            GradientPaint gp = new GradientPaint(0,0, COL_BG1, w, h, COL_BG3);
            g2.setPaint(gp); g2.fillRect(0,0,w,h);

           
            for (int i=0;i<particles.size();i++) {
                Particle pi = particles.get(i);
                for (int j=i+1;j<particles.size();j++) {
                    Particle pj = particles.get(j);
                    double dx=pi.x-pj.x, dy=pi.y-pj.y, d=Math.sqrt(dx*dx+dy*dy);
                    if (d<90) {
                        float alpha = (float)(0.12*(1-d/90));
                        g2.setColor(new Color(102,126,234,Math.max(0,Math.min(255,(int)(alpha*255)))));
                        g2.setStroke(new BasicStroke(0.5f));
                        g2.drawLine((int)pi.x,(int)pi.y,(int)pj.x,(int)pj.y);
                    }
                }
                g2.setColor(pi.color);
                g2.fillOval((int)(pi.x-pi.r),(int)(pi.y-pi.r),(int)(pi.r*2),(int)(pi.r*2));
            }

          
            for (Confetti c : confettiList) {
                c.update();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)Math.max(0,c.life)));
                g2.setColor(c.color);
                g2.translate(c.x, c.y);
                g2.rotate(Math.toRadians(c.rot));
                if (c.isRect) g2.fillRect(-(int)c.r,-(int)(c.r/2),(int)(c.r*2),(int)c.r);
                else { g2.fillOval(-(int)(c.r/2),-(int)(c.r/2),(int)c.r,(int)c.r); }
                g2.rotate(-Math.toRadians(c.rot));
                g2.translate(-c.x,-c.y);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
            }
        }
    }

   
    class GlassCard extends JPanel {
        GlassCard() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,18));
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),18,18));
            g2.setColor(new Color(255,255,255,45));
            g2.setStroke(new BasicStroke(0.5f));
            g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,18,18));
            super.paintComponent(g);
        }
    }

   
    class ScoreCard extends JPanel {
        JLabel numLabel;
        boolean active = false;
        ScoreCard(String labelText, String symbol, Color symColor) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(10,8,10,8));
            JLabel lbl = makeLabel(labelText, 9, Font.PLAIN, COL_DIM);
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            numLabel = makeLabel("0", 22, Font.BOLD, Color.WHITE);
            numLabel.setAlignmentX(CENTER_ALIGNMENT);
            add(lbl); add(gap(2)); add(numLabel);
        }
        void setActive(boolean a) { active=a; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = active ? new Color(255,215,0,30) : new Color(255,255,255,18);
            Color bd = active ? new Color(255,215,0,120) : new Color(255,255,255,45);
            g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
            g2.setColor(bd); g2.setStroke(new BasicStroke(active?1.5f:0.5f));
            g2.draw(new RoundRectangle2D.Float(1,1,getWidth()-2,getHeight()-2,14,14));
            super.paintComponent(g);
        }
    }

  
    class TurnBar extends JPanel {
        TurnBar() { setOpaque(false); setLayout(new FlowLayout(FlowLayout.CENTER,8,6)); setMaximumSize(new Dimension(400,44)); }
        void setLabels(JLabel sym, JLabel name) {
            JLabel lbl = makeLabel("Turn:", 11, Font.PLAIN, COL_DIM);
            add(lbl); add(sym); add(name);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,18));
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),22,22));
            g2.setColor(new Color(255,255,255,40)); g2.setStroke(new BasicStroke(0.5f));
            g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,22,22));
            super.paintComponent(g);
        }
    }

    
    class CellButton extends JButton {
        private String val = "";
        private boolean win = false;
        private boolean hover = false;
        private float animScale = 1f;
        private Timer popTimer;

        CellButton() {
            setPreferredSize(new Dimension(90, 90));
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            });
        }

        void setValue(String v) {
            val = v == null ? "" : v;
            if (!val.isEmpty()) {
                animScale = 0f;
                if (popTimer != null) popTimer.stop();
                popTimer = new Timer(16, e -> {
                    animScale = Math.min(1f, animScale + 0.12f);
                    repaint();
                    if (animScale >= 1f) ((Timer)e.getSource()).stop();
                });
                popTimer.start();
            }
            repaint();
        }

        void setWin(boolean w) { win=w; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            Color bg = win ? COL_WIN : hover && val.isEmpty() ? COL_CELL_H : COL_CELL;
            Color bd = win ? new Color(255,215,0,140) : new Color(255,255,255,40);
            g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,w,h,18,18));
            g2.setColor(bd); g2.setStroke(new BasicStroke(win?1.5f:0.5f));
            g2.draw(new RoundRectangle2D.Float(1,1,w-2,h-2,18,18));

            if (!val.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 38));
                g2.setColor(val.equals("X") ? COL_X : COL_O);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(val))/2;
                int ty = (h + fm.getAscent() - fm.getDescent())/2;
                // pop animation
                AffineTransform old = g2.getTransform();
                g2.translate(w/2.0, h/2.0);
                g2.scale(animScale, animScale);
                g2.translate(-w/2.0, -h/2.0);
                g2.drawString(val, tx, ty);
                g2.setTransform(old);
            }
        }
    }

    class RoundButton extends JButton {
        private Color bg = COL_PURPLE, hoverBg;
        RoundButton(String text) {
            super(text);
            hoverBg = COL_PURPLE.brighter();
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(150,40));
        }
        void setColors(Color normal, Color hover) { bg=normal; hoverBg=hover; }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = getModel().isRollover() ? hoverBg : bg;
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
            super.paintComponent(g);
        }
    }

   
    class Particle {
        double x,y,vx,vy,r,alpha;
        Color color;
        Particle() { reset(WINDOW_W, WINDOW_H); }
        void reset(int w,int h){
            x=rng.nextInt(w); y=rng.nextInt(h);
            vx=(rng.nextDouble()-.5)*.4; vy=(rng.nextDouble()-.5)*.4;
            r=rng.nextDouble()*2+0.8; alpha=rng.nextDouble()*.4+.15;
            color = rng.nextBoolean()
                ? new Color(102,126,234,(int)(alpha*255))
                : new Color(255,107,157,(int)(alpha*255));
        }
        void update(int w,int h){
            x+=vx; y+=vy;
            if(x<0)x=w; if(x>w)x=0; if(y<0)y=h; if(y>h)y=0;
        }
    }

   
    class Confetti {
        double x,y,vx,vy,r,rot,rotV,life;
        Color color; boolean isRect;
        static final Color[] COLORS = {
            new Color(255,107,157), new Color(103,212,247),
            new Color(255,215,0),   new Color(102,126,234),
            new Color(168,255,120), new Color(255,154,86)
        };
        Confetti(int cx,int cy){
            x=cx; y=cy;
            vx=(rng.nextDouble()-.5)*9; vy=(rng.nextDouble()-2.8)*5;
            r=rng.nextDouble()*6+2; rot=rng.nextDouble()*360;
            rotV=(rng.nextDouble()-.5)*10; life=1;
            color=COLORS[rng.nextInt(COLORS.length)]; isRect=rng.nextBoolean();
        }
        void update(){ x+=vx; y+=vy; vy+=.14; rot+=rotV; life-=.011; }
    }

      public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(TicTacToe::new);
    }
}