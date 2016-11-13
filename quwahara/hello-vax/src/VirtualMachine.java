import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class VirtualMachine {
	
	static Memory memory;
	static int pc;

	public static void main(String[] args) {
		
		byte[] aout = null;
		try {
			aout = Files.readAllBytes(Paths.get(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int headerSize = 32;
		byte[] header = Arrays.copyOfRange(aout, 0, headerSize);

//		int magicNum = get4Byte(Arrays.copyOfRange(header, 0, 4));
		int textSize = get4Byte(Arrays.copyOfRange(header, 4, 8));
		int dataSize = get4Byte(Arrays.copyOfRange(header, 8, 12));
//		int bssSize = get4Byte(Arrays.copyOfRange(header, 12, 16));
//		int symsSize = get4Byte(Arrays.copyOfRange(header, 16, 20));
//		int entryAddr = get4Byte(Arrays.copyOfRange(header, 20, 24));
//		int trSize = get4Byte(Arrays.copyOfRange(header, 24, 28));
//		int drSize = get4Byte(Arrays.copyOfRange(header, 28, 32));

		int textSizeMem = (textSize + 0x1ff) - ((textSize + 0x1ff) % 0x200);
		int dataSizeMem = (dataSize + 0x1ff) - ((dataSize + 0x1ff) % 0x200);
		
		memory = new Memory(textSizeMem + dataSizeMem);
		
		memory.loadToMemory(aout, headerSize, 0, textSize);
		memory.loadToMemory(aout, headerSize + textSize, textSizeMem, dataSize);
		
		pc = 0;
		int ap = 0;
		int r1 = 0;
		while(pc < textSize) {
			switch (fetch()) {
			case (byte)0x0:
				switch (fetch()) {
				case (byte)0x0:
					// Do nothing
					break;
				}
				break;

			// movl
			case (byte)0xd0:
				switch (fetch()) {
				case (byte)0x8f:
					int srcOprnd = get4Byte(Arrays.copyOfRange(memory.getMemory(), pc, pc + 4));
					for(int i = 0; i < 4; ++i) {
						fetch();
					}
					switch (fetch()) {
					case (byte) 0x51:
						r1 = srcOprnd;
						break;
					case (byte) 0x5c:
						ap = srcOprnd;
						break;
					case (byte) 0x61:
						memory.getMemory()[r1] = (byte) (srcOprnd << 24 >> 24);
						memory.getMemory()[r1 + 1] = (byte) (srcOprnd << 16 >> 24);
						memory.getMemory()[r1 + 2] = (byte) (srcOprnd << 8 >> 24);
						memory.getMemory()[r1 + 3] = (byte) (srcOprnd >> 24);
						ap = srcOprnd;
						break;
					case (byte) 0xa1:
						int displacement = fetch();
						memory.getMemory()[r1 + displacement] = (byte) (srcOprnd << 24 >> 24);
						memory.getMemory()[r1 + displacement + 1] = (byte) (srcOprnd << 16 >> 24);
						memory.getMemory()[r1 + displacement + 2] = (byte) (srcOprnd << 8 >> 24);
						memory.getMemory()[r1 + displacement + 3] = (byte) (srcOprnd >> 24);
						ap = srcOprnd;
						break;
					}
					break;
				}
				break;

				// chmk
			case (byte)0xbc:
				switch (fetch()) {
				case (byte)0x1:
					System.exit(get4Byte(Arrays.copyOfRange(memory.getMemory(), ap + 4, ap + 8)));
					break;
				case (byte)0x4:
					StringBuffer c = new StringBuffer();
					int fd = get4Byte(Arrays.copyOfRange(memory.getMemory(), ap + 4, ap + 8));
					int address = get4Byte(Arrays.copyOfRange(memory.getMemory(), ap + 8, ap + 12));
					int length = get4Byte(Arrays.copyOfRange(memory.getMemory(), ap + 12, ap + 16));
					for(int j = 0; j < length; ++j) {
						c.append((char)memory.getMemory()[address + j]);
					}
					switch (fd) {
					case 1:
						System.out.print(c);
						break;
					}
				
					break;
				}
				break;
			}
		}
	}
	
	private static byte fetch() {
		byte fetchByte = memory.getMemory()[pc];
		++pc;
		return fetchByte;
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
