import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

public class FilterCorpus
{

  public static void main(String[] args)
  {

    HashSet<String> spamList = new HashSet<String>();

    String badWords = "src/main/resources/bad-words.txt";
    try (BufferedReader in = new BufferedReader(new FileReader(badWords));)
    {
      String line;
      while ((line = in.readLine()) != null)
      {
        spamList.add(line.toLowerCase());
        spamList.add(line.toLowerCase() + "s");
        spamList.add(line.toLowerCase() + "es");
        spamList.add(line.toLowerCase() + "ed");
        spamList.add(line.toLowerCase() + "ing");
      } // while
    } catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Spam List not loaded");
      System.exit(0);
    } // catch

    String langs = "src/main/resources/lang_codes.txt";
    try (BufferedReader in = new BufferedReader(new FileReader(langs));)
    {
      String line;
      while ((line = in.readLine()) != null)
      {
        spamList.add(line.toLowerCase());
      } // while
    } catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Langs List not loaded");
      System.exit(0);
    } // catch

    spamList.addAll(Arrays.asList("hi", "stub", "not", "no", "puto", "easy",
        "simplify", "hkl", "png", "svg", "lol", "langen", "bjj", "hi", "hii",
        "hiii", "hiiii", "ugly", "bob", "ha", "haha", "hahaha", "hahahaha",
        "fukin", "friggin", "heyyyyy", "heyyyy", "heyyy", "heyy", "hey",
        "dongs", "mierda", "sucks", "bum", "blahblah", "randi", "bastard",
        "smelly", "benders", "hello", "kiran", "bloody", "lmao", "rofl",
        "hahahahahah", "yep", "shitty", "faisal", "ashton", "smells",
        "butthole", "poopy", "hahahahah", "james", "hiya", "hola", "pizza",
        "cheese", "yay", "wassup", "sucky", "shity", "penus", "noob", "nnn",
        "jk", "jim", "ji", "iiii", "idk", "hihi", "hahahahaha", "gggg",
        "fucken", "bla"));

    try (

        PrintWriter out = new PrintWriter(
            "src/main/resources/simpleDeleteCorpus_Filtered.tsv");
        BufferedReader in = new BufferedReader(
            new FileReader("src/main/resources/simpleDeleteCorpus.tsv")))
    {
      String line;
      while ((line = in.readLine()) != null)
      {
        String[] split = line.split("\t");
        if (!spamList.contains(split[0].toLowerCase()) && split[0].length() > 1)
          out.println(line);
      } // while
    } catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("could not parse corpus");
      System.exit(0);
    }
  }

}
