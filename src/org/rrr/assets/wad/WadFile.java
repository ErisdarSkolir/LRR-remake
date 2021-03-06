package org.rrr.assets.wad;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class WadFile {
	
	private RandomAccessFile raf;
	private String[] entries;
	private long[] fStart;
	private long[] fLength;
	
	public WadStream getStream(String path) {
		int index = -1;
		for(int i = 0; i < entries.length; i++)
			if(entries[i].equals(path))
				index = i;
		if(index == -1)
			return null;
		WadStream stream = new WadStream(raf, fStart[index], fLength[index]);
		return stream;
	}
	
	@SuppressWarnings("resource")
	public static WadFile getWadFile(File f) throws IOException {
		
		WadFile wad = new WadFile();
		
		RandomAccessFile in = new RandomAccessFile(f, "r");
		long pos = 0;
		
		byte[] buff = new byte[2048];
		in.read(buff, 0, 8); pos+=8;
		if(!new String(buff, 0, 4).equals("WWAD")) {
			System.err.println("Different wad file type! : " + new String(buff, 0, 4));
			in.close();
			return null;
		}
		
		int lentries = getIntLE(buff, 4);
		System.out.println(lentries);
		
		wad.entries = new String[lentries];
		wad.fStart = new long[lentries];
		wad.fLength = new long[lentries];
		
		int buffOffset = 0;
		in.read(buff, 0, 2048); pos+=2048;
		for(int i = 0; i < lentries; i++) {
			String name = "";
			for(int j = buffOffset; j < 2048; j++) {
				if(buff[j] == 0) {
					name += new String(buff, buffOffset, j-buffOffset);
					wad.entries[i] = name;
					buffOffset = j+1;
					break;
				}
				if(j == 2047) {
					name += new String(buff, buffOffset, 2048-buffOffset);
					in.read(buff, 0, 2048);  pos+=2048;
					buffOffset = 0;
					j = 0;
				}
			}
			
		}
		for(int i = 0; i < lentries; i++) {
			for(int j = buffOffset; j < 2048; j++) {
				if(buff[j] == 0) {
					buffOffset = j+1;
					break;
				}
				if(j == 2047) {
					in.read(buff, 0, 2048);  pos+=2048;
					buffOffset = 0;
					j = 0;
				}
			}
			
		}
		in.seek(pos-(2048-buffOffset));
		
		for(int i = 0; i < lentries; i++) {
			in.read(buff, 0, 16);
			wad.fLength[i] = getIntLE(buff, 8);
			wad.fStart[i] = getIntLE(buff, 12);
		}
		
		wad.raf = in;
		
		return wad;
		
	}
	
	public void close() throws IOException {
		raf.close();
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	public static void main(String[] args) {
		
		try {
			WadFile wad = WadFile.getWadFile(new File("LegoRR1.wad"));
			WadStream s = wad.getStream("credits.txt");
			WadStream s2 = wad.getStream("Lego.cfg");
			BufferedReader br = new BufferedReader(new InputStreamReader(s));
			BufferedReader br2 = new BufferedReader(new InputStreamReader(s2));
			String line = null;
			String line2 = null;
			while((line = br.readLine()) != null && (line2 = br2.readLine()) != null) {
				System.out.println("1: " + line);
				System.out.println("2: " + line2);
			}
			wad.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
