import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Disasm {
	
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
		
		pc = 0;
		while(pc < textSize) {
			System.out.printf("%08x: ", pc);
			switch (fetch()) {
			case (byte)0x0:
				if(pc > 1) {
					System.out.printf("halt\n");
					break;
				}
				switch (fetch()) {
				case (byte)0x0:
					System.out.printf(".word 0\n");
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
						System.out.printf("movl $0x%x,r1\n", srcOprnd);
						break;
					case (byte) 0x5c:
						System.out.printf("movl $0x%x,ap\n", srcOprnd);
						break;
					case (byte) 0x61:
						System.out.printf("movl $0x%x,(r1)\n", srcOprnd);
						break;
					}
					break;
				}
				break;

			// chmk
			case (byte)0xbc:
				switch (fetch()) {
				case (byte)0x1:
					System.out.printf("chmk $1\n");
					break;
				case (byte)0x4:
					System.out.printf("chmk $4\n");
					break;
				}
				break;
			}
		}
	}
	
	private static byte fetch() {
		byte fetchByte = memory.getMemory()[pc];
		++pc;
		System.out.printf("%02x ", fetchByte);
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
