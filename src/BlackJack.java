import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.imageio.ImageIO;

public class BlackJack {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("JQK".contains(value)) {
                return 10;
            }
            if ("A".equals(value)) {
                return 11;
            }
            return Integer.parseInt(value); //2-10
        }

        public boolean isAce() {
            return "A".equals(value);
        }

        public String getImagePath() {
            int rankIndex;
            switch (value) {
                case "A": rankIndex = 0; break;
                case "J": rankIndex = 10; break;
                case "Q": rankIndex = 11; break;
                case "K": rankIndex = 12; break;
                default: rankIndex = Integer.parseInt(value) - 1; break;
            }

            int fileIndex;
            switch (type) {
                case "H": fileIndex = rankIndex + 1; break;   // hearts
                case "S": fileIndex = 15 + rankIndex; break;  // spades
                case "D": fileIndex = 29 + rankIndex; break;  // diamonds
                case "C": fileIndex = 43 + rankIndex; break;  // clubs
                default: fileIndex = 0; break;
            }

            return "resources/images/" + String.format("%02d_kerenel_Cards.png", fileIndex);
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); //shuffle deck

    //dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;

    //window
    int boardWidth = 608;
    int boardHeight = 672;

    int cardWidth = 96;
    int cardHeight = 134;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                //draw hidden card
                Image hiddenCardImage = loadImage("resources/images/28_kerenel_Cards.png");
                if (!stayButton.isEnabled()) {
                    hiddenCardImage = loadImage(hiddenCard.getImagePath());
                }
                if (hiddenCardImage != null) {
                    g.drawImage(hiddenCardImage, 20, 20, cardWidth, cardHeight, null);
                }

                //draw dealer's hand 
                for (int  i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = loadImage(card.getImagePath());
                    if (cardImage != null) {
                        g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                    }
                }

                //draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImage = loadImage(card.getImagePath());
                    if (cardImage != null) {
                        g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                    }
                }

                if (!stayButton.isEnabled()) {
                    int finalDealerSum = reduceDealerAce();
                    int finalPlayerSum = reducePlayerAce();
                    System.out.println("STAY:");
                    System.out.println(finalDealerSum);
                    System.out.println(finalPlayerSum);

                    String message = "";
                    if  (finalPlayerSum > 21) {
                        message = "You Lose!";
                    } else if (finalDealerSum > 21) {
                        message = "You Win!";
                    } 
                    // both you and dealer <= 21
                    else if (finalPlayerSum == finalDealerSum) {
                        message = "Tie!";
                    } else if (finalPlayerSum > finalDealerSum) {
                        message = "You Win!";
                    } else if (finalPlayerSum < finalDealerSum) {
                        message = "You Lose!";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");

    BlackJack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(34, 139, 34));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce()? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J
                    hitButton.setEnabled(false);
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (reduceDealerAce() < 17) {
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce()? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {
        //deck
        buildDeck();
        shuffleDeck();

        //dealer
        dealerHand = new  ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount= 0;

        hiddenCard = deck.remove(deck.size()-1); //remove card at last index
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        System.out.println("DEALER:");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("PLAYER:");
        System.out.println(playerHand);
        System.out.println(playerHand);
        System.out.println(playerAceCount);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }

    private Image loadImage(String path) {
        try {
            // try file system first
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (Exception e) {
            // ignore and try resource
        }

        try {
            java.net.URL url = getClass().getResource("/" + path);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int reducePlayerAce() {
        int sum = playerSum;
        int aces = playerAceCount;
        while (sum > 21 && aces > 0) {
            sum -= 10;
            aces -= 1;
        }
        return sum;
    }

    public int reduceDealerAce() {
        int sum = dealerSum;
        int aces = dealerAceCount;
        while (sum > 21 && aces > 0) {
            sum -= 10;
            aces -= 1;
        }
        return sum;
    }
}