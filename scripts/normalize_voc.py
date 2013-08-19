import codecs
import sys

def norm(input_file, output_file, global_file, specific_file):
	input = codecs.open(input_file, "r", "utf-8")
	glob = codecs.open(global_file, "r", "utf-8")
	specific = codecs.open(specific_file, "r", "utf-8")
	output = codecs.open(output_file, "w", "utf-8")

	list_voc = []
	for line in glob:
		voc = line.replace("\n","")
		if len(voc.split("_")) > 1:
			list_voc.append((voc, voc.replace("_"," ")))
	for line in specific:
		voc = line.replace("\n","")
		if len(voc.split("_")) > 1:
			list_voc.append((voc, voc.replace("_"," ")))

	for line in input:
		if line != "\n":
			new_line = line
			for voc, voc_spaced in list_voc:
				new_line = new_line.replace(voc_spaced,voc)	
			output.write(new_line)

args = sys.argv
if len(args) != 6:
	print("The correct command is: python3 "+args[0]+" [INPUT_FILE_PATH] [OUTPUT_FILE_PATH] [INPUT_FILE_NAME] [GLOBAL_VOC] [SPECIFIC_VOC]")
norm(args[1]+args[3], args[2]+"voc-"+args[3], args[4], args[5])