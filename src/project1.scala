/**
 * Code to mine bitcoins
 * 
 */

//import BearerTokenGenerator._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.actors.Actor.self
import scala.actors.TIMEOUT
import scala.actors.threadpool.TimeUnit
import java.security.SecureRandom
import java.security.MessageDigest


case object execute
case object interrupt
case object done
case class bitcoin(input :String,output : String)
case object terminate
case class terminate(duration: Long)

case object rmsg
case object msg
case object Test
case object sendzeroes
case object getNoOfZeroes

case object sendActorCount
case object actorcount

object project1{
   def main(args: Array[String]): Unit = { 
  //Accept number of zeroes and ip address arguments from command line
  
  var param =  args(0)
  var remoteHost=""
  var zeroesExpected=0
  if(param.toString().contains('.')){
 remoteHost = param.toString()  
  }
  else{
 zeroesExpected = args(0).toInt
 remoteHost = "127.0.0.1" 
  }
 
  
 
  //2nd method
  if(remoteHost == "127.0.0.1"){
    val masterSystem = ActorSystem("MasterSystem", ConfigFactory.load("Master.conf"))
    val master=masterSystem.actorOf(Props(new Master(zeroesExpected,remoteHost)),name="Master") 
    val processors = Runtime.getRuntime().availableProcessors()
    
    for(i<- 0 to processors-1){
    	
    	val actorName="MiningActor"+i.toString()
    	var actor = masterSystem.actorOf(Props(new MiningActor(remoteHost,master,zeroesExpected)), name = actorName)
    	actor!sendActorCount
    	actor! execute     
    }
    
    //Start timer
    val timer = masterSystem.actorOf(Props(new Timer(master)), "timer")
   }
    else{
    val localSystem = ActorSystem("LocalSystem",ConfigFactory.load("Local.conf"))
    val processors = Runtime.getRuntime().availableProcessors()
    for(i<- 0 to processors-1){
        val actorName="MiningActor"+i.toString()
        var actor = localSystem.actorOf(Props(new RemoteMiningActor(remoteHost)), name = actorName)
        actor!sendActorCount
        actor ! getNoOfZeroes
         
} 
   
  } 
  
  } 
   
   
}

