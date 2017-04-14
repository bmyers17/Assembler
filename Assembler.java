import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.TreeSet;

public class Assembler
{
	private static final char COMMENT = '#';
	private static final String LABEL = "label";
	private static final String CURRENT = "here";
	private static final char REGISTER = '@';
	private static final char ARGUMENT = '&';
	private static final String ASSEMBLER = "@assembler";
	private static final String LOAD = "LOD";
	private static final char L = 'L';
	private static final String MACRO = "macro";
	private static final String END = "end";
	private static final String machineFile = "machine.txt";
	private static final String macroFile = "macros.txt";
	private static final String assembledFile = "assembled.txt";
	
	private static HashMap<String, String> variables;
	private static HashMap<String, String[][]> macros;
	private static HashMap<String, String> machineCodes;
	
	private static int id = 0;
	private static int location;
	
	public static List<String[]> assemble(String[] initFile)
	{
		machineCodes = new HashMap<String, String>();
		initCodes(machineCodes);
		
		initFile = clean(initFile);
		List<String[]> file = tokenize(initFile);
		
		location = getLocation(file);
		variables = new HashMap<String, String>();
		file = stripVars(file);

		macros = new HashMap<String, String[][]>();
		addMacros(file);
		
		file = prepare(file);
		logAssembled(file);
		return file;
	}

	public static void logAssembled(List<String[]> file)
	{
		String[] ready = new String[file.size()];

		int k = 0;
		for (String[] line : file)
		{
			for (int j = 0; j < line.length; j++)
			{
				if (j == 0)
					ready[k] = "";
				ready[k] += line[j] + " ";
			}

			k++;
		}

		IOManager.write(assembledFile, ready);
	}

	private static void initCodes(HashMap<String, String> codes)
	{
		List<String[]> file = tokenize(clean(IOManager.read(machineFile)));

		for (String[] assignment : file)
			codes.put(assignment[0], assignment[1]);
	}

	private static String[] clean(String[] file)
	{
		List<String> cleaned = new LinkedList<String>();

		for (String line : file)
		{
			String updatedLine = line.trim();

			if (line.length() == 0 || line.charAt(0) == COMMENT)
				line = "";

			if (line.length() != 0)
				cleaned.add(line);
		}

		return cleaned.toArray(new String[cleaned.size()]);
	}

	private static List<String[]> tokenize(String[] file)
	{
		LinkedList<String[]> tokenized = new LinkedList<String[]>();

		for (String line : file)
		{
			List<String> tokens = new LinkedList<String>();
			Scanner parser = new Scanner(line);

			while (parser.hasNext())
				tokens.add(parser.next());

			parser.close();

			tokenized.add(tokens.toArray(new String[tokens.size()]));
		}

		return tokenized;
	}

	private static int getLocation(List<String[]> file)
	{
		return Integer.parseInt(file.remove(0)[0].substring(1), 16);
	}

	private static List<String[]> stripVars(List<String[]> file)
	{
		List<String[]> stripped = new LinkedList<String[]>();

		for (String[] line : file)
		{
			if (line[0].equals(LABEL) && !line[3].equals(CURRENT))
				variables.put(line[1], line[3]);
			else
				stripped.add(line);
		}

		return stripped;
	}

	private static void addMacros(List<String[]> file)
	{
		List<String[]> macroCode = tokenize(IOManager.read(macroFile));

		boolean inMacro = false;

		while (inMacro || file.get(0)[0].equals(MACRO))
		{
			if (file.get(0)[0].equals(END))
				inMacro = false;
			else if (file.get(0)[0].equals(MACRO))
				inMacro = true;

			macroCode.add(file.remove(0));
		}

		String name = "";
		List<String[]> currentMacro = new LinkedList<String[]>();

		while (!macroCode.isEmpty())
		{
			String[] line = macroCode.remove(0);

			if (line[0].equals(MACRO))
			{
				currentMacro = new LinkedList<String[]>();
				name = line[1];
			}
			else if (line[0].equals(END))
				macros.put(name, currentMacro.toArray(new String[currentMacro.size()][]));
			else
				currentMacro.add(line);
		}
	}
	
