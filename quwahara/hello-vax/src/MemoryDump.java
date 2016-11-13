import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MemoryDump {
	
	static Memory memory;

	public static void main(String[] args) {
		
		byte[] aout = null;
		try {
			aout = Files.readAllBytes(Paths.get(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int headerSize = 32;
		byte[] header = Arrays.copyOfRange(aout, 0, headerSize);

		int magicNum = get4Byte(Arrays.copyOfRange(header, 0, 4));
		int textSize = get4Byte(Arrays.copyOfRange(header, 4, 8));
		int dataSize = get4Byte(Arrays.copyOfRange(header, 8, 12));
		int bssSize = get4Byte(Arrays.copyOfRange(header, 12, 16));
		int symsSize = get4Byte(Arrays.copyOfRange(header, 16, 20));
		int entryAddr = get4Byte(Arrays.copyOfRange(header, 20, 24));
		int trSize = get4Byte(Arrays.copyOfRange(header, 24, 28));
		int drSize = get4Byte(Arrays.copyOfRange(header, 28, 32));
		
		// Print Header
		System.out.printf("magic = %08x, text = %08x, data = %08x, bss = %08x\n", magicNum, textSize, dataSize, bssSize);
		System.out.printf("syms = %08x, entry = %08x, trsize = %08x, drsize = %08x\n", symsSize, entryAddr, trSize, drSize);
		
		int textSizeMem = (textSize + 0x1ff) - ((textSize + 0x1ff) % 0x200);
		int dataSizeMem = (dataSize + 0x1ff) - ((dataSize + 0x1ff) % 0x200);
		
		memory = new Memory(textSizeMem + dataSizeMem);
		
		memory.loadToMemory(aout, headerSize, 0, textSize);
		memory.loadToMemory(aout, headerSize + textSize, textSizeMem, dataSize);

		System.out.printf(".text\n");
		printBin(memory.getMemory(), 0, textSize);
		System.out.printf(".data\n");
		printBin(memory.getMemory(), textSizeMem, textSizeMem + dataSize);
	}
	
	private static void printBin(byte[] b, int start, int end) {
		
		StringBuffer c = new StringBuffer();

		int i;
		for(i = start; i < end; ++i) {
			if(i%16 == 0) {
				System.out.printf("%08x ", i);
			}
			System.out.printf("%02x ", b[i]);
			if(b[i] < 33 || b[i] > 126) {
				c.append(".");
			}
			else {
				c.append((char)b[i]);
			}
			if(i%16 == 15) {
				printChar(c);
			}
		}
		if(((i-1)%16) < 15) {
			for(; ((i-1)%16) < 15; ++i) {
				System.out.printf("   ");
			}
			printChar(c);
		}
		
	}

	private static void printChar(StringBuffer c) {
		System.out.print(c);
		System.out.print("\n");
		c.delete(0,  c.length());
	}

	private static int get4Byte(byte[] b) {
		return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	static class Memory {
		static byte[] memory;
		
		Memory(int size) {
			memory = new byte[size];
		}
		
		void loadToMemory(byte[] src, int srcPos, int destPos, int length) {
			System.arraycopy(src, srcPos, memory, destPos, length);
		}
		
		byte[] getMemory() {
			return memory;
		}
		
	}

}
