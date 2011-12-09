import java.io.BufferedReader;
import java.net.*;
import java.io.BufferedWriter;
import java.io.DataInputStream;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Lamport extends Thread {
	static int clockrate;
	static  volatile long  clock;
	static volatile HashMap<Long,Integer> ackHash =new HashMap<Long,Integer>();
	static  volatile Vector<Message> Queue = new Vector<Message>();
	static volatile TreeMap<Long,Message> tQueue= new TreeMap<Long,Message>();
	static volatile int xBall;
	static volatile int yBall;
	static ArrayList<String> Address;
	static ArrayList<String> logContainer = new ArrayList<String>();
	 int  updateOperations;
	 static int oplimit=0;
	static int ID;
	static boolean connectionComplete;
	static boolean connection0;
	static boolean connection1;
	static boolean connection2;
	static Socket SelfClient=null;
	static ServerSocket SelfServer=null;
	static Socket MyClient1=null;
	static Socket MyClient2=null;
	static ServerSocket MyService1=null;
	static ServerSocket MyService2=null;
	static Socket localClient=null;
	static Socket localServer=null;
	static Socket clientSocket1 = null;
	static Socket clientSocket2 = null;
	static int Interval;
	static Object lock=new Object();
	static int  finishcount=0;
	volatile static boolean clockstopper=false;
	static int operationindex=1;

	public static Object updatemulticast= new Object();
	static int ackcount=0;
	 static int mcount=0;
	 static ObjectInputStream oos1=null;
	 static ObjectInputStream oos2=null;
	 static ObjectInputStream oos3=null;
	 static ObjectOutputStream ois1=null;
	 static ObjectOutputStream ois2=null;
	 static ObjectOutputStream ois3=null;
	static boolean killme=false;
	static boolean exitflag=false;
	private static Object finishme=new Object();
	static int a=0;
	static int b=0;
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    public static synchronized void clockincrement(){
    	clock=clock+clockrate;
    }
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());

	  }
	public synchronized static void logWriter(ArrayList<String> s) throws IOException{
		FileWriter fstream = new FileWriter("log"+Lamport.ID);
	     BufferedWriter out = new BufferedWriter(fstream);
	     for(String s1:s){
	    	 out.write(s1); 
	    	 out.newLine();
	    	 out.flush();
	    	 }
	   out.close();
	   exitflag=true;
	  }
	
	public synchronized static void Multicast(Message m1){
		
				
			   
				try{
				ois1.writeObject(m1);
				ois1.flush();
				//System.out.println(" Sending " + m1.T + " to " + " self"+" with values x,y "+m1.P.x+","+m1.P.y);
				//oos.close();
			  
			    
				ois2.writeObject(m1);
				ois2.flush();
				//System.out.println(" Sending " + m1.T + " to " + " Process 1"+" with values x,y "+m1.P.x+","+m1.P.y);
				//oos.close();
		
			
				
				ois3.writeObject(m1);
				ois3.flush();
				//System.out.println(" Sending " + m1.T + " to " + " Process 2"+" with values x,y "+m1.P.x+","+m1.P.y);
				//oos.close();
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			
	
		
		
	}
	public synchronized static void MessageOP(Message m1) throws IOException{
       
		if(m1.T.equals("NOOP")){
			Lamport.oplimit=Lamport.oplimit+m1.P.x;
			return;
		}
		if(m1.T.equals("FINISH")){
			finishcount++;
			
			if(finishcount==3){
				//System.out.println("closing all threads");
				Lamport.killme=true;
				Finisher.stopthread=true;
				Finisher.done();
				Lamport.clockstopper=true;
				Lamport.done();
				Reciever1.stopthread=true;
				Reciever1.done();
				Reciever2.stopthread=true;
				Reciever2.done();
				Reciever3.stopthread=true;
				Reciever3.done();
				System.exit(0);
			}
			
			return;
		}
		if(m1.T.equals("UPDATE")){
			    long x = m1.ts.clock%10;
				long y=m1.ts.clock-x;
				if(clock<y)
				{	
				clock=y+ID;
			    }
				Queue.add(m1);
				//tQueue.put(m1.ts.clock,m1);
		
		
		PayLoad p1 = new PayLoad(m1.P.x,m1.P.y,ID);
			Message m2 = new Message("ACK",p1,m1.ts);
			//multicast ACK
			
			Multicast(m2);
			


			return;
		}
		else if(m1.T.equals("ACK")){
			ackcount++;
			//System.out.println("AckCount "+ackcount);
			Integer a=(Integer)ackHash.get(m1.ts.clock);
			if(a==null){
				ackHash.put(m1.ts.clock, 1);
				return;	
			}
			
			int i=a.intValue();
		    i++;
			ackHash.put(m1.ts.clock,i);
				
			
			if(i==3){
				Queue.remove(m1);
				//tQueue.remove(tQueue.firstKey());
				//System.out.println("xBall : " +m3.P.x);
				//System.out.println("yBall : "+m3.P.y);
				int xBall1=xBall+m1.P.x;
				int yBall1=yBall+m1.P.y;
				long clock1=clock;
				logContainer.add("["+Lamport.now()+"] "+"["+"OP"+operationindex+":"+"C"+clock1+"]"+"Ball moved to"+"("+xBall1+")"+","+"("+yBall1+") "+"from"+"("+xBall+")"+","+"("+yBall+") ");
				System.out.println("["+Lamport.now()+"] "+"["+"OP"+operationindex+":"+"C"+clock1+"]"+"Ball moved to"+"("+xBall1+")"+","+"("+yBall1+") "+"from"+"("+xBall+")"+","+"("+yBall+") ");
				operationindex++;
				xBall=xBall1;
				yBall=yBall1;
				Lamport.oplimit--;
				return;
			}
		
			
			

			
			
		}





	}
	public void loadAddress() throws IOException{
		FileInputStream fstream = new FileInputStream("info.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine=null;
		Address = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   
		{   
           
			Address.add(strLine);

		}
		in.close();
	}
	public static void done(){
		clockstopper=true;
	}
	public void run(){
		
		try{
			while(clockstopper!=true){
				Thread.sleep(1000);
				clockincrement();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
    public static void setstream() throws IOException{
    
    	//set input stream and output stream
    	try{
    	if(ID==0){
    		
    		
    		ois1 = new ObjectOutputStream(localServer.getOutputStream());
    		
			ois2 = new ObjectOutputStream(clientSocket1.getOutputStream());
		
		    ois3 = new ObjectOutputStream(clientSocket2.getOutputStream());
		    
    		oos1 = new ObjectInputStream(SelfClient.getInputStream());
		   
    		oos2 = new ObjectInputStream(clientSocket1.getInputStream());
    	
		    oos3 = new ObjectInputStream(clientSocket2.getInputStream());
		     
	    }
    	if(ID==1){
    	
    		ois1 = new ObjectOutputStream(MyClient1.getOutputStream());
			
			ois2 = new ObjectOutputStream(localServer.getOutputStream());
	
			ois3 = new ObjectOutputStream(clientSocket1.getOutputStream());
		
			oos1 = new ObjectInputStream(SelfClient.getInputStream());
			
			oos2 = new ObjectInputStream(MyClient1.getInputStream());
			
			oos3 = new ObjectInputStream(clientSocket1.getInputStream());
		
				
			
    	}
    	if(ID==2){
    		
    		ois1= new ObjectOutputStream(MyClient1.getOutputStream());
			ois2= new ObjectOutputStream(MyClient2.getOutputStream());
		    ois3 = new ObjectOutputStream(localServer.getOutputStream());
		    oos1 = new ObjectInputStream(MyClient1.getInputStream());
	        oos2 = new ObjectInputStream(MyClient2.getInputStream());
		    oos3 = new ObjectInputStream(SelfClient.getInputStream());

				
				
    		
    	}
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	}
    	
    	
    
    	
    }
	public static void main(String[] args) throws InterruptedException, IOException{
		
		
		Lamport l1 = new Lamport();
		Lamport.ID=Integer.parseInt(args[0]);
		Lamport.clock=ID;
		
		l1.updateOperations=Integer.parseInt(args[1]);
		Lamport.clockrate=Integer.parseInt(args[2])*10;
		 
		l1.loadAddress();
		
		int i=0;
		
	   for(String s :Address){
			StringTokenizer st = new StringTokenizer(s);
			
			logContainer.add("P"+"["+i+"] "+st.nextToken()+": "+st.nextToken());
		   
			i++;
		
		}
	
		
		logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"] "+" establishing connections with other processes");
		
	
		
		l1.start();
		connectionComplete = false;
		connection0=false;
		connection1=false;
		connection2=false;
		
	      if(ID==2){
	    	  a=0;
	    	  b=1;
	      }
	      if(ID==1){
	    	  a=0;
	    	  b=2;
	      }
	      if(ID==0){
	    	  a=1;
	    	  b=2;
	      }
		

		try 
		{
			l1.loadAddress();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		//connection establishing thread
        Thread1 t1 = new Thread1();
        t1.start();
		

		synchronized (Lamport.lock) {
			Lamport.lock.wait();
		}
		logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"] "+" All processes connected");
		setstream();
	
		/*phase 2 starts here */
		//generate random interval
		Random randomGenerator = new Random();
		Lamport.Interval = randomGenerator.nextInt(1000)+1;
		Reciever1 r1 = new Reciever1();
		r1.start();
		Reciever2 r2 = new Reciever2();
		r2.start();
		Reciever3 r3 = new Reciever3();
		r3.start();
		
		//logContainer.add("["+Lamport.now()+"]"+"Starting Receiver thread to listen to messages from other processes");
		PayLoad p1 = new PayLoad(l1.updateOperations,0,ID);
		TimeStamp ts = new TimeStamp(0);
		Message m1 = new Message("NOOP",p1,ts);
		Multicast(m1);
		while(l1.updateOperations!=0){

         Delay d1 = new Delay();
         d1.start();
         synchronized(Lamport.lock){
        	 Lamport.lock.wait();
         }

         UpdateMulticast u = new UpdateMulticast();
         u.start();
        
         l1.updateOperations--;
         //System.out.println("No of updateOperations remaining "+l1.updateOperations);
}
	
	    
	

	   while(Lamport.oplimit!=0){
		System.out.print("");   
	   }
	   
	   logContainer.add("["+Lamport.now()+"] "+"P"+"["+Lamport.a+"] "+"finished");
       logContainer.add("["+Lamport.now()+"] "+"P"+"["+Lamport.b+"] "+"finished");
       
       logContainer.add("["+Lamport.now()+"]"+"All finished P"+"["+ID+"]"+"is terminating... ");
       logWriter(logContainer);
       
       
      
      Finisher f1 = new Finisher();
        f1.start();
        synchronized(Lamport.finishme){
        	Lamport.finishme.wait();
        }
        
      if(Lamport.killme){
    	  
    	  while(r1.isAlive()||r2.isAlive()||r3.isAlive()||l1.isAlive()){
    		     Reciever1.stopthread=true;
    		     Reciever1.done();
    			 Reciever2.stopthread=true;
    			 Reciever2.done();
    			 Reciever3.stopthread=true;
    			 Reciever3.done();
    			 Lamport.clockstopper=true;
    			 Lamport.done();
    			 Finisher.stopthread=true;
    			 Finisher.done();
    			 System.exit(0);
    	  }
      }
      
 
    
     
        


	}

	
   public static class Delay extends Thread{
	   static volatile boolean stopDelay;
	   public void run(){
		   try {
			Thread.sleep(Lamport.Interval);
			synchronized(Lamport.lock){
				Lamport.lock.notifyAll();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	   }
   }
	public static class UpdateMulticast extends Thread{
		public void run(){
			Random randomGenerator = new Random();
			int x=randomGenerator.nextInt(201)-100;
			int y=randomGenerator.nextInt(201)-100;
			PayLoad p1 = new PayLoad(x,y,ID);
			clock=clock+clockrate;
			TimeStamp t1 = new TimeStamp(clock);

			Message m1 = new Message("UPDATE",p1,t1);
			Multicast(m1);
			


		}
	}
    public static class Reciever1 extends Thread{
    	static volatile boolean stopthread=false;
    	public static void done(){
    		stopthread=true;
    	}
    	public void run(){
    		while(stopthread==false)
     	   {
         	Message m1;
			try {
				m1 = (Message)oos1.readObject();
				if(m1!=null){
	         		//System.out.println("Recieved " +m1.T+ "from " +m1.P.ID +"at "+m1.ts.clock + "and " +"xValue,yValue "+m1.P.x+" ,"+m1.P.y);
	         		MessageOP(m1);
	         	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.exit(0);
				System.out.println("IOexception on r1");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				
			}
         	
     	   }
    	}
    }
    public static class Reciever2 extends Thread{
    	static volatile boolean stopthread=false;
    	public static void done(){
    		stopthread=true;
    	}
    	public void run(){
    		while(stopthread==false){
    			Message m2;
				try {
					m2 = (Message)oos2.readObject();
					if(m2!=null){
	            		//System.out.println("Recieved " +m2.T+ "from " +m2.P.ID +"at "+m2.ts.clock + "and " +"xValue,yValue "+m2.P.x+" ,"+m2.P.y);
	            		MessageOP(m2);
	            	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.exit(0);
					System.out.println("IOexception on r2");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
            	
    		}
    }
    }
    	 public static class Reciever3 extends Thread {
    		 static volatile boolean stopthread=false;
    		 public static void done(){
    	    		stopthread=true;
    	    	}
    	    	public void run(){
    	    		while(stopthread==false){
    	    			Message m3;
						try {
							m3 = (Message)oos3.readObject();
							if(m3!=null){
	                    		//System.out.println("Recieved " +m3.T+ "from " +m3.P.ID +"at "+m3.ts.clock + "and " +"xValue,yValue "+m3.P.x+" ,"+m3.P.y);
	                    		MessageOP(m3);
	                    	}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.exit(0);
							System.out.println("IOexception on r3");
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
                    	
    	    		}
    	    }
    	 }
    	
	

		



	
	
	public static class Finisher extends Thread{
		static volatile boolean stopthread=false;
		public static void done(){
    		stopthread=true;
    	}
		public void run(){

			while(stopthread!=true){
				if(Queue.isEmpty()){
					finishcount++;
					PayLoad p1 = new PayLoad(0,0,Lamport.ID);
					TimeStamp t1 = new TimeStamp(0);
					Message m1 = new Message("FINISH",p1,t1);
					Multicast(m1);
					stopthread=true;
					
					break;
					}
			}
			 synchronized(Lamport.finishme){
				 
			 Lamport.finishme.notifyAll();   
			   }
				}
	}
	public static class Thread1 extends Thread {
		public void run() {
			
		    
			while(connectionComplete!=true){
				if(ID==0){
					//set up a Server and client for self
					while(connection0 == false){
						try {
							
							StringTokenizer st = new StringTokenizer(Address.get(0));
							st.nextToken();
							int serverport=Integer.parseInt(st.nextToken())-2;
							SelfServer = new ServerSocket(serverport);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Opening Server on port "+serverport);
							
							StringTokenizer st1 = new StringTokenizer(Address.get(0));
							String serverip=st1.nextToken();
							
							SelfClient = new Socket(serverip,serverport);
							while(localServer==null){
							localServer = SelfServer.accept();   
							}
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to "+" P"+"["+ID+"]"+"( "+serverip+" )");
							connection0=true;
													}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to Connection Request from Process 0");
						}
					}

					//listen to connection request from 1
					while(connection1==false)
					{

						try {
							StringTokenizer st = new StringTokenizer(Address.get(0));
							st.nextToken();
							int serverport=Integer.parseInt(st.nextToken());
							MyService1 = new ServerSocket(serverport);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Opening Server on port "+serverport);
							while(clientSocket1==null){
							clientSocket1 = MyService1.accept();   
							}
							StringTokenizer st1= new StringTokenizer(Address.get(1));
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"connected from P[1] "+st1.nextToken());
							connection1=true;
							
						}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to Connection Request from Process 1");
						}



					}
					//listen to connection request from 2
					while(connection2==false){

						try {
							StringTokenizer st = new StringTokenizer(Address.get(0));
							st.nextToken();
							int serverport=Integer.parseInt(st.nextToken())-1;
							MyService2 = new ServerSocket(serverport);
					logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Opening Server on port "+serverport);
							while(clientSocket2==null){
							clientSocket2 = MyService2.accept();   
							}
							StringTokenizer st1= new StringTokenizer(Address.get(2));
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"connected from P[2] "+st1.nextToken());
							connection2=true;
						}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to connection request from Process 2");
						}



					}
					//creating local service

					connectionComplete = true;

				}
				if(ID==1){
					//connect to 0
					while(connection0==false){

						try {
							StringTokenizer st = new StringTokenizer(Address.get(0));
							String process0=st.nextToken();
							int portnumber=Integer.parseInt(st.nextToken());
							MyClient1 = new Socket(process0,portnumber);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to Process 0 "+"["+process0+"] "+"on "+portnumber);
							connection0=true;
						} 
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Trying to Connect to Process 0");
						}
					}
					//listen to connection request from 2
					while(connection2==false){

						try {
							StringTokenizer st = new StringTokenizer(Address.get(1));
							st.nextToken();
							int portnumber=Integer.parseInt(st.nextToken());
							MyService1 = new ServerSocket(portnumber);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"opened server on port"+portnumber);
							
							while(clientSocket1==null){
								clientSocket1 = MyService1.accept();  
							}
							StringTokenizer st1 = new StringTokenizer(Address.get(2));
							
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"connected from P[2] "+st1.nextToken());
							connection2=true;
						}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to connection request from Process 2");
						}



					}
					//set up a Server and client for self
					while(connection1 == false){
						try {
							StringTokenizer st = new StringTokenizer(Address.get(1));
							st.nextToken();
							int serverport=Integer.parseInt(st.nextToken())-2;
							SelfServer = new ServerSocket(serverport);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Opening Server on port "+serverport);
							StringTokenizer st1 = new StringTokenizer(Address.get(1));
							String serverip=st1.nextToken();
							SelfClient = new Socket(serverip, Integer.parseInt(st1.nextToken())-2);
							while(localServer==null){
							localServer = SelfServer.accept();   
							}
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to "+" P"+"["+ID+"]"+"( "+serverip+" )");
							connection1=true;
						}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to Connection Request from Process 1");
						}
					}


					connectionComplete=true;

				}
				if(ID==2){
					//connect to 1
					while(connection1==false){

						try {
							StringTokenizer st = new StringTokenizer(Address.get(1));
							String process1=st.nextToken();
							int portnumber=Integer.parseInt(st.nextToken());
							MyClient1 = new Socket(process1,portnumber);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to Process 1 "+"["+process1+"] "+"on "+portnumber);
							connection1=true;
						} 
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Trying to Connect to Process 1");
						}
					}


					//connect to 0
					while(connection0==false){

						try {
							StringTokenizer st = new StringTokenizer(Address.get(0));
							String process0=st.nextToken();
							int portnumber=Integer.parseInt(st.nextToken())-1;
							MyClient2 = new Socket(process0, portnumber);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to Process 0 "+"["+process0+"] "+"on "+portnumber);
							connection0=true;
						} 
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Trying to Connect to Process 0");
						}
					}
					//set up a Server and client for self
					while(connection2 == false){
						try {
							StringTokenizer st = new StringTokenizer(Address.get(2));
							st.nextToken();
							int serverport=Integer.parseInt(st.nextToken())-2;
							SelfServer = new ServerSocket(serverport);
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Opening Server on port "+serverport);
							StringTokenizer st1 = new StringTokenizer(Address.get(2));
							String serverip=st1.nextToken();
							 serverport=Integer.parseInt(st1.nextToken())-2;
							SelfClient = new Socket(serverip,serverport );
							while(localServer==null){
							localServer = SelfServer.accept();   
							}
							logContainer.add("["+Lamport.now()+"]"+" P"+"["+ID+"]"+"Connected to "+" P"+"["+ID+"]"+"( "+serverip+" )");
							connection2=true;
						}
						catch (IOException e) {
							//System.out.println(e);
							//System.out.println("Listening to Connection Request from Process 0");
						}
					}

					connectionComplete=true;

				}

			}
			logContainer.add("["+Lamport.now()+"]"+" All Connection established successfully");
			System.out.println("["+Lamport.now()+"]"+" All Connection established successfully");
			synchronized (Lamport.lock) {
				Lamport.lock.notifyAll();
			}
			
		}

	}




}
class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String T = new String();
	PayLoad P= new PayLoad(0,0,Lamport.ID);
	TimeStamp ts= new TimeStamp(Lamport.clock);
	//volatile int[] Ack= new int[3];
	 volatile int ackcount=0;
	boolean flag = false;

	Message(String t,PayLoad p1,TimeStamp ts){
		this.T=t;
		this.P.x=p1.x;
		this.P.y=p1.y;
		this.P.ID=p1.ID;
		this.ts=ts;

	}

	@Override
	public boolean equals(Object obj) {
		Message m=(Message)obj;
		return (this.ts.clock==m.ts.clock?true:false);
	}

}
class PayLoad implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int x;
	int y;
	int ID;
	PayLoad(int x,int y,int ID){
		this.x=x;
		this.y=y;
		this.ID=ID;
	}

}
class ACK implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	int ID;
	TimeStamp t1;
	public ACK(int ID,TimeStamp t1)
	{
		this.ID=ID;
		this.t1=t1;
	}
}
class TimeStamp implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	long clock;
	public TimeStamp(long clock2) 
	{
		this.clock=clock2;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		TimeStamp t=(TimeStamp)obj;
		return (this.clock==t.clock?true:false);
	}
	
	

}
