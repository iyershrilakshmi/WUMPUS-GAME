import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements KeyListener {
    private Image backgroundImage;
    private Image caveBackgroundImage;
    private Image bottomlessPitImage;
    private Image batsImage;
    private Image wumpusImage;
    private Image endGameImage;
    private Image batHazardImage;
    private Image pitHazardImage;
    private Image wumpusHazardImage;
    private BufferedImage playerFrontImage;
    private BufferedImage playerBackImage;
    private BufferedImage playerLeftImage;
    private BufferedImage playerRightImage;
    private BufferedImage currentPlayerImage;
    private BufferedImage objectImage;
    private BufferedImage platformImage;
    private BufferedImage coinImage;
    private BufferedImage wonGameImage;
    private int playerX;
    private int playerY;
    private int initialPlayerY;
    private int offsetX;
    private int offsetY;
    private final int playerWidth = 50;
    private final int playerHeight = 50;
    private final int objectWidth = 50;
    private final int objectHeight = 50;
    private final int playerStep = 10;
    private final int backgroundStep = 20;
    private final int verticalStep = 5;
    private final int jumpHeight = 120;
    private List<int[]> objectPositions;
    private List<int[]> platformPositions;
    private List<Integer> coinCounts;
    private boolean isDialogShowing = false;
    private boolean isPlayerNearObject = false;
    private int nearbyObjectIndex = -1;
    private boolean isCaveBackground = false;
    private boolean isJumping = false;
    private int jumpStartY;
    private int jumpSpeed = 5;
    private boolean isFalling = false;
    private int fallSpeed = 1;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private Timer jumpTimer;
    private Chatbox chatbox;
    private Cave cave;
    private Timer platformTimer;
    private int movingPlatformIndex;
    private int movingPlatformDirection = 1;
    private int platformBaseY;
    private JButton arrowButton;
    private JButton shootArrowButton;
    private JButton coinsButton;
    private int arrowCount = 1;
    private int coinCount = 0;
    private int enteredRoomNumber = -1;
    private boolean canEnterRoomInCave = false;
    private int triviaCorrectAnswers = 0;
    private Set<Integer> bottomlessPits;
    private Set<Integer> superBats;
    private int wumpusRoom;
    private boolean showPitMessage = false;
    private boolean showBatMessage = false;
    private boolean showWumpusMessage = false;
    private boolean showEndGame = false;
    private boolean batEncounter = false;
    private JPanel imagePanel;
    private List<Integer> enteredRoomNumbers; // List to store entered room numbers
    private boolean isBlankScreen = false;
    private boolean isTransitioningFromHazard = false;
    private String hazardType = "";
    private boolean isTriviaForArrows = false; // Flag to check if trivia is for purchasing arrows

    public GamePanel() {
        cave = new Cave();
        cave.readCaveData("cave_data.txt");

        try {
            loadImages();
            scaleImages();

            objectPositions = new ArrayList<>();
            platformPositions = new ArrayList<>();
            coinCounts = new ArrayList<>();
            enteredRoomNumbers = new ArrayList<>(); // Initialize the list

            initializeObjectPositions();
            initializePlatforms();

            movingPlatformIndex = new Random().nextInt(platformPositions.size());
            platformBaseY = platformPositions.get(movingPlatformIndex)[1];

            startPlatformTimer();

            initializeHazards();

        } catch (IOException e) {
            e.printStackTrace();
        }

        setFocusable(true);
        addKeyListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initializePlayerPosition();
            }
        });

        chatbox = new Chatbox();
        setLayout(null);
        chatbox.setBounds(50, getHeight() - 150, getWidth() - 100, 100);
        chatbox.setVisible(false);
        add(chatbox);

        arrowButton = new JButton("Shoot an Arrow: " + arrowCount);
        styleButton(arrowButton);
        arrowButton.setVisible(false);
        arrowButton.addActionListener(e -> {
            if (arrowCount > 0) {
                arrowCount--; // Decrease arrow count when shooting
                arrowButton.setText("Shoot an Arrow: " + arrowCount);
                showArrowInputDialog();
            } else {
                JOptionPane.showMessageDialog(this, "No arrows left!");
            }
            requestFocusInWindow(); // Return focus to the game panel
        });
        add(arrowButton);

        shootArrowButton = new JButton("Purchase Arrows");
        styleButton(shootArrowButton);
        shootArrowButton.setVisible(false);
        shootArrowButton.addActionListener(e -> {
            if (coinCount >= 12) {
                coinCount -= 12;  // Deduct 12 coins
                coinsButton.setText("Coins: " + coinCount);  // Update the button label
                isTriviaForArrows = true; // Set the flag to true when purchasing arrows
                // Open the Trivia GUI
                SwingUtilities.invokeLater(() -> {
                    TriviaGUI triviaGUI = new TriviaGUI("trivia_questions.txt", this);
                    triviaGUI.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "You need at least 12 coins to purchase arrows!");
            }
            requestFocusInWindow(); // Return focus to the game panel
        });
        add(shootArrowButton);

        coinsButton = new JButton("Coins: " + coinCount);
        styleButton(coinsButton);
        coinsButton.setVisible(false);
        add(coinsButton);

        // Start the gravity timer
        Timer gravityTimer = new Timer(30, e -> applyGravity());
        gravityTimer.start();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(255, 204, 102));
        button.setForeground(Color.WHITE);
        button.setBorder(new LineBorder(new Color(255, 153, 51), 2, true));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
    }

    private void loadImages() throws IOException {
        backgroundImage = ImageIO.read(new File("image1.png"));
        caveBackgroundImage = ImageIO.read(new File("cave.png"));
        bottomlessPitImage = ImageIO.read(new File("bottomlesspit.png"));
        batsImage = ImageIO.read(new File("bats.png"));
        wumpusImage = ImageIO.read(new File("wumpus.png"));
        endGameImage = ImageIO.read(new File("endgame.png"));
        wonGameImage = ImageIO.read(new File("wongame.png"));
        batHazardImage = ImageIO.read(new File("bat_hazard.png"));
        pitHazardImage = ImageIO.read(new File("pit_hazard.png"));
        wumpusHazardImage = ImageIO.read(new File("wumpus_hazard.png"));
        playerFrontImage = ImageIO.read(new File("playerfrontside.png"));
        playerBackImage = ImageIO.read(new File("playerbackside.png"));
        playerLeftImage = ImageIO.read(new File("playerleftside.png"));
        playerRightImage = ImageIO.read(new File("playerrightside.png"));
        objectImage = ImageIO.read(new File("image2.png"));
        platformImage = ImageIO.read(new File("platform.png"));
        coinImage = ImageIO.read(new File("coin.png"));
    }

    private void scaleImages() {
        playerFrontImage = scaleImage(playerFrontImage, playerWidth, playerHeight);
        playerBackImage = scaleImage(playerBackImage, playerWidth, playerHeight);
        playerLeftImage = scaleImage(playerLeftImage, playerWidth, playerHeight);
        playerRightImage = scaleImage(playerRightImage, playerWidth, playerHeight);
        objectImage = scaleImage(objectImage, objectWidth, objectHeight);
        platformImage = scaleImage(platformImage, 80, 80);
        coinImage = scaleImage(coinImage, 40, 40);
        currentPlayerImage = playerFrontImage;
    }

    private BufferedImage scaleImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return scaledImage;
    }

    private void initializePlayerPosition() {
        playerX = getWidth() / 2 - playerWidth / 2;
        playerY = getHeight() / 2 - playerHeight / 2;
        initialPlayerY = playerY;

        offsetX = (backgroundImage.getWidth(this) - getWidth()) / 2;
        offsetY = (backgroundImage.getHeight(this) - getHeight()) / 2;

        updateChatboxBounds();

        repaint();
    }

    private void updateChatboxBounds() {
        chatbox.setBounds(50, getHeight() - 150, getWidth() - 100, 100);
        chatbox.revalidate();
        chatbox.repaint();
    }

    private void initializeObjectPositions() {
        int cols = 6;
        int rows = 5;

        int hSpacing = (backgroundImage.getWidth(this) - objectWidth) / (cols - 1);
        int vSpacing = (backgroundImage.getHeight(this) - objectHeight) / (rows - 1);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * hSpacing;
                int y = row * vSpacing;
                objectPositions.add(new int[]{x, y});
            }
        }
    }

    private void initializePlatforms() {
        int playerInitialX = getWidth() / 2 - playerWidth / 2;
        int playerInitialY = getHeight() / 2 - playerHeight / 2;
        platformBaseY = caveBackgroundImage.getHeight(this) * 4 - getHeight() - 150;

        Random random = new Random();

        platformPositions.clear();
        coinCounts.clear();

        platformPositions.add(new int[]{900 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        platformPositions.add(new int[]{1000 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        platformPositions.add(new int[]{500 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        platformPositions.add(new int[]{200 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        platformPositions.add(new int[]{300 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        platformPositions.add(new int[]{700 + (int)(Math.random() * 400), platformBaseY - ((int)(Math.random() * 200) - 400)});
        coinCounts.add(random.nextInt(2) + 1);

        objectPositions.add(new int[]{1600, 1280});
    }

    private void startPlatformTimer() {
        platformTimer = new Timer(500, e -> {
            int[] pos = platformPositions.get(movingPlatformIndex);
            pos[1] += movingPlatformDirection * 150;
            if (pos[1] >= platformBaseY + 2 || pos[1] <= platformBaseY - 2) {
                movingPlatformDirection *= -1;
            }
            repaint();
        });
        platformTimer.start();
    }

    private void applyGravity() {
        if (isCaveBackground && !isJumping && !isFalling && !isOnPlatform() && !allCoinsCollected()) {
            startFalling();
        }
    }

    private boolean isOnPlatform() {
        for (int[] pos : platformPositions) {
            int platformX = pos[0] - offsetX;
            int platformY = pos[1] - offsetY;
            int platformWidth = 80;
            int platformHeight = 80;
            if (playerX + playerWidth > platformX &&
                    playerX < platformX + platformWidth &&
                    playerY + playerHeight >= platformY &&
                    playerY + playerHeight <= platformY + platformHeight) {
                return true;
            }
        }
        return false;
    }

    private void startFalling() {
        isFalling = true;
        Timer fallTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFalling) {
                    playerY += fallSpeed;
                    if (playerY >= initialPlayerY) {
                        playerY = initialPlayerY;
                        isFalling = false;
                        ((Timer) e.getSource()).stop();
                    }
                }
                repaint();
            }
        });
        fallTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateChatboxBounds();

        if (isBlankScreen) {
            if ("bat".equals(hazardType)) {
                g.drawImage(batHazardImage, 0, 0, getWidth(), getHeight(), this);
                if (!isTransitioningFromHazard) {
                    isTransitioningFromHazard = true;
                    chatbox.setVisible(true);
                    chatbox.showMessage("Bats Ahead!");
                }
            } else if ("pit".equals(hazardType)) {
                g.drawImage(pitHazardImage, 0, 0, getWidth(), getHeight(), this);
                if (!isTransitioningFromHazard) {
                    isTransitioningFromHazard = true;
                    chatbox.setVisible(true);
                    chatbox.showMessage("Bottomless Pit Ahead!");
                }
            } else if ("wumpus".equals(hazardType)) {
                g.drawImage(wumpusHazardImage, 0, 0, getWidth(), getHeight(), this);
                if (!isTransitioningFromHazard) {
                    isTransitioningFromHazard = true;
                    chatbox.setVisible(true);
                    chatbox.showMessage("I smell a Wumpus!");
                }
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        } else {
            drawBackground(g);
            drawPlatforms(g);
            drawObjects(g);
            drawPlayer(g);
            drawEnteredRoomNumbers(g); // Draw the entered room numbers on the screen
            if (isCaveBackground) {
                updateButtonPositions();
                drawCaveObject(g);  // Draw the object in the cave background
            }
        }
    }

    private void drawBackground(Graphics g) {
        Image bgImage = isCaveBackground ? caveBackgroundImage : backgroundImage;
        if (showPitMessage) {
            bgImage = bottomlessPitImage;
        } else if (showBatMessage) {
            bgImage = batsImage;
        } else if (showWumpusMessage) {
            bgImage = wumpusImage;
        } else if (showEndGame) {
            bgImage = endGameImage;
        }
        int bgWidth = isCaveBackground ? caveBackgroundImage.getWidth(this) * 4 : backgroundImage.getWidth(this);
        int bgHeight = isCaveBackground ? caveBackgroundImage.getHeight(this) * 4 : backgroundImage.getHeight(this);
        g.drawImage(bgImage, -offsetX, -offsetY, bgWidth, bgHeight, this);
    }

    private void drawPlatforms(Graphics g) {
        for (int i = 0; i < platformPositions.size(); i++) {
            int[] pos = platformPositions.get(i);
            g.drawImage(platformImage, pos[0] - offsetX, pos[1] - offsetY, this);
            for (int j = 0; j < coinCounts.get(i); j++) {
                g.drawImage(coinImage, pos[0] - offsetX + 40 * j, pos[1] - offsetY - 40, this);
            }
        }
    }

    private void drawObjects(Graphics g) {
        if (objectImage != null) {
            for (int i = 0; i < objectPositions.size(); i++) {
                int[] pos = objectPositions.get(i);
                BufferedImage numberedObjectImage = addRoomNumberToImage(objectImage, i + 1);
                g.drawImage(numberedObjectImage, pos[0] - offsetX, pos[1] - offsetY, this);
            }
        }
    }

    private void drawCaveObject(Graphics g) {
        int x = 1600 - offsetX; // Adjust as per the position in the cave background
        int y = 1280 - offsetY; // Adjust as per the position in the cave background
        if (enteredRoomNumber > 0) {
            BufferedImage numberedObjectImage = addRoomNumberToImage(objectImage, enteredRoomNumber);
            g.drawImage(numberedObjectImage, x, y, this);
        } else {
            g.drawImage(objectImage, x, y, this); // Draw object without number
        }
    }

    private BufferedImage addRoomNumberToImage(BufferedImage image, int roomNumber) {
        BufferedImage numberedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = numberedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String roomNumberStr = String.valueOf(roomNumber);
        int textWidth = fm.stringWidth(roomNumberStr);
        int x = (image.getWidth() - textWidth) / 2;
        int y = (image.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(roomNumberStr, x, y);
        g2d.dispose();
        return numberedImage;
    }

    private void drawPlayer(Graphics g) {
        if (currentPlayerImage != null) {
            g.drawImage(currentPlayerImage, playerX, playerY, this);
        }
    }

    private void drawEnteredRoomNumbers(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        String roomNumbersStr = String.join(", ", enteredRoomNumbers.stream().map(String::valueOf).toArray(String[]::new));
        g.drawString("Entered Rooms: " + roomNumbersStr, 10, 20);
    }

    private void updateButtonPositions() {
        int buttonX = getWidth() - 170;
        int buttonY = 20;
        arrowButton.setBounds(buttonX, buttonY, 160, 30);
        shootArrowButton.setBounds(buttonX, buttonY + 40, 160, 30); // Increase the width to 160 to fit the text
        coinsButton.setBounds(buttonX, buttonY + 80, 160, 30); // Position the "Coins" button below the other buttons
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (isJumping && !isCaveBackground) {
            return;
        }

        if (showPitMessage || showBatMessage || showWumpusMessage || showEndGame) {
            if (key == KeyEvent.VK_ENTER) {
                if (showPitMessage) {
                    showPitMessage = false;
                    hideChatbox();
                    remove(imagePanel);
                    revalidate();
                    repaint();
                    isTriviaForArrows = false; // Ensure the flag is false when encountering a pit
                    openTriviaGUI();
                } else if (showBatMessage) {
                    showBatMessage = false;
                    hideChatbox();
                    remove(imagePanel);
                    revalidate();
                    repaint();
                    handleBatMessage();
                } else if (showWumpusMessage) {
                    showWumpusMessage = false;
                    hideChatbox();
                    remove(imagePanel);
                    revalidate();
                    repaint();
                    moveWumpus();
                } else if (showEndGame) {
                    System.exit(0);  // Close the game or restart
                }
            }
            return;
        }

        if (isTransitioningFromHazard) {
            if (key == KeyEvent.VK_ENTER) {
                isTransitioningFromHazard = false;
                isBlankScreen = false;
                remove(imagePanel);
                revalidate();
                repaint();
                chatbox.setVisible(false);  // Hide the chatbox after the transition
                transitionToCave();
            }
            return;
        }

        switch (key) {
            case KeyEvent.VK_UP -> handleUpKey();
            case KeyEvent.VK_DOWN -> movePlayerDown();
            case KeyEvent.VK_LEFT -> movePlayerLeft();
            case KeyEvent.VK_RIGHT -> movePlayerRight();
            case KeyEvent.VK_ENTER -> handleEnter();
        }

        if (!isCaveBackground) {
            checkForNearbyObjects();
        } else {
            checkForNearbyObjectsInCave();
        }

        checkForCoinCollisions();
        repaint();
    }

    private void handleUpKey() {
        if (isCaveBackground && allCoinsCollected()) {
            movePlayerUp();
        } else if (isCaveBackground) {
            handleJump();
        } else {
            currentPlayerImage = playerBackImage;
            if (playerY > getHeight() / 2 - playerHeight / 2) {
                playerY -= playerStep;
            } else if (offsetY > 0) {
                offsetY = Math.max(offsetY - backgroundStep, 0);
            } else {
                playerY = Math.max(playerY - playerStep, 0);
            }
        }
    }

    private boolean allCoinsCollected() {
        for (int count : coinCounts) {
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    private void movePlayerUp() {
        if (playerY > 0) {
            playerY -= verticalStep;
            if (offsetY > 0) {
                offsetY -= verticalStep;
            }
            repaint();
        } else if (offsetY > 0) {
            offsetY = Math.max(offsetY - backgroundStep, 0);
            repaint();
        }
    }

    private void handleJump() {
        if (isCaveBackground) {
            if (!isJumping && !isFalling) {
                isJumping = true;
                jumpStartY = playerY;
                jumpTimer = new Timer(20, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isJumping) {
                            if (playerY > jumpStartY - jumpHeight) {
                                playerY -= jumpSpeed;
                                if (isMovingLeft) {
                                    playerX -= playerStep;
                                }
                                if (isMovingRight) {
                                    playerX += playerStep;
                                }
                            } else {
                                isJumping = false;
                                isFalling = true;
                            }
                        } else if (isFalling) {
                            if (!checkPlatformCollision()) {
                                playerY += fallSpeed;
                                if (playerY >= initialPlayerY) {
                                    playerY = initialPlayerY;
                                    isFalling = false;
                                    jumpTimer.stop();
                                }
                            } else {
                                isFalling = false;
                                jumpTimer.stop();
                            }
                        }
                        repaint();
                    }
                });
                jumpTimer.start();
            }
        }
    }

    private void openTriviaGUI() {
        SwingUtilities.invokeLater(() -> {
            TriviaGUI triviaGUI = new TriviaGUI("trivia_questions.txt", this);
            triviaGUI.setVisible(true);
        });
    }

    private void movePlayerDown() {
        currentPlayerImage = playerFrontImage;
        if (playerY < getHeight() / 2 - playerHeight / 2) {
            playerY += playerStep;
        } else if (offsetY < (isCaveBackground ? caveBackgroundImage.getHeight(this) * 4 : backgroundImage.getHeight(this)) - getHeight()) {
            offsetY = Math.min(offsetY + backgroundStep, (isCaveBackground ? caveBackgroundImage.getHeight(this) * 4 : backgroundImage.getHeight(this)) - getHeight());
        } else {
            playerY = Math.min(playerY + playerStep, getHeight() - playerHeight);
        }
    }

    private void movePlayerLeft() {
        currentPlayerImage = playerLeftImage;
        isMovingLeft = true;
        if (playerX > getWidth() / 2 - playerWidth / 2) {
            playerX -= playerStep;
        } else if (offsetX > 0) {
            offsetX = Math.max(offsetX - backgroundStep, 0);
        } else {
            playerX = Math.max(playerX - playerStep, 0);
        }
    }

    private void movePlayerRight() {
        currentPlayerImage = playerRightImage;
        isMovingRight = true;
        if (playerX < getWidth() / 2 - playerWidth / 2) {
            playerX += playerStep;
        } else if (offsetX < (isCaveBackground ? caveBackgroundImage.getWidth(this) * 4 : backgroundImage.getWidth(this)) - getWidth()) {
            offsetX = Math.min(offsetX + backgroundStep, (isCaveBackground ? caveBackgroundImage.getWidth(this) * 4 : backgroundImage.getWidth(this)) - getWidth());
        } else {
            playerX = Math.min(playerX + playerStep, getWidth() - playerWidth);
        }
    }

    private void handleEnter() {
        if (isPlayerNearObject && !isCaveBackground) {
            isCaveBackground = true;
            objectPositions.clear();
            playerX = getWidth() / 2 - playerWidth / 2;
            playerY = getHeight() / 2 - playerHeight / 2;
            initialPlayerY = playerY;
            offsetX = 0;
            offsetY = caveBackgroundImage.getHeight(this) * 4 - getHeight();
            initializePlatforms();
            hideChatbox();
            arrowButton.setVisible(true);  // Show the button when transitioning to the cave background
            shootArrowButton.setVisible(true);  // Show the "Shoot an Arrow" button when transitioning to the cave background
            coinsButton.setVisible(true);  // Show the "Coins" button when transitioning to the cave background
            updateButtonPositions();
            repaint();
        } else if (isDialogShowing) {
            // Show input dialog for user to enter room number
            String input = JOptionPane.showInputDialog(this, "Enter room number:");
            if (input != null && !input.isEmpty()) {
                try {
                    int roomNumber = Integer.parseInt(input);
                    enteredRoomNumber = roomNumber;  // Store the entered room number
                    enteredRoomNumbers.add(roomNumber); // Add the entered room number to the list
                    repaint();  // Repaint to show the room number on the image2.png in the cave background
                    // Get connected rooms from the cave
                    ArrayList<Integer> connectedRooms = cave.getAdjacentRooms(roomNumber);
                    StringBuilder message = new StringBuilder("That room number is connected to rooms ");
                    for (int i = 0; i < connectedRooms.size(); i++) {
                        message.append(connectedRooms.get(i));
                        if (i < connectedRooms.size() - 1) {
                            message.append(", ");
                        }
                    }
                    message.append(".");
                    chatbox.showMessage(message.toString());
                    canEnterRoomInCave = true; // Allow entering the room in the cave
                    isDialogShowing = false;  // Set to false after processing input

                    checkForAdjacentHazards(connectedRooms); // Check for adjacent hazards

                } catch (NumberFormatException e) {
                    // Handle invalid input
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                }
            }
        } else if (canEnterRoomInCave) {
            if (bottomlessPits.contains(enteredRoomNumber)) {
                showPitMessage = true;
                showImage(bottomlessPitImage);
                chatbox.setVisible(true);
                chatbox.showMessage("You've encountered a pit. You must answer trivia questions to save yourself.");
            } else if (superBats.contains(enteredRoomNumber)) {
                showBatMessage = true;
                showImage(batsImage);
                chatbox.setVisible(true);
                chatbox.showMessage("Bats! You move to a new room.");
            } else if (wumpusRoom == enteredRoomNumber) {
                showWumpusMessage = true;
                showImage(wumpusImage);
                chatbox.setVisible(true);
                chatbox.showMessage("You've encountered the Wumpus!");
            } else {
                showBlackScreenAndReturnToCave();
            }
            canEnterRoomInCave = false; // Reset the flag
        }
    }

    private void checkForAdjacentHazards(ArrayList<Integer> connectedRooms) {
        for (int connectedRoom : connectedRooms) {
            if (bottomlessPits.contains(connectedRoom)) {
                hazardType = "pit";
                isBlankScreen = true;
                isTransitioningFromHazard = true;
                repaint();
                break;
            } else if (superBats.contains(connectedRoom)) {
                hazardType = "bat";
                isBlankScreen = true;
                isTransitioningFromHazard = true;
                repaint();
                break;
            } else if (wumpusRoom == connectedRoom) {
                hazardType = "wumpus";
                isBlankScreen = true;
                isTransitioningFromHazard = true;
                repaint();
                break;
            }
        }
    }

    private void showBlankScreenAndTransition() {
        // Determine the type of hazard
        hazardType = "";
        ArrayList<Integer> adjacentRooms = cave.getAdjacentRooms(enteredRoomNumber);
        for (int adjacentRoom : adjacentRooms) {
            if (bottomlessPits.contains(adjacentRoom)) {
                hazardType = "pit";
                break;
            } else if (superBats.contains(adjacentRoom)) {
                hazardType = "bat";
                break;
            } else if (wumpusRoom == adjacentRoom) {
                hazardType = "wumpus";
                break;
            }
        }

        isBlankScreen = true;
        isTransitioningFromHazard = true;
        repaint();
    }

    private void showImage(Image image) {
        // Show the specified image
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        imagePanel.setBounds(0, 0, getWidth(), getHeight());
        add(imagePanel);
        revalidate();
        repaint();
    }

    private void transitionToCave() {
        isCaveBackground = true;
        objectPositions.clear();
        initializePlatforms(); // Reinitialize platforms and coins
        playerX = getWidth() / 2 - playerWidth / 2;
        playerY = getHeight() / 2 - playerHeight / 2;
        initialPlayerY = playerY;
        offsetX = 0; // Reset offsetX to show bottom left corner
        offsetY = caveBackgroundImage.getHeight(this) * 4 - getHeight(); // Set offsetY to bottom left corner
        arrowButton.setVisible(true);
        shootArrowButton.setVisible(true);
        coinsButton.setVisible(true);
        updateButtonPositions();
        repaint();
    }

    public void returnFromTrivia(int correctAnswers) {
        triviaCorrectAnswers = correctAnswers;
        continueGameFromTrivia();
    }

    private void continueGameFromTrivia() {
        if (isTriviaForArrows && triviaCorrectAnswers >= 2) {
            arrowCount += 2;  // Increase arrow count by 2
            arrowButton.setText("Shoot an Arrow: " + arrowCount);  // Update the button text
        }

        if (triviaCorrectAnswers < 2 && !isTriviaForArrows) {
            showEndGame = true;
            showImage(endGameImage);
            chatbox.setVisible(true);
            chatbox.showMessage("Game over. Press Enter to exit.");
        } else {
            showBlackScreenAndReturnToCave();
        }

        isTriviaForArrows = false; // Reset the flag after processing
        requestFocusInWindow(); // Return focus to the game panel
        repaint();
    }

    private void showBlackScreenAndReturnToCave() {
        // Show a black screen for 1 second and then return to the cave background
        JPanel blackScreen = new JPanel();
        blackScreen.setBackground(Color.BLACK);
        blackScreen.setBounds(0, 0, getWidth(), getHeight());
        add(blackScreen);
        revalidate();
        repaint();

        Timer timer = new Timer(1000, e -> {
            remove(blackScreen);
            revalidate();
            repaint();
            // Zoom into the bottom left corner of the cave background
            zoomIntoBottomLeftCorner();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void zoomIntoBottomLeftCorner() {
        isCaveBackground = true;
        objectPositions.clear();
        initializePlatforms(); // Reinitialize platforms and coins
        playerX = getWidth() / 2 - playerWidth / 2;
        playerY = getHeight() / 2 - playerHeight / 2;
        initialPlayerY = playerY;
        offsetX = 0; // Reset offsetX to show bottom left corner
        offsetY = caveBackgroundImage.getHeight(this) * 4 - getHeight(); // Set offsetY to bottom left corner
        arrowButton.setVisible(true);
        shootArrowButton.setVisible(true);
        coinsButton.setVisible(true);
        updateButtonPositions();
        repaint();
        if (batEncounter) {
            showConnectedRoomsDialog(); // Show the connected rooms dialog after bats encounter
            batEncounter = false; // Reset batEncounter flag
        }
    }

    private void handleBatMessage() {
        // Change the room number on the object image to a random one
        int newRoomNumber = new Random().nextInt(30) + 1;
        while (newRoomNumber == enteredRoomNumber) {
            newRoomNumber = new Random().nextInt(30) + 1;
        }

        // Add both the current room number and the new room number to the list
        enteredRoomNumbers.add(enteredRoomNumber);
        enteredRoomNumbers.add(newRoomNumber);

        enteredRoomNumber = newRoomNumber;

        // Also change the bats room number to another random one
        int newBatsRoomNumber = new Random().nextInt(30) + 1;
        while (newBatsRoomNumber == enteredRoomNumber || superBats.contains(newBatsRoomNumber)) {
            newBatsRoomNumber = new Random().nextInt(30) + 1;
        }
        superBats.remove(enteredRoomNumber); // Remove the previous bats room number
        superBats.add(newBatsRoomNumber); // Add the new bats room number

        batEncounter = true; // Set the batEncounter flag
        showBlackScreenAndReturnToCave();
    }

    private void showConnectedRoomsDialog() {
        ArrayList<Integer> adjacentRooms = cave.getAdjacentRooms(enteredRoomNumber);
        StringBuilder message = new StringBuilder("This room number is connected to room numbers ");
        for (int i = 0; i < adjacentRooms.size(); i++) {
            message.append(adjacentRooms.get(i));
            if (i < adjacentRooms.size() - 1) {
                message.append(", ");
            }
        }
        message.append(".");
        chatbox.setVisible(true);
        chatbox.showMessage(message.toString());
    }

    private void moveWumpus() {
        int playerRoom = enteredRoomNumber;
        Random random = new Random();
        int newWumpusRoom;
        do {
            int roomsToMove = random.nextInt(3) + 2;
            newWumpusRoom = playerRoom + (random.nextBoolean() ? roomsToMove : -roomsToMove);
        } while (newWumpusRoom <= 0 || newWumpusRoom > 30 || newWumpusRoom == playerRoom);

        wumpusRoom = newWumpusRoom;
        showBlackScreenAndReturnToCave();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            isMovingLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            isMovingRight = false;
        }
        currentPlayerImage = playerFrontImage;
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private void checkForNearbyObjects() {
        boolean foundNearbyObject = false;
        for (int i = 0; i < objectPositions.size(); i++) {
            int[] pos = objectPositions.get(i);
            int objectX = pos[0] - offsetX;
            int objectY = pos[1] - offsetY;
            if (Math.abs(playerX - objectX) < playerWidth && Math.abs(playerY - objectY) < playerHeight) {
                foundNearbyObject = true;
                if (nearbyObjectIndex != i) {
                    nearbyObjectIndex = i;
                    showTypewriterDialog(i + 1);
                }
                break;
            }
        }
        isPlayerNearObject = foundNearbyObject;
        if (!foundNearbyObject) {
            nearbyObjectIndex = -1;
            hideChatbox();
        }
    }

    private void checkForNearbyObjectsInCave() {
        boolean foundNearbyObject = false;
        for (int i = 0; i < objectPositions.size(); i++) {
            int[] pos = objectPositions.get(i);
            int objectX = pos[0] - offsetX;
            int objectY = pos[1] - offsetY;
            if (Math.abs(playerX - objectX) < playerWidth && Math.abs(playerY - objectY) < playerHeight) {
                foundNearbyObject = true;
                if (nearbyObjectIndex != i) {
                    nearbyObjectIndex = i;
                    if (!batEncounter) {
                        showCaveDialog();
                    }
                }
                break;
            }
        }
        isPlayerNearObject = foundNearbyObject;
        if (!foundNearbyObject) {
            nearbyObjectIndex = -1;
            hideChatbox();
        }
    }

    private void checkForCoinCollisions() {
        for (int i = 0; i < platformPositions.size(); i++) {
            int[] pos = platformPositions.get(i);
            for (int j = 0; j < coinCounts.get(i); j++) {
                int coinX = pos[0] + 40 * j - offsetX;
                int coinY = pos[1] - 40 - offsetY;
                if (playerX + playerWidth > coinX && playerX < coinX + 40 &&
                        playerY + playerHeight > coinY && playerY < coinY + 40) {
                    coinCounts.set(i, coinCounts.get(i) - 1);
                    coinCount++;  // Increase the coin count when a coin is collected
                    coinsButton.setText("Coins: " + coinCount);  // Update the button label
                }
            }
        }
    }

    private void showTypewriterDialog(int roomNumber) {
        ArrayList<Integer> adjacentRooms = cave.getAdjacentRooms(roomNumber);
        StringBuilder message = new StringBuilder("This is room number " + roomNumber + ", and it is connected to room numbers ");
        for (int i = 0; i < adjacentRooms.size(); i++) {
            message.append(adjacentRooms.get(i));
            if (i < adjacentRooms.size() - 1) {
                message.append(", ");
            }
        }
        message.append(".");
        chatbox.setVisible(true);
        chatbox.showMessage(message.toString());
    }

    private void showCaveDialog() {
        String message = "Choose which room number you want to go to: ____";
        chatbox.setVisible(true);
        chatbox.showMessage(message);
        isDialogShowing = true;  // Set to true when showing the dialog
    }

    private void hideChatbox() {
        chatbox.setVisible(false);
        isDialogShowing = false;
        revalidate();
        repaint();
    }

    private boolean checkPlatformCollision() {
        boolean onPlatform = false;
        for (int[] pos : platformPositions) {
            int platformX = pos[0] - offsetX;
            int platformY = pos[1] - offsetY;
            int platformWidth = 80;
            int platformHeight = 80;
            if (playerX + playerWidth > platformX &&
                    playerX < platformX + platformWidth &&
                    playerY + playerHeight >= platformY &&
                    playerY + playerHeight <= platformY + platformHeight) {
                if (playerX + playerWidth > platformX && playerX < platformX + platformWidth / 2) {
                    playerY = platformY - playerHeight;
                    onPlatform = true;
                }
                break;
            }
        }
        if (!onPlatform && !isJumping) {
            startFalling();
        }
        return onPlatform;
    }

    private void checkPlatformRange() {
        boolean inPlatformRange = false;
        for (int[] pos : platformPositions) {
            int platformX = pos[0] - offsetX;
            int platformY = pos[1] - offsetY;
            int platformWidth = 80;
            int platformHeight = 80;
            if (playerX + playerWidth > platformX && playerX < platformX + platformWidth) {
                inPlatformRange = true;
                break;
            }
        }
        if (!inPlatformRange && !isJumping && !isFalling) {
            startFalling();
        }
    }

    private void initializeHazards() {
        Random random = new Random();
        bottomlessPits = new HashSet<>();
        superBats = new HashSet<>();

        while (bottomlessPits.size() < 2) {
            int pitRoom = random.nextInt(30) + 1;
            bottomlessPits.add(pitRoom);
        }

        while (superBats.size() < 2) {
            int batRoom = random.nextInt(30) + 1;
            if (!bottomlessPits.contains(batRoom)) {
                superBats.add(batRoom);
            }
        }

        wumpusRoom = random.nextInt(30) + 1;
    }

    private void showArrowInputDialog() {
        String input = JOptionPane.showInputDialog(this, "Choose which Room to shoot an Arrow into:");
        if (input != null && !input.isEmpty()) {
            try {
                int roomNumber = Integer.parseInt(input);
                if (roomNumber == wumpusRoom) {
                    showEndGame = true;
                    showImage(wonGameImage);
                    chatbox.setVisible(true);
                    chatbox.showMessage("You hit the Wumpus! You won! Press Enter to exit.");
                } else {
                    JOptionPane.showMessageDialog(this, "The Wumpus isn't in that room.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid room number.");
            }
        }
    }
}




