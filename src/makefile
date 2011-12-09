GS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Lamport.java 
        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
