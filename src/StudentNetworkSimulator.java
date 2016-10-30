public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(int entity, String dataSent)
     *       Passes "dataSent" up to layer 5 from "entity" [A or B]
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */


    int sequenceNumberA;                                                        //this is the global variable for sequenceNumberA
    String payloadA;                                                            //this is the global variable for payloadA 
    int lastSquenceNumber;                                                      //this is the global variable for lastSequenceNumber
    boolean isWaiting = false;                                                  //this is the global boolean for isWaiting, initialized to false 

    // This is the constructor.
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   long seed)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer. Return 1 if accepting the message to send, 
    // return 0 if refusing to send the message
    @Override
    protected int aOutput(Message message)
    {
        //checks if is waiting is true        
        if (isWaiting == true){                                                 //if it is waiting we do not send a new message.
            System.out.println("I'm waiting for a reponse to previous messsage");
            return 0;                                                           //this is done by returning 0 
        }
        
       
        if (isWaiting == false){                                                //A entity is not waiting
            sequenceNumberA ++;                                                 //imcrementing sequenceNumber 
            payloadA = message.getData();                                       //payload = message contents 
            int checkSumA = checkSumFunction (payloadA);                        //checksum a = to message contents
            //System.out.println("Checksum = "+ checkSumA);
            Packet p = new Packet(sequenceNumberA,0,checkSumA,message.getData());//creating a new packet with a sequence number , no ack checksumA value and the mesage
            toLayer3(A,p);                                                      //sends packet to layer 3 as A entity   
            System.out.println("Sending message");
            
            isWaiting = true;                                                   //after packet sent change is waiting value
            startTimer(A,500);                                                 //starting the timer A for 1 second so aTimer intrerpt will handle delay or loss of packets
            
            
        }
                
        return 1;
    }
    
    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)      
    {
        
        
           String payload = packet.getPayload();                                //assign payload to a variable for checksum
           int sequenceNumberB = packet.getSeqnum();                            //getting sequence number from packet
           int checkSumA = packet.getChecksum();                                //getting checksum value from packet
           int checkSumB = checkSumFunction (payload);                          //preforming checksum function on payload  
           
           
           toLayer5(B,new Message(packet.getPayload()));                        //b gets payload to display message in layer 5
           System.out.println("Checksum = "+ checkSumB);
           System.out.println("Sequence Number = " + sequenceNumberB);

           //checksum comparison
           if (checkSumB == checkSumA && sequenceNumberA == sequenceNumberB){   //if the checksums match && sequenceNumbers match execute this code

                System.out.println("Message ok sending back a ack");
                Packet p = new Packet(sequenceNumberB,1,0);                     //create a new packet with sequenceNumberb, and ack value of 1(ack)and no checksum                            
                toLayer3(B,p);                                                  //sends packet to layer 3 as B entity  
                //startTimer(1, 100);                                           //timer would be necessary if B had a timer
                lastSquenceNumber = sequenceNumberB;                            //lastSequence number now equals the lastest sequence number from a sucessful packet

            }

            else {

                Packet p = new Packet(sequenceNumberB,0,0);                     //create a new packet with sequenceNumberb, and ack value of 0(nack)and no checksum 
                System.out.println("Message Corrupted sending back a nack");
                lastSquenceNumber = 0;                                          //reset last sequence number to 0 meaning that the failed packet will not current the order
                toLayer3(B,p);                                                  //sends packet to layer 3 as B entity  
            }

           
    }
    
    
    protected void aInput(Packet packet)
    {
        
    	int ack = packet.getAcknum();                                           //gets ack value from packet
        int sequenceNumber = packet.getSeqnum();                                //gets sequenceNumber value from packet
        stopTimer(A);                                                           //stopping a timer because message got a response
        
        //switch statement based of ack value
        switch (ack) {
            case 1:                                                             //if ack is 1 execute this case
                System.out.println("I got a ack");
                if (sequenceNumber !=  sequenceNumberA){                        //check if sequence numbers don't match (got corrupted)
                    System.out.println("But sequence number was corrupted");    
                    System.out.println("Resending Message");
                    resendMessage();                                            //execute resend function
                    startTimer(A, 500);                                        //starts timer so aTimer intrerpt will handle delay or loss of packets
                }
                else{
                    System.out.println("Packet " + sequenceNumber +" is ok");
                    isWaiting = false;                                          //aOutput is not longer waiting for an ack response its ok to send a new message 
                }
                break;
            case 0:                                                             //if ack value is 0 execute this case because you receives a nack
                System.out.println("I got a nack");
                resendMessage();                                                //execute resendMessage function 
                startTimer(A, 500);                                             //starts timer so aTimer intrerpt will handle delay or loss of packets
                break;
            default:                                                            //if ack is any other number execute this case
                System.out.println("Ack got corrupted");
                resendMessage();                                                //execute resendMessage function 
                startTimer(A, 500);                                            //starts timer so aTimer intrerpt will handle delay or loss of packets
                break;
        }

    }
    
    //called when timer elapses
    protected void aTimerInterrupt()
    {
        //timer stops automathically
        System.out.println("I've been waiting too long");
        System.out.println("Packet " + sequenceNumberA + " was too slow");      //was used for debugging
        System.out.println("Resending Packet " + sequenceNumberA);
        
        Packet p = new Packet(sequenceNumberA,0,                                //new packet is created with sequenceA, no ack
                              checkSumFunction(payloadA),payloadA);             //checksum value is callled again taking is payloadA and the message itself is sent 
        toLayer3(A,p);                                                          //sent to layer 3 A with package
        startTimer(A, 500);                                                    //started the timer for future timer interrupt
             
    }
        
    protected void aInit()
    {

    }

    protected void bInit()
    {
        
    }
    
    //checksumFunction
    public static int checkSumFunction (String payload){                        //returns and int & takes a String parms payload
        int checksum = 0;                                                       
          for (int i = 0; i < payload.length(); i ++){                          //for loop which goes through the lenght of the passed in string
                 checksum += payload.charAt(i);                                 //checkums gets the ascii value of that character added to it
        }
        return checksum;                                                        //once the all the characters have been added to checksum returns that value
    }
    
    //resendMessage function
    public void resendMessage (){
        String payload = payloadA;                                              //assign payload to a variable for checksum
        int checkSumA = checkSumFunction (payload);                             //checksum a = to message contents
        System.out.println("Checksum = "+ checkSumA);

        Packet p = new Packet(sequenceNumberA,0,checkSumA,payload);             //creating a new packet with a sequence number , no ack checksumA value and the mesage
        toLayer3(A,p);                                                          //sends packet to layer 3 as A entity   
        System.out.println("Sending message");
    }
}
