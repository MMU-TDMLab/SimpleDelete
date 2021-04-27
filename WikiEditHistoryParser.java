import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class WikiEditHistoryParser extends DefaultHandler
{

  private StringBuilder          recordedText;
  private boolean                recordText;
  private boolean                goodTitle;
  private CoreDocument           priorRevision;
  private HashSet<String>        dict;
  private HashSet<String>        corpus;
  private HashSet<String>        spamList;
  private int                    count;

  private static StanfordCoreNLP pipeline;
  private static WikiConfig      config;
  private static WtEngineImpl    engine;
  private static PrintWriter     out;

  public static void main(String[] args) throws Exception
  {
    String dir =
        "/Users/mshardlow/Documents/Projects/Simplification/Plainifier/TerseBert_Eval/SimpleWikiEditHistories/";
    // String wikibooks = "simplewikibooks-20210401-pages-meta-history.xml";
    // String wikiquote = "simplewikiquote-20210401-pages-meta-history.xml";
    // String wiktionary = "simplewiktionary-20210401-pages-meta-history.xml";
    String wiki = "simplewiki-20210401-pages-meta-history.xml";

    out = new PrintWriter("src/main/resources/simpleDeleteCorpus.tsv");

    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit");

    pipeline = new StanfordCoreNLP(props);

    config = DefaultConfigEnWp.generate();
    engine = new WtEngineImpl(config);

    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    SAXParser saxParser = spf.newSAXParser();
    XMLReader xmlReader = saxParser.getXMLReader();
    xmlReader.setContentHandler(new WikiEditHistoryParser());

    // parseFile(xmlReader, dir, wikibooks);
    // parseFile(xmlReader, dir, wikiquote);
    // parseFile(xmlReader, dir, wiktionary);
    parseFile(xmlReader, dir, wiki);
    out.close();
  }

  private static void parseFile(XMLReader xmlReader, String dir,
      String filename) throws Exception
  {
    System.out.println("Reading: " + filename);
    xmlReader.parse(new InputSource(new FileReader(dir + filename)));
  }

  public void startDocument() throws SAXException
  {
    count = 0;
    recordedText = new StringBuilder();
    recordText = false;
    goodTitle = false;
    priorRevision = null;
    if (corpus == null)
      corpus = new HashSet<String>();

    if (dict == null)
    {
      dict = new HashSet<String>();
      String freqPath =
          "/Users/mshardlow/Documents/Projects/Simplification/Plainifier/MTurk/Baselines/vocab_cs";
      try (BufferedReader in = new BufferedReader(new FileReader(freqPath));)
      {
        String line;
        while ((line = in.readLine()) != null)
        {
          String[] splitLine = line.split("\t");
          long frequency = Long.parseLong(splitLine[1]);
          String key = splitLine[0];
          if (frequency > 10000)
            dict.add(key);
        } // while
      } catch (Exception e)
      {
        e.printStackTrace();
        System.out.println("Dictionary not loaded");
        System.exit(0);
      } // catch
    }

    if (spamList == null)
    {
      spamList = new HashSet<String>();

      String badWords = "src/main/resources/bad-words.txt";
      try (BufferedReader in = new BufferedReader(new FileReader(badWords));)
      {
        String line;
        while ((line = in.readLine()) != null)
        {
          spamList.add(line);
          spamList.add(line + "s");
          spamList.add(line + "es");
          spamList.add(line + "ed");
          spamList.add(line + "ing");
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
          spamList.add(line);
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
          "butthole", "poopy", "hahahahah", "james", "hiya"));
    }
  }

  public void endDocument()
  {
    out.flush();
    System.out.println(corpus.size());
  }

  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) throws SAXException
  {
    if (localName.equals("text") || localName.equals("title"))
      recordText = true;
    else if (localName.equals("page"))
    {
      priorRevision = null;
      count++;
      if (count % 10 == 0)
      {
        System.out.println(count + ": " + corpus.size());
        out.flush();
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
    if (recordText)
      recordedText.append(Arrays.copyOfRange(ch, start, (start + length)));
  }

  public void endElement(String uri, String localName, String qName)
      throws SAXException
  {
    if (localName.equals("text"))
    {
      if (goodTitle)
      {
        processText();
      }
      resetTextRecord();
    } else if (localName.equals("title"))
    {
      goodTitle = recordedText.indexOf(":") == -1;
    }
  }

  private void resetTextRecord()
  {
    recordedText.setLength(0);
    recordText = false;
  }

  private void processText()
  {
    try
    {
      String text = recordedText.toString();
      if (text.length() > 100000)
      {
        priorRevision = null;
        System.out.println(
            "overlong revision: " + text.substring(0, 1000).replace("\n", " "));
      } else
      {
        text = convertWikiText(text, 1000);

        // sometimes it doesn't capture all links on first pass, so run again
        if (text.contains("[") && text.contains("]"))
          text = convertWikiText(text, 1000);

        CoreDocument currentRevision =
            new CoreDocument(convertWikiText(text, 1000));

        pipeline.annotate(currentRevision);

        if (priorRevision != null)
        {
          for (CoreSentence currentSentence : currentRevision.sentences())
          {
            for (CoreSentence priorSentence : priorRevision.sentences())
            {
              List<String> currentTokens = currentSentence.tokensAsStrings();
              List<String> priorTokens = priorSentence.tokensAsStrings();

              int forwardIndex = 0;
              int backwardsIndexCurrent = currentTokens.size() - 1;
              int backwardsIndexPrior = priorTokens.size() - 1;

              while (forwardIndex < currentTokens.size()
                  && forwardIndex < priorTokens.size() && currentTokens
                      .get(forwardIndex).equals(priorTokens.get(forwardIndex)))
                forwardIndex++;

              while (backwardsIndexCurrent >= 0 && backwardsIndexPrior >= 0
                  && currentTokens.get(backwardsIndexCurrent)
                      .equals(priorTokens.get(backwardsIndexPrior)))
              {
                backwardsIndexCurrent--;
                backwardsIndexPrior--;
              }

              List<String> priorTokens2 =
                  new ArrayList<String>(priorSentence.tokensAsStrings());
              for (String token : currentSentence.tokensAsStrings())
              {
                int index = priorTokens2.indexOf(token);
                if (index != -1)
                  priorTokens2.remove(index);
              }

              if (priorTokens2.size() == 1 // check if max one token has been
                                           // dropped
                  && (priorSentence.tokens().size()
                      - currentSentence.tokens().size()) == 1 // check if the
                                                              // length
                                                              // difference
                                                              // between
                                                              // sentences
                                                              // is one
                  && dict.contains(priorTokens2.get(0)) // check if word is in
                                                        // dictionary
                  && currentSentence.tokens().size() < 30 // avoid long lines
                  && priorTokens2.get(0).matches("[a-z]+")// lowercase and avoid
                                                          // punctuation
                  && priorSentence.tokensAsStrings()
                      .indexOf(priorTokens2.get(0)) != 0 // avoid if first word
                                                         // in
                                                         // sentence
                  && !spamList.contains(priorTokens2.get(0).toLowerCase()) // avoid
                                                                           // spammy
                  // words
                  && forwardIndex == backwardsIndexPrior // ensure only
                                                         // difference is in
                                                         // dropped term
                  && priorTokens2.get(0).length() > 1) // min 2 chars in dropped
                                                       // term
              {
                int sentenceOffset =
                    priorSentence.tokens().get(0).beginPosition();
                int startIndex =
                    priorSentence.tokens().get(forwardIndex).beginPosition()
                        - sentenceOffset;
                int endIndex =
                    priorSentence.tokens().get(forwardIndex).endPosition()
                        - sentenceOffset;
                String corpusEntry = priorTokens2.get(0) + "\t" + startIndex
                    + "\t" + endIndex + "\t" + priorSentence.text()
                        .replace("\n", " ").replace("\t", " ");
                if (corpus.add(corpusEntry))
                {
                  out.println(corpusEntry);
                }

                break;
              }
            }
          }
        }

        priorRevision = currentRevision;
      }
    } catch (Exception e)
    {
      System.out.println("getText Failed with error: " + e.toString());
      priorRevision = null;
    }

  }

  // taken from:
  // https://stackoverflow.com/questions/11612118/java-wikitext-parser
  public static String convertWikiText(String wikiText, int maxLineLength)
      throws LinkTargetException, EngineException
  {
    PageTitle pageTitle = PageTitle.make(config, "-");
    PageId pageId = new PageId(pageTitle, -1);
    // Compile the retrieved page
    EngProcessedPage cp = engine.postprocess(pageId, wikiText, null);

    StringBuilder sb = new StringBuilder();
    getText(cp.iterator(), sb, 0);
    return sb.toString();
  }

  private static void getText(Iterator<WtNode> iterator,
      StringBuilder stringBuilder, int depth)
  {
    try
    {
      if (depth < 200)
      {
        while (iterator.hasNext())
        {
          WtNode next = iterator.next();
          if (next.getNodeName().equals("WtText"))
            stringBuilder.append(next.getProperty("content"));
          else if (next.getNodeName().equals("WtInternalLink"))
          {
            getText(next.get(1).getNodeTypeName().contains("WtNoLinkTitle")
                ? next.get(0).iterator()
                : next.get(1).iterator(), stringBuilder, depth + 1);
          } else if (next.getNodeName().equals("WtTemplate"))
            getText(next.get(1).iterator(), stringBuilder, depth + 1);
          else
            getText(next.iterator(), stringBuilder, depth + 1);
        }
      } else
      {
        System.out.println("Too deep parse for text: "
            + stringBuilder.toString().substring(0, 1000).replace("\n", " "));
      }
    } catch (Exception e)
    {
      System.out.println("getText Failed with error: " + e.toString());
    }

  }

}// class