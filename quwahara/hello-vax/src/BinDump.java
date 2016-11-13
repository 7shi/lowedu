import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BinDump {

	public static void main(String[] args) {
		byte[] aout = null;
		try {
			aout = Files.readAllBytes(Paths.get(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		printBin(aout, 0, aout.length);
	}

	private static void printBin(byte[] b, int start, int end) {
		StringBuffer c = new StringBuffer();
		
		int i;
		for(i = start; i < end; i++) {
			if(i % 16 == 0) {
				System.out.printf("%08x ", i);
			}
			System.out.printf("%02x ", b[i]);
			
			if(b[i] < 33 || b[i] > 126) {
				c.append(".");
			}
			else {
				c.append((char)b[i]);
			}
			if(i % 16 == 15) {
				printChar(c);
			}
		}
		if(((i - 1) % 16) < 15) {
			for(; ((i - 1) % 16) < 15; i++) {
				System.out.printf("   ");
			}
			printChar(c);
		}
	}

	private static void printChar(StringBuffer c) {
		System.out.println(c);
		c.delete(0, c.length());
	}


}
