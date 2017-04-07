SRCDIR = src
BINDIR = bin


JAVAC = javac
JFLAGS = -g -d $(BINDIR) -cp $(BINDIR):$(JUNIT)


vpath %.java $(SRCDIR)
vpath %.class $(BINDIR)

# define general build rule for java sources
.SUFFIXES:  .java  .class

.java.class:
	$(JAVAC)  $(JFLAGS)  $<

#default rule - will be invoked by make

all: Message Client

# The following two targets deal with the mutual dependency 
Message: Server

Server:
	rm -rf $(BINDIR)/Message.class
	javac $(JFLAGS) $(SRCDIR)/Message.java $(SRCDIR)/Server.java
	
Client:
	javac $(JFLAGS) $(SRCDIR)/ChatAppClient.java
	
run:
	java -classpath bin/ Server ${ARGS}

runClient:
	java -classpath bin/ ChatAppClient ${ARGS}
	
clean:
	@echo "Removing class files"
	@rm -f  $(BINDIR)/*.class
	@rm -f $(SRCDIR)/*.class