class Master(zeroesExpected: Int, remoteHost:String) extends Actor {

var hashMap = Map.empty[String, String]
var totalMessages=0
var flag=false
var actorCount=0
var totalDuration: Long=0
var actorNo=0
var bitcoinCount=0
var testDuration: Int=300

def receive={
case `interrupt` =>
      {
    	  if(flag==false){  
    		  totalMessages+=1000000;
    		  sender ! execute
    	  }
    	  else
    	  {
    		  totalMessages+=1000000;
    		  sender ! terminate
    	  }
     
      }
    case bitcoin(input,output) =>
      {
       println("bitcoin:"+input+"\thash:"+ output)
       bitcoinCount+=1
      //println("Hello from MiningActor")
      hashMap+= (input->output)
      }
     
    case `done` => 
      				{ 
      				  actorNo+=1
      				 //val throughtput = (totalMessages * 1000.0 / totalDuration).toInt
      				 //println(s"== It took $totalDuration secs to deliver $totalMessages messages, throughtput $throughtput msg/s, " +
      						 	//s"payload size 10000")	
      				 //Thread.sleep(100)
      				  if(actorNo==actorCount){
      				    println(s"actorCount is $actorCount")
      				    println(s"Total Number of bitcoins mined is $bitcoinCount")
      				    println(s"Total number of messages processed is $totalMessages")
      				    //println(totalDuration)
      				    val throughtput = (totalMessages / testDuration)
      				    println(s"Throughtput is $throughtput messages/sec")
      				    context.system.shutdown()
      				  }
      				}
        
    case terminate(duration) => {
    								totalDuration=duration
    								flag=true
    							}	
    
    case `sendzeroes` => sender ! zeroesExpected
    
    case actorcount=> actorCount+=1
         
  }
}

  
  
 class MiningActor(remoteHost : String, masterRef : ActorRef,z : Int) extends Actor{
   
  var flag = true 
  var messageCount=0
  var hashMap = Map.empty[String, String]
  var counter=0
 
  def receive ={
    
      case `sendActorCount`=> masterRef!actorcount   
      case `execute` =>
        {
        			for(i<- 0 to 1000000){
        			val input = BearerTokenGenerator.getToken()
        			
        			val hashOutput = BearerTokenGenerator.generateSHAToken(input)
        			if (Common.isBitCoin(hashOutput,z)){
        				//hashMap+= (input->hashOutput)
        				masterRef! bitcoin(input,hashOutput)
        				//println("bitcoin:"+input+"\thash:"+ hashOutput)
        			}
        		}
        			masterRef ! interrupt
        }
     
     
    case `msg` =>
      println(s"Actor received message")
      sender ! rmsg
     
    case terminate =>
      {masterRef ! done
       context.stop(self)
      //context.system.shutdown()
      }
                  
   
  }
  
}

 class RemoteMiningActor(remoteHost : String) extends Actor{
   
  var flag = true 
  var messageCount=0
  val masterRef=context.actorSelection(s"akka.tcp://MasterSystem@$remoteHost:2552/user/Master")
  //println(remoteHost)
  var counter=0
  var zeroes=0
  def receive ={
    
	  case `sendActorCount`=> masterRef!actorcount
      case `execute` =>
        {
        			for(i<- 0 to 1000000){
        			val input = BearerTokenGenerator.getToken()
        			val hashOutput = BearerTokenGenerator.generateSHAToken(input)
        			if (Common.isBitCoin(hashOutput,zeroes)){
        				masterRef! bitcoin(input,hashOutput)
        			
        			}
        		}
        			masterRef ! interrupt
        }
     
     
    case `msg` =>
      println(s"Actor received message")
      sender ! rmsg
     
    case `terminate` =>
      			{	masterRef ! done
      				context.stop(self)
      //context.system.shutdown())))
      			}
   
    case `getNoOfZeroes` => masterRef ! sendzeroes        
    
    case z:Int => 
                  { zeroes=z
                    self!execute 
                  }
   
  }
  
}
 
 class Timer(masterRef : ActorRef) extends Actor{
	var startTime = System.currentTimeMillis()
	var flag=true
	while(flag==true)
	{
	  var currentTime = System.currentTimeMillis()
	  
	  if(currentTime >= startTime+300000)
	  {
	    flag=false
	    masterRef ! terminate(currentTime)
	  }
	}
	def receive={
	  case _=> println("Default")
	}
	
}
 
 object BearerTokenGenerator {
  
	val TOKEN_LENGTH = 45	// TOKEN_LENGTH is not the return size from a hash, 
				// but the total characters used as random token prior to hash
				// 45 was selected because System.nanoTime().toString returns
				// 19 characters.  45 + 19 = 64.  Therefore we are guaranteed
				// at least 64 characters (bytes) to use in hash, to avoid MD5 collision < 64
	val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.-"
	val secureRandom = new SecureRandom()

	private def toHex(bytes: Array[Byte]): String = bytes.map( "%02x".format(_) ).mkString("")

	private def sha(s: String): String = { 
	    toHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8")))
	}
	private def md5(s: String): String = { 
	    toHex(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")))
	}

	// use tail recursion, functional style to build string.
	private def generateToken(tokenLength: Int) : String = {
		val charLen = TOKEN_CHARS.length()
   		def generateTokenAccumulator(accumulator: String, number: Int) : String = {
	       	if (number == 0) return accumulator
	       	else
	           generateTokenAccumulator(accumulator + TOKEN_CHARS(secureRandom.nextInt(charLen)).toString, number - 1)
	   	}
   		generateTokenAccumulator("", tokenLength)
	}

	/*
	 *  Hash the Token to return a 32 or 64 character HEX String
	 *  
	 *  Parameters:
	 *  tokenprifix: string to concatenate with random generated token prior to HASH to improve uniqueness, such as username
     *
     *  Returns:
     *  MD5 hash of (username + current time + random token generator) as token, 128 bits, 32 characters
     * or
     *  SHA-256 hash of (username + current time + random token generator) as token, 256 bits, 64 characters
     */
	def generateMD5Token(tokenprefix: String): String =  {
		md5(tokenprefix + System.nanoTime() + generateToken(TOKEN_LENGTH)) 
	}
	/*def generateSHAToken(tokenprefix: String): String =  {
		sha(tokenprefix + System.nanoTime() + generateToken(TOKEN_LENGTH)) 
	}*/
	//getInput String
	def getToken(tokenprefix: String): String =  {
		return(tokenprefix + System.nanoTime() + generateToken(TOKEN_LENGTH)) 
	}
	def generateSHA256Token(tokenprefix: String): String =  {
		sha(tokenprefix) 
	}
	// final 2 methods we're using
	def getToken(): String =  {
		return("balajiiyer" + System.nanoTime()) 
	}
	
	def generateSHAToken(shaInput :String):String ={
	  sha(shaInput)
	}
}

object Common {
	def isBitCoin(hashValue: String , zeroesExpected : Int):Boolean = {
		var flag = true
		for (index <- 0 to (zeroesExpected-1))
	    {
	    		  if(hashValue.charAt(index)!='0')
	    		  {
	    		    flag= false
	    		    return flag
	    		  }
	    		  
	    }
	    	  return flag
		}
}

