# SimpleDelete

This repository hosts the SimpleDelete Corpus and code used to create it.

The corpus can be found in TSV format in the file: simpleDeleteCorpus_Filtered.tsv

This file contains one instance per line and has the form:

<target><begin><end><context>

The target word should appear in the context bounded by the begin and end offsets. This target word is a candidate for deletion in the context.

This work was published at the Second Workshop on Text Simplification, Accessibility and Readability, Colocated with RANLP 2023, Varna, Bulgaria.

The paper is available via the ACL anthology, and also is included in this repository as 2023.tsar-1.5.pdf

https://aclanthology.org/2023.tsar-1.5/

The bibtex for citation is below:

```
@inproceedings{shardlow-przybyla-2023-simplification,
    title = "Simplification by Lexical Deletion",
    author = "Shardlow, Matthew  and
      Przyby{\l}a, Piotr",
    editor = "{\v{S}}tajner, Sanja  and
      Saggio, Horacio  and
      Shardlow, Matthew  and
      Alva-Manchego, Fernando",
    booktitle = "Proceedings of the Second Workshop on Text Simplification, Accessibility and Readability",
    month = sep,
    year = "2023",
    address = "Varna, Bulgaria",
    publisher = "INCOMA Ltd., Shoumen, Bulgaria",
    url = "https://aclanthology.org/2023.tsar-1.5",
    pages = "44--50",
    abstract = "Lexical simplification traditionally focuses on the replacement of tokens with simpler alternatives. However, in some cases the goal of this task (simplifying the form while preserving the meaning) may be better served by removing a word rather than replacing it. In fact, we show that existing datasets rely heavily on the deletion operation. We propose supervised and unsupervised solutions for lexical deletion based on classification, end-to-end simplification systems and custom language models. We contribute a new silver-standard corpus of lexical deletions (called SimpleDelete), which we mine from simple English Wikipedia edit histories and use to evaluate approaches to detecting superfluous words. The results show that even unsupervised approaches (TerseBERT) can achieve good performance in this new task. Deletion is one part of the wider lexical simplification puzzle, which we show can be isolated and investigated.",
}
```
