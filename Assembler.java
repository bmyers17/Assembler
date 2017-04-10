import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;

public class Assembler
{
	private static final char COMMENT = '#';
	private static final String LABEL = "label";
	private static final String CURRENT = "here";
	private static final char REGISTER = '@';
	private static final char ARGUMENT = '&';
	private static final String ASSEMBLER = "@assembler";
	private static final String LOAD = "LOD";
	private static final String L = 'L';
	private static final String machineFile = "machine.txt";
	private static final String macroFile = "macros.txt";
	
	private static List<String> supported;
	private static HashMap<String, String> variables;
	private static HashMap<String[], String[][]> macros;
	
	private static int id = 0;
	
	private static String[] assemble(String[] initFile)
	{
		initFile = clean(initFile);
		String[][] file = tokenize(initFile);
		
		variables = new HashMap<String, String>();
		file = stripVars(file, variables);
		
		return interpret(prepare(file));
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

	private static String[][] stripVars(String[][] file, HashMap<String, String> map)
	{
		List<String[]> stripped = new LinkedList<String[]>();

		for (String[] line : file)
		{
			if (line[0].equals(LABEL) && !line[3].equals(CURRENT))
				map.put(line[1], line[3]);
			else
				stripped.add(line);
		}

		return stripped.toArray(new String[stripped.size()][]);
	}
	
	private static String[] prepare(String[][] file)
	{
		supported = new TreeSet<String>(IOManager.read(machineFile));
		
		List<String[]> packed = new LinkedList<String[]>(file);
		List<String[]> unpacked = new LinkedList<String[]>();
		List<String[]> machineReady = new LinkedList<String[]>();
		
		resolveMacros(packed, unpacked);
		resolveVariables(unpacked);
		resolveMachine(unpacked, machineReady);
		resolveVariables(machineReady);
		
		while (!packed.isEmpty())
			unpackLine(packed.remove(0), packed, unpacked, supported);
		
		return unpacked.toArray(new String[unpacked.size()][]);
	}
			      
	private static void resolveMacros(List<String[]> packed, List<String[]> unpacked)
	{
		if (packed.isEmpty())
			return unpacked;
		
		line = packed.remove(0);
		
		if (!macros.containsKey(line[0]))
			unpacked.add(line);
		else
		{
			for (int k = 1; k < line.length; k++)
				variables.put(ARGUMENT + "" + k + "_" + id, line[k]);
			
			String[][] macro = copy(macros.get(line[0]));
			
			for (int k = 0; k < macro.length; k++)
				for (int j = 0; j < macro[k].length; j++)
					if (macro[k][j].charAt(0) = ARGUMENT)
						macro[k][j] += "_" + id;
			id++;
			
			for (int k = macro.length - 1; k >= 0; k--)
				packed.add(macro[k], 0);
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
			if (!line[0].equals(load) && line[1].charAt(0) == L)
			{
				ready.add(new String[2] {LOAD, ASSEMBLER, line[1].substring(1)});
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
