dep_parsing
===========

To run the prototype, use the following command after having compiled:

java parser/Parse_file [input_file_path] [output_file_path] [global-voc_file_path] [specific-voc_file_path]

Note:
===========
* The input file has to be dependency trees in CoNLL format (with columns separeted by commas).
* Two trees have to be separated by a line of commas: ",,,,,,,,,,"
* The two vocabulary files have to be composed of words (one word per line). If you deal with compound words, link these words by underscores.
