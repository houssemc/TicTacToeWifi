package com.houssem.game.myPackage;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Houssem on 4/4/2018.
 */

public class myGrid {

    private final int boxLength = 72;
    private final int bordMinX = 3;
    private final int bordMinY = 318;
    private final int LENGTH = 4;

    private enum mark {empty,x,o,X,O}
    private mark[][] grid;

    private SpriteBatch spriteBatch;
    private Sprite xSprite, oSprite;
    private Sprite XSprite, OSprite;

    public myGrid(SpriteBatch spriteBatch, Sprite xSprite, Sprite oSprite, Sprite XSprite, Sprite OSprite) {

        this.spriteBatch = spriteBatch;
        this.xSprite = xSprite;
        this.oSprite = oSprite;
        this.XSprite = XSprite;
        this.OSprite = OSprite;

        grid = new mark[15][15];
        reset();
    }

    public void draw() {
        for(int i=0;i<15;i++)
            for(int j=0;j<15;j++)
                switch(grid[i][j]) {
                    case o:
                        oSprite.setPosition(bordMinX+boxLength*i, bordMinY+boxLength*j);
                        oSprite.draw(spriteBatch);
                        break;
                    case x:
                        xSprite.setPosition(bordMinX+boxLength*i, bordMinY+boxLength*j);
                        xSprite.draw(spriteBatch);
                        break;
                    case O:
                        OSprite.setPosition(bordMinX+boxLength*i, bordMinY+boxLength*j);
                        OSprite.draw(spriteBatch);
                        break;
                    case X:
                        XSprite.setPosition(bordMinX+boxLength*i, bordMinY+boxLength*j);
                        XSprite.draw(spriteBatch);
                        break;
                }
    }

    public int getColFromX(int x) {
        return x/boxLength;
    }

    public int getRowFromY(int y) {
        return (y-bordMinY)/boxLength;
    }

    public boolean isValidMove(int x, int y) {
        if(x>=0&&x<15&&y>=0&&y<15&&grid[x][y]==mark.empty)
            return true;
        else return false;
    }

    public void update(int x, int y, String s) {
        if(s.equals("x")) grid[x][y]=mark.x;
        else if(s.equals("o")) grid[x][y]=mark.o;
    }

    public boolean isRoundEnded(int x, int y) {

        mark center = grid[x][y];

        // horizontal check

        int length = 1;
        int minX = x, maxX = x;
        while(length<LENGTH && minX-1>=0 && grid[minX-1][y]==center) {
            length++;
            minX--;
        }
        while(length<LENGTH && maxX+1<15 && grid[maxX+1][y]==center) {
            length++;
            maxX++;
        }
        if(length>=LENGTH) {
            for(int i=minX;i<=maxX;i++)
                grid[i][y]=center==mark.o?mark.O:mark.X;
            return true;
        }

        // vertical check

        length = 1;
        int minY = y, maxY = y;
        while(length<LENGTH && minY-1>=0 && grid[x][minY-1]==center) {
            length++;
            minY--;
        }
        while(length<LENGTH && maxY+1<15 && grid[x][maxY+1]==center) {
            length++;
            maxY++;
        }
        if(length>=LENGTH) {
            for(int j=minY;j<=maxY;j++)
                grid[x][j]=center==mark.o?mark.O:mark.X;
            return true;
        }

        // diagonal check 1

        length = 1;
        minX = maxX = x;
        minY = maxY = y;
        while(length<LENGTH
                && minX-1>=0 && minY-1>=0
                && grid[minX-1][minY-1]==center) {
            length++;
            minX--;
            minY--;
        }
        while(length<LENGTH
                && maxX+1<15 && maxY+1<15
                && grid[maxX+1][maxY+1]==center) {
            length++;
            maxX++;
            maxY++;
        }
        if(length>=LENGTH) {
            for(int i=minX,j=minY;i<=maxX;i++,j++)
                grid[i][j]=center==mark.o?mark.O:mark.X;
            return true;
        }

        // diagonal check 2

        length = 1;
        minX = maxX = x;
        minY = maxY = y;
        while(length<LENGTH
                && minX-1>=0 && maxY+1<15
                && grid[minX-1][maxY+1]==center) {
            length++;
            minX--;
            maxY++;
        }
        while(length<LENGTH
                && maxX+1<15 && minY-1>=0
                && grid[maxX+1][minY-1]==center) {
            length++;
            maxX++;
            minY--;
        }
        if(length>=LENGTH) {
            for(int i=minX,j=maxY;i<=maxX;i++,j--)
                grid[i][j]=center==mark.o?mark.O:mark.X;
            return true;
        }
        return false;
    }

    public void reset() {
        for(int i=0;i<15;i++)
            for(int j=0;j<15;j++)
                grid[i][j]=mark.empty;
    }

    public boolean isFull() {
        for(int i=0;i<15;i++)
            for(int j=0;j<15;j++)
                if(grid[i][j]==mark.empty)
                    return false;
        return true;
    }
}