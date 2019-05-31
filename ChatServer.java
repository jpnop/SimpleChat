//git hub address https://github.com/jpnop/SimpleChat

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}

	} // main
}

class ChatThread extends Thread{
	ArrayList<String> no=new ArrayList<>();
	
	no.add("banknumber");
	no.add("phonenumber");
	no.add("passportnumber");
	//{"banknumber","phonenumber","passportnumber"};
	int size=no.size();/	
	
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		//	pw.println("#Do not ask private information : accountnumber,phonenumber,passportnumber,residentnumber,acoountpassword");
			pw.flush();
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				else if(line.indexOf("/userlist")==0){
					send_userlist(id);
				}
				else if(line.indexOf("/addspam ")==0){
					addspam(line);
				}
				else if(line.indexOf("/spamlist")==0){
					spamlist(line,id);
				}

				else{
					if(ban(line,id)!=1)
					broadcast(id + " : " + line);
				}
			}

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				 long time=System.currentTimeMillis();
                                SimpleDateFormat dayTime=new SimpleDateFormat("hh:mm:ss");
                                String str=dayTime.format(new Date(time));
                                pw.println(str + id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw==hm.get(id)) continue; //if pw of user id is same with pw, skip and continue.
				
				SimpleDateFormat dayTime=new SimpleDateFormat("hh:mm:ss");
				String str=dayTime.format(new Date(time));
				pw.println(str + msg);
				pw.flush();
			}
		}
	} // broadcast

	public int ban(String msg,String id){		
		int p=0;
		int i;
                for(i=0; i<no.size();i++){
                   if(msg.indexOf(no[i])!=-1) {p=1; break;}
                   }
		if(p==1){
			Object obj=hm.get(id);
			if(obj!=null){
				PrintWriter pw=(PrintWriter)obj;
				pw.println("You shut up! your sentence contains ban word : " + no[i] );
				pw.flush();
			}
		}
	
		return p;
	} // if user typed such ban words using indexOf function, warnning sentence prints.
          
	*public  void addspam(String msg){
		no.add(msg);
	}
	public void spamlist(String msg,String id){
		 Object obj=hm.get(id);
                  if(obj!=null){
                      PrintWriter pw=(PrintWriter)obj;
		      for(int i=0; i<no.size(); i++){
			      pw.println( no[i] + " " );
                               
                        }
		      pw.flush();
		}

	public void send_userlist(String id){
		int count=0;
		PrintWriter pw=(PrintWriter)hm.get(id);
		Set set=hm.keySet();
		Iterator iteration=set.iterator();
		while(iteration.hasNext()){
			String temp=(String)iteration.next();
			pw.println(temp);
			pw.flush();
			count++;
		}
		pw.println("number of user : "+ count);
		pw.flush();
	}//KeySet function combines all of user id, Iterator reads one by one and then, prints id and counts.



}
