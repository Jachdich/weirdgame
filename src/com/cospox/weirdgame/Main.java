package com.cospox.weirdgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;
import fisica.*;

public class Main extends PApplet{

    public static void main(String[] args) {
        PApplet.main("com.cospox.weirdgame.Main");
    }

    class Obstical extends FBox {
      int initialX, initialY;
      Obstical(int w, int h, int initialX_, int initialY_) {
        super(w, h);
        this.initialX = initialX_;
        this.initialY = initialY_;
      }
    }

    class Game {
      FWorld world;
      FBox player;
      ArrayList<Obstical> obsticals;
      PVector offset = new PVector(0, 0);
      BufferedReader reader;
      boolean[] keysPressed = new boolean[4];
      boolean   lastMousePressed = false;
      Game(PApplet app) {
        Fisica.init(app);
        this.world = new FWorld();
        this.player = new FBox(20, 20);
        this.player.setPosition(20, 20);
        this.world.add(this.player);
        this.world.setEdges();
        this.loadWorld();
      }

      void error(String msg) {
        println(msg);
      }
      
      void loadWorld() {
        this.reader = createReader("level.map");
        String testfile = "";
        String line = "";
        try {
          line = reader.readLine();
        } 
        catch (IOException e) {
          this.error("Error: Game files are missing");
          line = null;
        } catch (NullPointerException e) {
        	this.error("Error: Game files are missing");
        	line = null;
        }

        while (line != null) {
          testfile += line;
          try {
            line = reader.readLine();
          } 
          catch (IOException e) {
            this.error("Error: Game files are missing");
            line = null;
          }
        }

        int num = Integer.parseInt(testfile.split(":")[0]);
        String cleanfile = testfile.split(":")[1].replace(")", "");
        String[] objects = cleanfile.split("OBJ\\(");
        int[][] data = new int[num][4];
        obsticals = new ArrayList<Obstical>();
        int index = 0;
        for (int i = 1; i < num; i++) {
          String obj = objects[i];
          data[index] = this.parseCSV(obj);
          index++;
        }

        for (int i = 0; i < num + 1; i++) {
          if (i == 0) {
            this.initBox(10000, 20, -100, 500, i);
          } else {
            int[] obj = data[i - 1];
            this.initBox(obj[2], obj[3], obj[0], obj[1], i);
          }
        }
      }

      void initBox(int w, int h, int x, int y, int i) {
        this.obsticals.add(new Obstical(w, h, x, y));
        this.obsticals.get(i).setPosition(this.obsticals.get(i).initialX, this.obsticals.get(i).initialY);
        this.obsticals.get(i).setStatic(true);
        this.world.add(this.obsticals.get(i));
      }

      int[] parseCSV(String data) {
        String cleandata = data.replace("=", "").replace("x", "");
        cleandata = cleandata.replace("y", "").replace("w", "").replace("h", "");
        String[] numbers = cleandata.replace(" ", "").split(",");
        int[] result = new int[4];
        //println(cleandata);
        //println(numbers);
        for (int i = 0; i < 4; i++) {
          result[i] = Integer.parseInt(numbers[i]);
        }
        return result;
      }

      void xOffsetCalculations() {
        if (player.getX() > width - (width / 3)) {
          float off = width - (width / 3) - this.player.getX();
          this.player.setPosition(width - (width / 3), this.player.getY());
          this.offset.x += off;
        }
        if (player.getX() < width / 5) {
          float off = width / 5 - this.player.getX();
          this.player.setPosition(width / 5, this.player.getY());
          this.offset.x += off;
        }
      }

      void yOffsetCalculations() {
        if (player.getY() < height / 4) {
          float off = height / 4 - this.player.getY();
          this.player.setPosition(this.player.getX(), height / 4);
          this.offset.y += off;
        }
        if (player.getY() > height - (height / 4)) {
          float off = height - (height / 4) - this.player.getY();
          this.player.setPosition(this.player.getX(), height - (height / 4));
          this.offset.y += off;
        }
      }

      void draw() {
        background(255);
        fill(255, 0, 0);
        text(frameRate, 20, 20);
        text(getPlayerPos(), 20, 40);

        this.xOffsetCalculations();
        this.yOffsetCalculations();

        for (int i = 0; i < this.obsticals.size() - 1; i++) {
          int x = this.obsticals.get(i).initialX;
          int y = this.obsticals.get(i).initialY;
          this.obsticals.get(i).setPosition(x + this.offset.x, y + this.offset.y);
        }

        this.eventHandler();
        this.world.step();
        this.world.draw();
      }

      void eventHandler() {
    	if (mousePressed && !(this.lastMousePressed)) {
    		int x = mouseX - this.offset.x;
    		int y = mouseY - this.offset.y;
    		this.initBox(100, 10, x, y, this.obsticals.size());
    	}
    	this.lastMousePressed = mousePressed;
    	
    	if (!keyPressed) { return; }
        if (key == 'd') { //d
          this.player.addForce(200, 0);
        } 
        if (key == 'a') { //a
          this.player.addForce(-200, 0);
        } 
        if (key == 's') { //s
          this.player.addForce(0, 200);
        } 
        if (key == 'w') { //w
          if (contacting(this.player)) {
            this.player.addImpulse(0, -150);
          }
        }
      }
      
      String getPlayerPos() {
        int x = (int)(this.offset.x - this.player.getX());
        int y = (int)(this.offset.y - this.player.getY());
        return "X: " + str(x) + " Y: " + str(y);
      }

      boolean contacting(FBox a) {
        if (a.getTouching().size() > 0) {
          return true;
        }
        return false;
      }

      void keyPressedEvent(char k) {
        if (k == 'f') { this.loadWorld(); }
        if (k == 'd') { this.keysPressed[0] = true; }
        if (k == 'a') { this.keysPressed[1] = true; }
        if (k == 's') { this.keysPressed[2] = true; }
        if (k == 'w') { this.keysPressed[3] = true; }
      }

      void keyReleasedEvent(char k) {
        if (k == 'd') { this.keysPressed[0] = false; }
        if (k == 'a') { this.keysPressed[1] = false; }
        if (k == 's') { this.keysPressed[2] = false; }
        if (k == 'w') { this.keysPressed[3] = false; }
      }
    }

    Game game;
    
    @Override
	public void settings() {
    	fullScreen();
    }

    @Override
	public void setup() {
      //size(400, 400);
      //fullScreen(P2D);
      game = new Game(this);
    }

    @Override
	public void draw() {
      game.draw();
    }

    public void keyReleased(char key) {
      game.keyReleasedEvent(key);
    }

    public void keyPressed(char key) {
      game.keyPressedEvent(key);
    }

}
