import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class IOManager
{
	public static String[] read(String path)
	{
		try
		{
			return readFile(path);
		}
		catch (IOException e)
		{
			return null;
		}
	}

	public static boolean write(String path, String[] file)
	{
		try
		{
			writeFile(path, file);
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}

	private static String[] readFile(String path) throws IOException
	{
		Scanner input = new Scanner(new File(path));
		List<String> file = new ArrayList<String>();

		while (input.hasNext())
			file.add(input.nextLine());

		input.close();

		return file.toArray(new String[file.size()]);
	}

	private static void writeFile(String path, String[] file) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));

		for (String line : file)
		{
			writer.write(line);
			writer.newLine();
		}

		writer.close();
	}
}