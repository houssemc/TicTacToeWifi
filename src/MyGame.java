package com.houssem.game;

import com.houssem.game.myPackage.myGameClass;
import com.badlogic.gdx.ApplicationAdapter;

public class MyGame extends ApplicationAdapter {

	myGameClass game;

	@Override
	public void create () {
		game = new myGameClass();
		game.start();
	}

	@Override
	public void render () {
		game.render();
	}

	@Override
	public void resize (int width, int height) {
		game.resize(width,height);
	}

	@Override
	public void dispose () {
		game.dispose();
	}
}
