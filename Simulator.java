public class Simulator
{
	private static int[] memory;
	private static int[] registers;

	public static void run(String[] instructions)
	{
		for (int k = 0; k < instructions.length; k++)
		{
			int opcode = Integer.parseInt(instructions[k].substring(0, 3), 2);
			int additional = Integer.parseInt(instructions[k].substring(3, 4), 2);
			int argument = Integer.parseInt(instructions[k].substring(4), 2);

			switch (opcode)
			{
				case 0:
					int address = registers[11] * (int) Math.pow(2, 8) + registers[10];
					break;
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				case 6:
					break;
				case 7:
					break;
			}
		}
	}
}