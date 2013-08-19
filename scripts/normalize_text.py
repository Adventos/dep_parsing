import codecs
import sys
import re

def norm(input, output):
	finput = codecs.open(input,"r","utf-8")
	foutput = codecs.open(output,"w","utf-8")

	for line in finput:
		if line != "\n":
			line = line.replace("Cher Confrère, Chère Consoeur, ","")
			line = line.replace("Chère Consoeur, Cher Confrère, ","")
			line = line.replace("Cher Confrère, ","")
			line = line.replace("Chère Consoeur, ","")
			line = line.replace("Cher Monsieur, ","")
			line = line.replace("Cher Madame, ","")
			line = line.replace("Chère Consoeur, ","")
			line = re.sub("((V|v)(euillez agréer).*\.)|((J|j)(e vous prie d'agréer).*\.)","",line)
			line = line.replace("(Bien confraternellement)(\.)+","")
			line = line.replace("[Patient X]","Jean")
			line = line.replace("[Patient X+]","Jean")
			line = line.replace("[Patient]","Jean")
			line = line.replace("(e)","")
			line = line.replace("[DOC]","docteur")
			line = line.replace("[Monsieur X]","Jean")
			line = line.replace("[Madame X]","Jeanne")
			line = line.replace("</doc>","")
			index_i = line.find("<doc name=\"")
			index_j = line.find(">")
			line = line.replace(line[index_i:index_j+1]+" ","")
			line = line.replace(" [FIN]",".")
			line = line.replace("[ADRESSE]","rue de la terre")
			line = line.replace("[TEL]","0")
			line = line.replace("MOTIF DE L'EXAMEN: ","Le motif de l'examen est ")
			line = re.sub("(. Examen )\w( : )",". ",line)
			line = line.replace(" - "," , ")
			foutput.write(line)

	finput.close()
	foutput.close()

args = sys.argv
if len(args) != 4:
	print("The correct command is: python3 "+args[0]+" [INPUT_FILE_PATH] [OUTPUT_FILE_PATH] [INPUT_FILE_NAME]")
norm(args[1]+args[3], args[2]+"norm-"+args[3])