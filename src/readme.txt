Readme.txt
The program contains following classes.

Lamport
This is the main class of the program.It invokes all the other required threads as described below.

void Mulitcast(Message)
This function implements Multicast functionality.It writes message object to sockets.Objects are serialized and sent which are deserialized at the receiving end.

void MessageOP(Message)
This function performs necessary operations on recieved messages

Delay
This class delays execution by an interval chosen at random.

UpdateMulticast
This class is used for generating specified number of UPDATE messages after delay between each UPDATE message which is chosen at ran

Receiver1 ,Reciever2 ,Reciever 3
Reciever classes are used to read objects sent from other processes.they run as independent threads.

Finisher
Finisher class is used for terminating the process after all update operations are completed.


Thread1
Thread1 is used for establishing communication with all processes 

Message
Message class contains structure of message which is sent over TCP connection.

TimeStamp
Contains the structure to hold logical clock

PayLoad
Contains structure to depict delta x ,delta y and process ID.

