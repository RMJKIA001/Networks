To compile:
Type: make 

To run:
Open a terminal - *IMPORTANT* run server first ie
Type: make run 
	optional: if you want to edit the port number (i.e not run default)
Type: make run ARGS=[portNumber]

Then in a new terminal
Type: make runClient 
	to not use defaults
Type: make runClient ARGS=[username],[portNumber],[serverAddress]
or  : make runClient ARGS=[username],[portNumber]
or  : make runClient ARGS=[username]


Open server command:

> java Server [portNumber]

Open client command:

> java ChatAppClient [username] [portNumber] [serverAddress]
