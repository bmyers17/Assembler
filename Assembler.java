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
}