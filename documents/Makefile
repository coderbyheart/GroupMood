all: GroupMood.pdf

fetchmedia:
	# Aktualisiert externe Grafikressourcen
	# TODO:  PDF-Version verwenden
	# wget 'https://dl.dropbox.com/s/6if4n4hwa15drhv/GroupMood.png?dl=1' -O media/GroupMood.png
	wget 'https://docs.google.com/drawings/pub?id=1l4L820mmP-f-a9eSXz3J06o7X62ULCNetROviNARsM8&w=3000' -O media/Stimmung.png
	wget 'https://docs.google.com/drawings/pub?id=12rjlM8BKBg3HSZUyGDnus00I6_1NGbwt1h54abTsPEg&w=3000' -O media/Interesse.png
	wget 'https://docs.google.com/drawings/pub?id=19LIzlZ27yawPx6Lkb6PlAhIju_Ls64tq9_KNpnV_9iA&w=3000' -O media/Umfrage.png
	wget 'https://docs.google.com/drawings/pub?id=1obmDqAyLJxTEXqWiCD5I_NGshS9XVVXSJI38SvgPeLU&w=3000' -O media/Fotoabstimmung.png
	wget 'https://docs.google.com/drawings/pub?id=1CmTOZMzYvfOznmRbjiqz_PTNNmtQTyDzgxpqqwRZCn0&w=3000' -O media/app.png
	
GroupMood.pdf: *.tex media/*.png media/*.png
	-pdflatex -interaction=nonstopmode GroupMood.tex
	-pdflatex -interaction=nonstopmode GroupMood.tex
	pdflatex -interaction=nonstopmode GroupMood.tex

clean:
	-/bin/rm *.log *.aux *.toc *.out
