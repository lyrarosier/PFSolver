import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class PFSolver extends JFrame {

    private static final int SIZE = 6;

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton solveBtn, clearBtn;

    public PFSolver() {
        super("PFSolver - Puzzle Game");

        JLabel inputLabel = new JLabel("enter below:");
        inputLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        inputArea = new JTextArea(6, 20);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        JScrollPane inputScroll = new JScrollPane(inputArea);

        JLabel outputLabel = new JLabel("solution:");
        outputLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        outputArea = new JTextArea(6, 20);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        solveBtn = new JButton("solve");
        clearBtn = new JButton("clear");

        JPanel btnPanel = new JPanel();
        btnPanel.add(solveBtn);
        btnPanel.add(clearBtn);

        JPanel inputPanel = new JPanel(new BorderLayout(5,5));
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout(5,5));
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1,2,10,10));
        centerPanel.add(inputPanel);
        centerPanel.add(outputPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        solveBtn.addActionListener(e -> {
            outputArea.setText("thinking...");
            String inputText = inputArea.getText();

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                String result;
                @Override
                protected Void doInBackground() {
                    try {
                        Board board = Board.fromString(inputText);
                        if (board.isFullyValid()) {
                            result = "already valid:\n" + board.toString();
                        } else {
                            boolean solved = board.heuristicFix(100000000);
                            if (solved) {
                                result = board.toString();
                            } else {
                                result = "there is no solution.\nmake sure you entered the frogs correctly.";
                            }
                        }
                    } catch (Exception ex) {
                        result = "invalid input format. please enter exactly 6 lines of 6 characters each.\n" + ex.getMessage();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    outputArea.setText(result.trim());
                    outputArea.setCaretPosition(0);
                }
            };
            worker.execute();
        });

        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    static class Board {
        int size = SIZE;
        char[][] grid;
        Random rand = new Random();

        Board(char[][] grid) {
            this.grid = grid;
        }

        static Board fromString(String input) throws IOException {
            BufferedReader br = new BufferedReader(new StringReader(input.trim()));
            char[][] grid = new char[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                String line = br.readLine();
                if (line == null || line.length() != SIZE)
                    throw new IOException("line " + (i + 1) + " is invalid or missing.");
                grid[i] = line.toCharArray();
            }
            return new Board(grid);
        }

        boolean isFullyValid() {
            for (int r = 0; r < size; r++)
                for (int c = 0; c < size; c++)
                    if (!isInRunOfThree(r, c)) return false;
            return true;
        }

        boolean isInRunOfThree(int r, int c) {
            char ch = grid[r][c];
            if (ch == '-') return false;

            int count = 1;
            int i = c - 1;
            while (i >= 0 && grid[r][i] == ch) { count++; i--; }
            i = c + 1;
            while (i < size && grid[r][i] == ch) { count++; i++; }
            if (count >= 3) return true;

            count = 1;
            i = r - 1;
            while (i >= 0 && grid[i][c] == ch) { count++; i--; }
            i = r + 1;
            while (i < size && grid[i][c] == ch) { count++; i++; }
            return count >= 3;
        }

        int countInvalidTiles() {
            int count = 0;
            for (int r = 0; r < size; r++)
                for (int c = 0; c < size; c++)
                    if (!isInRunOfThree(r, c)) count++;
            return count;
        }

        boolean heuristicFix(int maxAttempts) {
            int currentInvalid = countInvalidTiles();
            if (currentInvalid == 0) return true;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int r1 = rand.nextInt(size), c1 = rand.nextInt(size);
                int r2 = rand.nextInt(size), c2 = rand.nextInt(size);
                while (r1 == r2 && c1 == c2) {
                    r2 = rand.nextInt(size);
                    c2 = rand.nextInt(size);
                }

                swap(r1, c1, r2, c2);

                int newInvalid = countInvalidTiles();
                if (newInvalid <= currentInvalid) {
    		    currentInvalid = newInvalid;
    		    if (currentInvalid == 0) return true;
		} else {
    		    // accept worse move with small probability to escape local minima
    		    double acceptanceProbability = 0.01; // 1% chance to accept worse move
    		    if (rand.nextDouble() < acceptanceProbability) {
        	        currentInvalid = newInvalid;
    		    } else {
                        swap(r1, c1, r2, c2); // revert swap
                    }
                }
            }
            return false;
        }

        void swap(int r1, int c1, int r2, int c2) {
            char temp = grid[r1][c1];
            grid[r1][c1] = grid[r2][c2];
            grid[r2][c2] = temp;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < size; r++) {
                sb.append(grid[r]);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PFSolver::new);
    }
}
