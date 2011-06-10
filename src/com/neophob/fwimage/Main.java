package com.neophob.fwimage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.neophob.fwimage.helper.Layer;
import com.neophob.fwimage.helper.PngHelper;

public class Main {

	private static final String NAME = "\nfw image - binary visualizer v0.5 - michu@neophob.com - http://www.neophob.com";
	
	enum outputMode {
		BW,
		RGB
	}
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("fwimage if=IN_NAME of=OUT_FILE [x=SIZE] [mode=BW|RGB] [-nomarker] [-avg]");
		System.out.println("Parameter:");
		System.out.println("  if=INPUT_FILE");
		System.out.println("  of=OUTPUT_FILE, file format is .png");
		System.out.println("  x=X_SIZE OF IMAGE, default is 1024");
		System.out.println("  mode=BW|RGB, BW: 1:1 mapping, RGB: 1:3 mapping, default is BW");
		System.out.println("  -nomarker, disable marker after 1mb of data");
		System.out.println("  -avg, use average of INPUT_FILE as base");
		System.out.println("\nExample: fwimage if=myimage of=pic.png x=512 mode=RGB\n");
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Layer layer;
		String filenameIn = "";
		String filenameOut = "";
		outputMode om = outputMode.BW;
		int x=1024;
		int y;
		boolean showMarker=true;
		boolean useAvg=false;
		
		System.out.println(NAME);
		/* parse args */
		for (int i = 0; i < args.length; i++) {
			int loc = args[i].indexOf("=");
			String key = (loc > 0) ? args[i].substring(0, loc) : args[i];
			String value = (loc > 0) ? args[i].substring(loc+1) : "";
			
			/* strip trash characters */
			value=value.replace('\r', ' ').trim();
			key=key.replace('\r', ' ').trim();
			value=value.replace('\n', ' ').trim();
			key=key.replace('\n', ' ').trim();
			
			//System.out.println("_"+key+"_: _"+value+"-");
			if (key.equalsIgnoreCase("if")) {
				filenameIn = value;
			} else if (key.equalsIgnoreCase("of")) {
				filenameOut = value;
			} else if (key.equalsIgnoreCase("x")) {
				try { x = Integer.parseInt(value); } catch (Exception e) {e.printStackTrace();}
			} else if (key.equalsIgnoreCase("mode")) {
				if (outputMode.RGB.toString().equalsIgnoreCase(value)) 
					om = outputMode.RGB;
			} else if (key.equalsIgnoreCase("-nomarker")) {
				showMarker=false;
			} else if (key.equalsIgnoreCase("-avg")) {
				useAvg=true;
			}
		}
		
		if (filenameIn.isEmpty() || filenameOut.isEmpty()) {
			usage();
		}
		
		File fileIn = new File(filenameIn);
		FileInputStream fisIn = null;
		BufferedInputStream bisIn = null;
		DataInputStream disIn = null;
		
		//adjust Y size
		y = (int) (fileIn.length()/x);
		if (om==outputMode.RGB) {
			y/=3;			
		}
		
		if (y==0) {
			System.out.println("File is empty");
			System.exit(1);
		}
		
		System.out.print("alloc "+x+"*"+y+" ("+x*y+") bytes...");
		layer = new Layer(x, y);
		System.out.println(" done");
		
		try {
			fisIn = new FileInputStream(fileIn);
			bisIn = new BufferedInputStream(fisIn);
			disIn = new DataInputStream(bisIn);

			long l=0;
			if (om==outputMode.RGB) {
				l=processColor(layer, disIn);				
			} else {
				l=processBlackWhite(layer, disIn);
			}

			if (useAvg)
				processAvg(layer,l);

			if (showMarker) 
				addMarker(layer,om);
			
			PngHelper.saveit(filenameOut, layer);
			
			fisIn.close();
			bisIn.close();
			disIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param layer
	 * @param dis
	 * @throws Exception
	 */
	private static long processBlackWhite(Layer layer, DataInputStream dis) throws Exception {
		short i;
		int[] tmp = layer.getBuffer();
		int idx=0;
		
		while (dis.available() != 0) {
			i = (short) dis.read();			
			tmp[idx++] = (int)(i << 16) | (i << 8) | i;
		}
		System.out.println("processed "+idx+" bytes");	
		layer.setBuffer(tmp);
		return idx;
	}

	/**
	 * 
	 * @param layer
	 * @param dis
	 * @throws Exception
	 */
	private static long processColor(Layer layer, DataInputStream dis) throws Exception {
		int[] tmp = layer.getBuffer();
		int idx=0;
		short r,g,b;

		while (dis.available() != 0) {
			r = (short) dis.read();
			g = (short) dis.read();
			b = (short) dis.read();
			tmp[idx++] = (int)(r << 16) | (g << 8) | b;			
		}
		System.out.println("processed "+idx+" bytes");	
		layer.setBuffer(tmp);
		return idx;
	}

	/**
	 * 
	 * @param layer
	 * @param dis
	 * @throws Exception
	 */
	private static void processAvg(Layer layer, long l) throws Exception {
		int[] tmp = layer.getBuffer();
		short r,g,b,savg;
		int tmpAvg;
		float avg=0.0f;
		float f;
		
		for (int idx=0; idx<l; idx++) {
			tmpAvg=tmp[idx];
            r = (short) ((tmpAvg>>16) & 255);                                                           
            g = (short) ((tmpAvg>>8)  & 255);                                                           
            b = (short) ( tmpAvg      & 255);                                                           
            
            f=r+g+b;
            f/=3.0f;
            if (avg==0) {
            	avg=f;
            } else {
                avg=((avg+f)/2.0f);            	
            }
			idx++;
		}
		savg=(short)avg;
		System.out.println("avg is "+savg);
		
		for (int idx=0; idx<l; idx++) {
			tmpAvg=tmp[idx];
            r = (short) ((tmpAvg>>16) & 255);                                                           
            g = (short) ((tmpAvg>>8)  & 255);                                                           
            b = (short) ( tmpAvg      & 255);
            
            //System.out.format("%d %d %d (%d)\t",r,g,b, savg);
            r = (short) (r-savg);
            g = (short) (g-savg);
            b = (short) (b-savg);
            
            if (r<0) r=(short) (0-r);
            if (g<0) g=(short) (0-g);
            if (b<0) b=(short) (0-b);    
            //System.out.format("%d %d %d\n",r,g,b);
            
			tmp[idx] = (int)(r << 16) | (g << 8) | b;
		}	
		layer.setBuffer(tmp);

	}
	
	/**
	 * add a marker each mb
	 */
	private static void addMarker(Layer layer, outputMode om) throws Exception {
		System.out.print("add marker... ");
		int marker = 1024*1024;
		if (om==outputMode.RGB) {
			marker/=3;
		}
		int[] tmp = layer.getBuffer();
		int markerLength = layer.getX()*2;
		for (int idx=marker; idx<tmp.length-markerLength; idx+=marker) {
			for (int i=0; i<markerLength; i++) {
				tmp[idx+i] = (int)(255 << 16) | (0 << 8) | 0;
			}
		}
		layer.setBuffer(tmp);
		System.out.println("done!");
	}
}
