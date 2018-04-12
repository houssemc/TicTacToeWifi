package com.houssem.game.myPackage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Houssem on 4/3/2018.
 */

public class myGameClass extends Thread {

    static final int WORLD_WIDTH = 1080;
    static final int WORLD_HEIGHT = 1920;

    OrthographicCamera camera;
    Viewport viewport;
    SpriteBatch spriteBatch;
    Sprite gameBoardSprite;
    Sprite createGameSprite;
    Sprite joinGameSprite;
    Sprite continueSprite;
    Sprite waitingSprite;
    Sprite xSprite,oSprite;
    Sprite XSprite,OSprite;
    BitmapFont scoreFont;
    BitmapFont textFont;
    GlyphLayout scoreLayout;
    Music sound1,sound2,sound3;

    enum GameState { loading, mainScreen, playing, roundEnd}
    GameState gameState = GameState.loading;
    myGrid grid;
    boolean myTurn = false;
    int score1=0,score2=0;

    enum PlayerType { client, server, notSelected}
    PlayerType playerType = PlayerType.notSelected;
    Socket socket;
    String myAdrress;
    final int PORT = 1234;

    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    int lastPlayedX,lastPlayedY;
    boolean quitingGame = false;
    String message = "";

     /*
     *  constructor
     */

    public myGameClass() {

        // setting game screen

        camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        camera.position.set(camera.viewportWidth/2,camera.viewportHeight/2,0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH,WORLD_HEIGHT,camera);
        spriteBatch = new SpriteBatch();

        // loading images

        gameBoardSprite = new Sprite(new Texture("images/board.png"));
        createGameSprite = new Sprite(new Texture("images/create.png"));
        joinGameSprite = new Sprite(new Texture("images/join.png"));
        continueSprite = new Sprite(new Texture("images/continue.png"));
        waitingSprite = new Sprite(new Texture("images/hourglass.png"));
        xSprite = new Sprite(new Texture("images/x.png"));
        oSprite = new Sprite(new Texture("images/o.png"));
        XSprite = new Sprite(new Texture("images/xx.png"));
        OSprite = new Sprite(new Texture("images/oo.png"));
        gameBoardSprite.setPosition(0,0);
        createGameSprite.setPosition(106,73);
        joinGameSprite.setPosition(702,66);
        continueSprite.setPosition(329,134);
        waitingSprite.setPosition(1009,1422);

        // loading fonts

        scoreFont = new BitmapFont(Gdx.files.internal("fonts/score.fnt"));
        scoreLayout = new GlyphLayout();
        textFont = new BitmapFont();

        // loading sounds

        sound1 = Gdx.audio.newMusic(Gdx.files.internal("sounds/sound1.wav"));
        sound2 = Gdx.audio.newMusic(Gdx.files.internal("sounds/sound2.wav"));
        sound3 = Gdx.audio.newMusic(Gdx.files.internal("sounds/sound3.wav"));

        // setting grid

        grid = new myGrid(spriteBatch,xSprite,oSprite,XSprite,OSprite);

        // this thread listens to user input
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown  (int x, int y, int pointer, int button) {
                processInput(x,y);
                return true;
            }
        });
    }

    public void  processInput(int x, int y) {

        if (playerType == PlayerType.notSelected) {
            if(createPressed(x,y)) {
                createGame();
            }

            else if (joinPressed(x,y)) {
                joinGame();
            }
        }

        else if (gameState == GameState.playing && myTurn) {
            lastPlayedX = grid.getColFromX(getXInGame(x));
            lastPlayedY = grid.getRowFromY(getYInGame(y));
            if(grid.isValidMove(lastPlayedX,lastPlayedY)) {
                myTurn = false;
                resumeGameThread();
            }
        }

        else if (gameState == GameState.roundEnd) {
            if(continuePressed(x,y))
                if(quitingGame) gameState = GameState.mainScreen;
                else gameState = GameState.playing;
                resumeGameThread();
        }

    }

    private void createGame() {

        myAdrress = getMyAddress();

        if(myAdrress.equals("localhost")) {
            message = "can't create game. please connect to public network.";
            return;
        }

        new Thread() {
            public void run() {
                if(playerType!=PlayerType.notSelected)return;
                try {
                    message = "waiting for player...";
                    ServerSocket serverSocket = new ServerSocket(PORT);
                    Socket _socket_ = serverSocket.accept();
                    if(playerType==PlayerType.notSelected) {
                        socket = _socket_;
                        message = "player connected.";
                        playerType = PlayerType.server;
                        resumeGameThread();
                    }
                } catch (IOException e) {}
            }
        }.start();

        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input (String text) {}
            @Override
            public void canceled () {}
        }, "Game created", "Your address is "+ myAdrress, "");
    }

    private void joinGame() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input (String text) {
                final String addr = text;
                if(addr.equals(myAdrress)||addr.equals(""))return;
                new Thread() {
                    public void run() {
                        try {
                            message = "connecting...";
                            Socket _socket_ = new Socket(addr,PORT);
                            if(playerType==PlayerType.notSelected) {
                                socket = _socket_;
                                message = "connected.";
                                playerType = PlayerType.client;
                                resumeGameThread();
                            }
                        } catch (IOException e) {
                            message = "connection failed.";
                        }
                    }
                }.start();
            }
            @Override
            public void canceled () {}
        }, "Enter server address", "", "");
    }

    private boolean continuePressed(int x, int y) {
        return getYInGame(y)<320;
    }

    private boolean joinPressed(int x, int y) {
        if(getYInGame(y)<320&&getXInGame(x)>WORLD_WIDTH/2)
            return true;
        else return false;
    }

    private boolean createPressed(int x, int y) {
        if(getYInGame(y)<320&&getXInGame(x)<WORLD_WIDTH/2)
            return true;
        else return false;
    }

    private int getXInGame(int x) {
        double worldWidthInScreen = Math.min(screenWidth,((WORLD_WIDTH*1.0/WORLD_HEIGHT)*screenHeight));
        double worldX0InScreen = screenWidth/2-worldWidthInScreen/2;
        double xCentred = x - worldX0InScreen;
        double xInGame = xCentred * (WORLD_WIDTH/worldWidthInScreen);
        return (int) xInGame;
    }

    private int getYInGame(int y) {
        double worldHeightInScreen = Math.min(screenHeight,((WORLD_HEIGHT*1.0/WORLD_WIDTH)*screenWidth));
        double worldY0InScreen = screenHeight/2+worldHeightInScreen/2;
        double yCentred = y - worldY0InScreen;
        double yInGame = yCentred * (WORLD_HEIGHT/worldHeightInScreen);
        return (int) -yInGame;
    }

    private String getMyAddress() {
        String address = "localhost";
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.isSiteLocalAddress())
                        address = i.getHostAddress();
                }
            }
        } catch (SocketException e) {} catch (IOException e) {}
        return address;
    }

    private void resumeGameThread() {
        message = "";
        synchronized(this) {
            notify();
        }
    }

    /*
     *  Render :
     *  this function displays the game based on current configuration
     */

    public void render () {

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        switch(gameState) {
            case loading:
                gameBoardSprite.draw(spriteBatch);
                message = "Loading...";
                break;
            case mainScreen:
                gameBoardSprite.draw(spriteBatch);
                drawScore();
                createGameSprite.draw(spriteBatch);
                joinGameSprite.draw(spriteBatch);
                break;
            case playing:
                gameBoardSprite.draw(spriteBatch);
                drawScore();
                grid.draw();
                if(!myTurn) waitingSprite.draw(spriteBatch);
                break;
            case roundEnd:
                gameBoardSprite.draw(spriteBatch);
                drawScore();
                grid.draw();
                continueSprite.draw(spriteBatch);
        }

        drawMessage();
        spriteBatch.end();
    }

    private void drawMessage() {
        textFont.getData().setScale(3);
        textFont.setColor(Color.WHITE);
        textFont.draw(spriteBatch,message,50,1450);
    }

    private void drawScore() {
        scoreLayout.setText(scoreFont,score1+"-"+score2);
        scoreFont.setColor(Color.WHITE);
        scoreFont.draw(spriteBatch, scoreLayout, 540-(scoreLayout.width/2), 1650);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    /*
     *  run:
     *  this is the main game thread
     *  it contains game logic
     */

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            // main screen

            sound1.play();
            gameState = GameState.mainScreen;
            message = "";

            // selecting client/server

            playerType = PlayerType.notSelected;
            pauseUntilPlayerTypeSelected();

            // start playing

            gameState = GameState.playing;
            quitingGame = false;
            score1=score2=0;
            grid.reset();
            message = "playing";

            // server always start first

            if(playerType == PlayerType.server) {
                myTurn=true;
                pauseUntilMyTurnPlayed();
                grid.update(lastPlayedX,lastPlayedY,"x");
                try {
                    sendMove();
                }catch(IOException e){}
            }

            while(!quitingGame) {

                // player2 turn

                try {
                    recieveMove();
                }catch(IOException e){
                    message = "player disconnected.";
                    quitingGame=true;
                    break;
                }
                grid.update(lastPlayedX,lastPlayedY,
                        playerType==PlayerType.server?"o":"x");

                if(grid.isRoundEnded(lastPlayedX,lastPlayedY)) {
                    gameState=GameState.roundEnd;
                    message = "you lost";
                    pauseUntilGameStateIsPlaying();
                    score2++;
                    grid.reset();
                }
                if(grid.isFull()) {
                    gameState=GameState.roundEnd;
                    message = "grid is full";
                    pauseUntilGameStateIsPlaying();
                    grid.reset();
                }

                // player1 turn

                myTurn=true;
                pauseUntilMyTurnPlayed();
                grid.update(lastPlayedX,lastPlayedY,
                        playerType==PlayerType.server?"x":"o");

                try {
                    sendMove();
                }catch(IOException e){
                    message = "player disconnected.";
                    quitingGame=true;
                    break;
                }

                if(grid.isRoundEnded(lastPlayedX,lastPlayedY)) {
                    gameState=GameState.roundEnd;
                    message = "you won";
                    pauseUntilGameStateIsPlaying();
                    score1++;
                    grid.reset();
                }
                if(grid.isFull()) {
                    gameState=GameState.roundEnd;
                    message = "grid is full";
                    pauseUntilGameStateIsPlaying();
                    grid.reset();
                }
            }

            sound2.play();
            gameState=GameState.roundEnd;
            pauseUntilGameStateIsMainScreen();
        }
    }

    private void pauseUntilGameStateIsMainScreen() {
        synchronized(this){try{
            while(gameState!=GameState.mainScreen) {
                wait();
            }
        }catch(InterruptedException e){}}
    }

    private void pauseUntilGameStateIsPlaying() {
        synchronized(this){try{
            while(gameState!=GameState.playing) {
                wait();
            }
        }catch(InterruptedException e){}}
    }

    private void pauseUntilMyTurnPlayed() {
        synchronized(this){try{
            while(myTurn!=false) {
                wait();
            }
        }catch(InterruptedException e){}}
    }

    private void pauseUntilPlayerTypeSelected() {
        playerType = PlayerType.notSelected;
        synchronized(this){try{
            while(playerType == PlayerType.notSelected) {
                wait();
            }
        }catch(InterruptedException e){}}
    }

    private void sendMove() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        out.println(lastPlayedX+"-"+lastPlayedY);
    }

    private void recieveMove() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String[] message = input.readLine().split("-");
        lastPlayedX=Integer.parseInt(message[0]);
        lastPlayedY=Integer.parseInt(message[1]);
    }

    /*
     *  destructor
     */

    public void dispose() {
        spriteBatch.dispose();
        scoreFont.dispose();
        textFont.dispose();
        sound1.dispose();
        sound2.dispose();
        sound3.dispose();
    }

}