	private static List<String[]> prepare(List<String[]> file)
	{		
		List<String[]> packed = file;
		List<String[]> unpacked = new LinkedList<String[]>();
		List<String[]> ready = new LinkedList<String[]>();
		List<String[]> machineReady = new LinkedList<String[]>();
		
		resolveMacros(packed, unpacked);
		resolveVariables(unpacked);
		resolveMachine(unpacked, ready);
		resolveAddresses(ready);
		resolveVariables(ready);

		return ready;
	}
			      
	private static void resolveMacros(List<String[]> packed, List<String[]> unpacked)
	{
		if (packed.isEmpty())
			return;
		
		String[] line = packed.remove(0);
		
		if (machineCodes.containsKey(line[0]))
			unpacked.add(line);
		else
		{
			for (int k = 1; k < line.length; k++)
				variables.put(ARGUMENT + "" + k + "_" + id, line[k]);
		
			String[][] macro = copy(macros.get(line[0]));
			
			for (int k = 0; k < macro.length; k++)
				for (int j = 0; j < macro[k].length; j++)
					if (macro[k][j].charAt(0) == ARGUMENT)
						macro[k][j] += "_" + id;
			id++;
			
			for (int k = macro.length - 1; k >= 0; k--)
				packed.add(0, macro[k]);
		}
		
		resolveMacros(packed, unpacked);
	}

	
	private static void resolveVariables(List<String[]> file)
	{
		for (String[] line : file)
			for (int k = 1; k < line.length; k++)
				if (variables.containsKey(line[k]))
					line[k] = variables.get(line[k]);
	}
	
	private static void resolveMachine(List<String[]> unpacked, List<String[]> ready)
	{
		fixNumbers(unpacked);
		addLoads(unpacked, ready);
	}

	private static void resolveAddresses(List<String[]> ready)
	{
		List<String[]> machineReady = new LinkedList<String[]>();
		int index = 0;

		for (String[] line : ready)
		{
			if (line[0].equals(LABEL) && line[3] == CURRENT)
				variables.put(line[1], "d" + (location + index));
			else
			{
				machineReady.add(line);
				index++;
			}
		}
	}
	
	private static void fixNumbers(List<String[]> unpacked)
	{
		for (String[] line : unpacked)
			for (int k = 1; k < line.length; k++)
				line[k] = convertBase(line[k]);
	}
	
	private static void addLoads(List<String[]> unpacked, List<String[]> ready)
	{
		for (String[] line : unpacked)
		{
			if (!line[0].equals(LOAD) && line[1].charAt(0) == L)
			{
				ready.add(new String[] {LOAD, ASSEMBLER, line[1].substring(1)});
				line[1] = ASSEMBLER;
			}
			
			ready.add(line);
		}	
	}
	
	private static String convertBase(String number)
	{
		char base = number.charAt(0);
		String rest = number.substring(1);
		int value = -1;
		
		if (base == 'x')
			value = Integer.parseInt(rest, 16);
		else if (base == 'd')
			value = Integer.parseInt(rest, 10);
		else if (base == 'b')
			value = Integer.parseInt(rest, 2);
		else
			return number;
		
		return L + Integer.toString(value, 2);
	}
			      
	private static String[][] copy(String[][] arr)
	{
		String[][] copied = new String[arr.length][];
		
		for (int r = 0; r < arr.length; r++)
		{
			copied[r] = new String[arr[r].length];
			for (int c = 0; c < arr[r].length; c++)
				copied[r][c] = arr[r][c];
		}
		
		return copied;
	}

	private static void print(List<String[]> file)
	{
		for (String[] line : file)
			System.out.println(Arrays.toString(line));
	}
}