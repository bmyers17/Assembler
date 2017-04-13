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
	
	private static Set<String> supported;
	private static HashMap<String, String> variables;
	private static HashMap<String, String[][]> macros;
	
	private static int id = 0;
	private static int location;
	
	public static String[][] assemble(String[] initFile)
	{
		initFile = clean(initFile);
		String[][] file = tokenize(initFile);
		
		location = getLocation(file);
		variables = new HashMap<String, String>();
		file = stripVars(file);

		addMacros(file);
		
		return prepare(file);
	}

	private static String[] clean(String[] file)
	{
		List<String> cleaned = new LinkedList<String>();

		for (String line : file)
		{
			String updatedLine = line.trim();

			if (line.charAt(0) == COMMENT)
				line = "";

			if (line.length() != 0)
				cleaned.add(line);
		}

		return cleaned.toArray(new String[cleaned.size()]);
	}

	private static String[][] tokenize(String[] file)
	{
		String[][] tokenized = new String[file.length][];

		for (int k = 0; k < file.length; k++)
		{
			List<String> tokens = new LinkedList<String>();
			Scanner parser = new Scanner(file[k]);

			while (parser.hasNext())
				tokens.add(parser.next());

			parser.close();

			tokenized[k] = tokens.toArray(new String[tokens.size()]);
		}

		return tokenized;
	}

	private static int getLocation(String[][] file)
	{
		return Integer.parseInt(file[0][0].substring(1), 16);
	}

	private static String[][] stripVars(String[][] file)
	{
		List<String[]> stripped = new LinkedList<String[]>();

		for (String[] line : file)
		{
			if (line[0].equals(LABEL) && !line[3].equals(CURRENT))
				variables.put(line[1], line[3]);
			else
				stripped.add(line);
		}

		return stripped.toArray(new String[stripped.size()][]);
	}

	private static void addMacros(String[][] file)
	{
		List<String[]> macroCode = new LinkedList<String[]>(Arrays.asList(tokenize(IOManager.read(macroFile))));

		int index = 0;
		boolean inMacro = false;
		String name = "";
		List<String[]> currentMacro = new LinkedList<String[]>();

		while (index < file.length && (inMacro || file[index][0].equals(MACRO)))
		{
			if (file[index][0].equals(MACRO))
			{
				inMacro = true;
				name = file[index][1];
			}
			else if (file[index][0].equals(END))
			{
				inMacro = false;
				macros.put(name, currentMacro.toArray(new String[currentMacro.size()][]));
				currentMacro = new LinkedList<String[]>();
			}
			else
				currentMacro.add(file[index]);
		}

	}
	
	private static String[][] prepare(String[][] file)
	{
		supported = new TreeSet<String>(Arrays.asList(IOManager.read(machineFile)));
		
		List<String[]> packed = new LinkedList<String[]>(Arrays.asList(file));
		List<String[]> unpacked = new LinkedList<String[]>();
		List<String[]> ready = new LinkedList<String[]>();
		List<String[]> machineReady = new LinkedList<String[]>();
		
		resolveMacros(packed, unpacked);
		resolveVariables(unpacked);
		resolveMachine(unpacked, ready);
		resolveAddresses(ready);
		resolveVariables(machineReady);
		
		return machineReady.toArray(new String[machineReady.size()][]);
	}
			      
	private static void resolveMacros(List<String[]> packed, List<String[]> unpacked)
	{
		if (packed.isEmpty())
			return;
		
		String[] line = packed.remove(0);
		
		if (!macros.containsKey(line[0]))
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
}
