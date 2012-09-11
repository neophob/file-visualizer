package com.neophob.fwimage.helper;

public class Layer {

	/**
	 * buffer: 1 pixel is represented as 4x short (argb) 
	 */
	private int[] buffer;
	private int x;
	private int y;

	public Layer(int x, int y) {
		this.x = x;
		//add one line, because of the division 
		this.y = y+1;
		buffer = new int[this.x*this.y];		
	}

	public int[] getBuffer() {
		return buffer;
	}

	public void setBuffer(int[] buffer) {
		this.buffer = buffer;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	
} 