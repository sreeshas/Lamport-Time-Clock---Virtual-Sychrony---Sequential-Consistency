Readme.txt
/*
  Copyright (C) 2011  Sreenidhi Sreesha (sreenidhibs@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
  
*/


This code was written with the assumption that TCP connection in Java is simplex connection and 
each TCP connection should be "explicitly" started on new server port, which is not true..
No effort was spent on optimization of the code ( I am not proud of this code) and 
i believe, the code can be made way more better by correcting the above assumptions.
This project was part of distributed computing course at UF
and code was developed under limited time and extreme pressure :).


The program contains following classes.

Lamport
This is the main class of the program.It invokes all the other required threads as described below.

void Multicast(Message)
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

