.PHONY: run clean

run: Main.class
	java Main
	echo `\n\n\n\n\n\n\n\n`

Main.class: *.java
	javac ./*.java

clean:
	rm ./*.class

