import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileDump {
	
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
		for(int i = 0; i < headerSize; i++) {
			System.out.printf("%02x ", header[i]);
			if(i % 16 == 15) {
				System.out.println();
			}
		}
		
		int textSize = get4Byte(Arrays.copyOfRange(header, 4, 8));
		int dataSize = get4Byte(Arrays.copyOfRange(header, 8, 12));
		int textSizeMem = (textSize + 0x1ff) - ((textSize + 0x1ff) % 0x200);
		int dataSizeMem = (dataSize + 0x1ff) - ((dataSize + 0x1ff) % 0x200);
		
		memory = new Memory(textSizeMem + dataSizeMem);
		
		memory.loadToMemory(aout, headerSize, 0, textSize);
		memory.loadToMemory(aout, headerSize + textSize, textSizeMem, dataSize);

		for(int i = 0; i < memory.getMemory().length; ++i) {
			System.out.printf("%02x ", memory.getMemory()[i]);
			if(i%16 == 15) {
				System.out.print("\n");
			}
		}
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
