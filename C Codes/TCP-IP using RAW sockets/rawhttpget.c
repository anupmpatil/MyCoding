#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <string.h> 
#include <sys/socket.h> 
#include <errno.h> 
#include <netinet/tcp.h>   
#include <netinet/ip.h>    
#include <netinet/in.h>
#include <netdb.h> 
#include <math.h>
#include <ifaddrs.h>
#include <arpa/inet.h>


static long int firstSeqNo = 0;
static long int firstAckNo = 0;
static int httpOkFound = 0;
static int iteration = 0;
static int writtenBytes = 0;
//FILE* fd = NULL;
char toWrite[10000000];
char ipaddress[100];
int threeWayHandshakeCompleted = 0;
char source_ip[32];
in_addr_t destinationIpAddress;
in_addr_t SourceIpAddress;
char httpRequestAddress[200];
char httpHostName[200];
int nextSeqNumber = 0;
int nextAckNumber = 0;
struct sockaddr_in sin_address;
char fileName[100];
int portNumber = 1234;
long int firstSeq = 0;
int validPort = 0;
int destPort = 80;
int mod = 6565656;

void getTcpHeader(struct tcphdr *, int, int, int, int, int, int, int);
void getIpHeader(struct iphdr *ipheader, int headerLength, int version, int typeOfService,
                 int totalLangth, int packetId, int fragOffset, int ttl, int protocol,
                 int checkSum, in_addr_t sourceIP, in_addr_t destIp);
struct dummyHeader getPsudoHeader(in_addr_t sourceIP, in_addr_t destIp, 
                                    int placeHolder, int protocol, int tcpLength);

int parsePacket(unsigned char *data, int length);
void parseHTTPData (char *httpData);
void writeContentsToFile(char *htmlContent);
in_addr_t getLocalIpAddress(void);
int sendFinishPacket(int, int, int);
void getHttpRequestDetails(char[]);	
int getPortNumber();
int initializePortNumberAndIP();
unsigned short calculateCheckSum (unsigned short *ptr, int bytenum);

int initializePortNumberAndIP()
{

	int retval = -1;
	int port = getPortNumber();
	SourceIpAddress = getLocalIpAddress();
	int iSetOption = 1;

	struct sockaddr_in self_address;
	int server_sockfd = socket(AF_INET, SOCK_STREAM, 0);
	setsockopt(server_sockfd, SOL_SOCKET, SO_REUSEADDR, (char*)&iSetOption, sizeof(iSetOption));
	memset(&self_address, 0, sizeof(struct sockaddr_in));
	self_address.sin_family = AF_INET;
	self_address.sin_addr.s_addr = SourceIpAddress;
	self_address.sin_port = htons(port);

    //printf("\n GENERATED RANDOM PORT = %d\n", port);
    //printf("\n GENERATED RANDOM PORT = %d\n", htons(port));

	if((bind(server_sockfd, (struct sockaddr*)&self_address, sizeof(self_address))) < 0)
	{
	        printf("\n ERROR Binding, may be this port is not available \n trying again for another port ...\n");
	        return -1;
	}

	close(server_sockfd);
    retval = ntohs(self_address.sin_port);     
    //printf("\n GENERATED RANDOM PORT = %d\n", retval);   


    return retval;
    //sleep(10);

}
int getPortNumber()
{

  int retval = 0;
  retval = rand() % 65536;
  return retval;
} 


struct dummyHeader
{
    u_int32_t saddr;
    u_int32_t daddr;
    u_int8_t pholder;
    u_int8_t protocol;
    u_int16_t tcp_length;
};


int main (int argc, char *argv[])
{
        char url[200]; 

////////////////////////////// FETCH command line arguments //////////////////////////////////////////////////// 
    //printf("\n argc = %d \n", argc);
    if(argc == 4)
    {
       portNumber = atoi(argv[3]);
    }

    else if(argc != 2)
    {
       printf("\n no url provided \n");
        exit(0);           
    }
    strcpy(url, argv[1]);
////////////////////////////// FETCH host name and path from input //////////////////////////////////////////////
 
    getHttpRequestDetails(url);
            
    //printf("\n reached here \n");

////////////////////////////////////// Fetch random port number ////////////////////////////////////////////////////////

    int fetchPortAttempts = 0;
    while((validPort != 1) || (fetchPortAttempts != 100))
    {
      portNumber = initializePortNumberAndIP();
      //printf("\n GENERATED PORT NUMBER ====== %d", portNumber);
      if(portNumber != -1)
      {
         validPort = 1;
         break;
      }
      fetchPortAttempts++;
    }

   if ((fetchPortAttempts == 100) || (validPort != 1))
   {
 	printf("ERROR: can not fetch valid port");
        exit(0);
   }

////////////////////////////////////////////////////////////////////////////////////////////////
// initialize first seq no
//firstSeq = rand() % mod;
///////////////////////////////////////////////////////////////////////////////////////////////

//Raw socket creation
	int s = socket (PF_INET, SOCK_RAW, IPPROTO_TCP);	     
	if(s == -1)
	{
	    perror("Failed to create socket");
	    exit(1);
	}
	struct timeval timeout;

	/* 60 Secs Timeout. If receive does not read anything for this time close connection*/
	timeout.tv_sec = 60;  
	timeout.tv_usec = 0; 

    setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(struct timeval));
	     
	//packet represented as datagram
	char datagram[8096] , *data, *pseudogram;
	char buffer[4096];
    char reply1[1500];
	char datagram2[4096], *data2;
            
	     
	//zero out the packet buffer
	memset (datagram, 0, 4096);
    memset (datagram2, 0, 4096);
	     
	//IP header
	struct iphdr *iph = (struct iphdr *) datagram;
    struct iphdr *iph1 = (struct iphdr *) datagram2;
	     
	//TCP header
	struct tcphdr *tcph = (struct tcphdr *) (datagram + sizeof (struct ip));
	getTcpHeader(tcph, firstSeq, 0, 1, 0, 0, 0, 0);
	    
    struct sockaddr_in sin;


/////////////////////////// Fetch server address /////////////////////////////////////////////	    
	struct hostent *server;

    // get server address
	server = gethostbyname(httpHostName);
	if (server == NULL) 
    {
        printf("ERROR, host does not exists .... \n");
	    exit(0);
    }
	     
    // use fetched server address
	sin.sin_family = AF_INET;
    bzero((char *) &sin, sizeof(sin));
	sin.sin_family = AF_INET;
	bcopy((char *)server->h_addr, (char *)&sin.sin_addr.s_addr, server->h_length);
    // sin.sin_addr.s_addr = inet_addr("129.10.165.157");
	sin.sin_port = htons(destPort);
	// sin.sin_port = htons(80);
    // for later use
    sin_address = sin;


	// Data Starts from this address
	data = datagram + sizeof(struct iphdr) + sizeof(struct tcphdr);
	int totalPacketLength = sizeof (struct iphdr) + sizeof (struct tcphdr) + strlen(data);
    // generate Ip Header
	getIpHeader(iph, 5, 4, 0, totalPacketLength, 41706, 0, 225, 
                    IPPROTO_TCP, 0, SourceIpAddress, sin.sin_addr.s_addr);
    // for later use
    destinationIpAddress = sin.sin_addr.s_addr;
//////////////////////////////////////////////////////////////////////////////////////////////////////////

	     
	//Ip checksum
	iph->check = calculateCheckSum ((unsigned short *) datagram, iph->tot_len);
	     
	     
	//Now the TCP checksum

    int tcp_length = sizeof(struct tcphdr) + strlen(data);
    struct dummyHeader psh;
    psh = getPsudoHeader(SourceIpAddress, sin.sin_addr.s_addr, 0, IPPROTO_TCP, tcp_length);
	     
	int psize = sizeof(struct dummyHeader) + sizeof(struct tcphdr) + strlen(data);
	pseudogram = malloc(psize);
	     
	memcpy(pseudogram, (char*) &psh, sizeof (struct dummyHeader));
	memcpy(pseudogram + sizeof(struct dummyHeader), tcph, sizeof(struct tcphdr) + strlen(data));

    //printf("\n hdr size %i", sizeof(pseudogram));	     
	tcph->check = calculateCheckSum((unsigned short*) pseudogram , psize);
            
	     
	//set IP_HDRINCL flag to tell the kernel that headers are included in the packet
	int var = 1;
	const int *val = &var;
	     
	if (setsockopt (s, IPPROTO_IP, IP_HDRINCL, val, sizeof (var)) < 0)
	{
	    perror("Error setting IP_HDRINCL");
	    exit(0);
	}

    // START 3 WAY HANDSHAKE WITH SYN
    // Now Sending the packet
	if (sendto (s, datagram, iph->tot_len ,  0, (struct sockaddr *) &sin, sizeof (sin)) < 0)
	{
	   printf("sendto failed");
	}
	//Data send successfully
	else
	{
		//printf ("Packet Sent. Length : %d \n" , iph->tot_len);
	}
           
 

    while(1) 
    {
       int retval = 1;
       recvfrom(s, reply1, sizeof(reply1), 0,(struct sockaddr *) &sin, (socklen_t *)sizeof (sin));
       if (strlen(reply1) == 0)
	   {
          printf("\n receive timeout .... server did not respond ....closing connection\n");
          exit(0);
       }
       retval = parsePacket(reply1, sizeof(reply1));
       bzero(reply1, sizeof(reply1));
       if (retval == -2)
		{
            threeWayHandshakeCompleted = 1;
            //printf("\n --------- REACHED HERE -----------\n");
            break;  
        }
    }

    close(s);


    // THIRD STEP IN 3 WAY HANDSHAKE
    int s1 = socket (PF_INET, SOCK_RAW, IPPROTO_TCP);
	setsockopt(s1, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(struct timeval));
    struct tcphdr *tcph1 = (struct tcphdr *) (datagram2 + sizeof (struct ip));
    data2 = datagram2 + sizeof(struct iphdr) + sizeof(struct tcphdr);

    strcpy(buffer, "GET ");
    strcat(buffer, httpRequestAddress);
    strcat(buffer, " HTTP/1.1\r\n");
    strcat(buffer, "Host: ");
    strcat(buffer, httpHostName);
    strcat(buffer, "\r\n");

    strcat(buffer, "Connection: keep-alive\r\n");
    strcat(buffer, "Cache-Control: max-age=0\r\n");
    strcat(buffer, "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
    strcat(buffer, "User-Agent: Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17\r\n");
    strcat(buffer, "Accept-Encoding: identity\r\n");
    strcat(buffer, "Accept-Language: en-US,en;q=0.8\r\n");
    strcat(buffer, "Accept-Chalrset: ISO-8859-1,utf-8;q=0.7,*;q=0.3\r\n");
    strcat(buffer, "\r\n");   
	strcpy(data2 , buffer);
    //printf("\n HTTP: request buffer \n %s", data2);
 

	if(s1 == -1)
	{
		//socket creation failed, may be because of non-root privileges
		perror("Failed to create socket");
	    exit(1);
	}


    //Fill in the IP Header
    totalPacketLength = sizeof (struct iphdr) + sizeof (struct tcphdr) + strlen(data2);
    getIpHeader(iph1, 5, 4, 0, totalPacketLength, 41707, 0, 225, 
                    IPPROTO_TCP, 0, SourceIpAddress, sin.sin_addr.s_addr);
	    
    
	//Ip checksum
	iph1->check = calculateCheckSum ((unsigned short *) datagram2, iph1->tot_len);


	//TCP Header
    getTcpHeader(tcph1, firstAckNo, firstSeqNo, 0, 0, 1, 1, 0);

    //printf("\n tcp seq no %d \n", tcph1->ack_seq);
	     
	//Now the TCP checksum

    //bzero(pseudogram, sizeof(pseudogram));
    tcp_length = sizeof(struct tcphdr) + strlen(data2);
	psh = getPsudoHeader(SourceIpAddress, sin.sin_addr.s_addr, 0, IPPROTO_TCP, tcp_length);
	psize = sizeof(struct dummyHeader) + sizeof(struct tcphdr) + strlen(data2);

	     
	memcpy(pseudogram , (char*) &psh , sizeof (struct dummyHeader));
	memcpy(pseudogram + sizeof(struct dummyHeader) , tcph1 , sizeof(struct tcphdr) + strlen(data2));
	     
	tcph1->check = calculateCheckSum((unsigned short*) pseudogram, psize);

	     
	//IP_HDRINCL to tell the kernel that headers are included in the packet
	int two = 2;
	const int *val1 = &two;
	     
	if (setsockopt (s1, IPPROTO_IP, IP_HDRINCL, val1, sizeof (two)) < 0)
	{
	    perror("Error setting IP_HDRINCL");
	    exit(0);
	}

    //printf("\n iph-len %d \n", iph1->tot_len);
	     

    //Send the packet
	if (sendto (s1, datagram2, iph1->tot_len ,  0, (struct sockaddr *) &sin, sizeof (sin)) < 0)
    {
	    printf("sendto failed");
	}
	//Data send successfully
	else
	{
		//printf ("Packet Send. Length : %d \n" , iph1->tot_len);
	}


    while(1) 
    {
        int retval = 1;
        recvfrom(s1, reply1, sizeof(reply1), 0,(struct sockaddr *) &sin, (socklen_t *)sizeof (sin));
        if (strlen(reply1) == 0)
        {
            printf("\n receive timeout .... server did not respond ....closing connection\n");
            exit(0);
        }
        retval = parsePacket(reply1, sizeof(reply1));
        if (retval == -3)
        {
            break;
        }
              
        int currentSock = sendFinishPacket(0,1,0);
        close(currentSock);
        bzero(reply1, sizeof(reply1));
        if (retval == -1)
        {
            break;  
        }
    }

	close(s1);

    int sockId = sendFinishPacket(1,1,0);
    {
        int retval = 1;
        recvfrom(sockId, reply1, sizeof(reply1), 0,(struct sockaddr *) &sin, (socklen_t *)sizeof (sin));
        if (strlen(reply1) == 0)
        {
            printf("\n receive timeout .... server did not respond ....closing connection\n");
            exit(0);
        }
        retval = parsePacket(reply1, sizeof(reply1));
        bzero(reply1, sizeof(reply1));
	    recvfrom(sockId, reply1, sizeof(reply1), 0,(struct sockaddr *) &sin, (socklen_t *)sizeof (sin));
        if (strlen(reply1) == 0)
        {
            printf("\n receive timeout .... server did not respond ....closing connection\n");
            exit(0);
        }
        retval = parsePacket(reply1, sizeof(reply1));
        bzero(reply1, sizeof(reply1));
    }
    close(sockId);
    int finishSockId = sendFinishPacket(0,1,1);
    close(finishSockId);

    writeContentsToFile(toWrite);

    return 0;
}
	 

// Parse incoming packet
int parsePacket(unsigned char *data, int length)
{

	int proto = (int)data[9];
	if (proto != 6)
	{
		printf("\n Non TCP Packet Dropped \n");
		return 0;
	}

	char sender_ip[32];

  /*printf("-----------------Packet Begins-----------------\n");
	printf("IP Version: %i, Packet Size: %i bytes, Id: %i\n",
	(data[0]>>4), (data[2]*256)+data[3], (data[4]*256)+data[5]);

	printf("Header length %i \n",(data[0] & (0x0F)));*/

	int ipPacketTotalLength = ((data[2]<<8) + data[3]);
	int dataLength = ipPacketTotalLength - 40;
  /*printf("Packet total length: %i", ipPacketTotalLength);

	printf("Fragment: %i, TTL: %i, HL: %iwds, Protocol: %i\n",
	((int)(data[6]>>4)*256)+data[7], data[8], ((char)(data[0]<<4))>>4, data[9]);*/

	int totalPacketWords = ((int) (((data[2]<<8) + data[3])/32)) + 1;

  //printf("Total packet size in words: %d \n", totalPacketWords);
  /*
	printf("Source: %i.%i.%i.%i, Destination: %i.%i.%i.%i\n",
	data[12], data[13], data[14], data[15],
	data[16], data[17], data[18], data[19]);*/

	char num[5];

	sprintf(num,"%d",data[12]);
	strcpy(sender_ip,num);
	strcat(sender_ip,".");

	sprintf(num,"%d",data[13]);
	strcat(sender_ip,num);
	strcat(sender_ip,".");

	sprintf(num,"%d",data[14]);
	strcat(sender_ip,num);
	strcat(sender_ip,".");

	sprintf(num,"%d",data[15]);
	strcat(sender_ip,num);
	strcat(sender_ip,"\0");

	//printf("\n ------SENDER IP -------%s\n", sender_ip);

	in_addr_t sender_ip_generated;
	sender_ip_generated = inet_addr (sender_ip);



	if (threeWayHandshakeCompleted == 0)
	{
        int synFlagOn = 0;
        int ackFlagOn = 0;
		if (sender_ip_generated != destinationIpAddress)
		{
			printf("\n-----------IP DOES NOT MATCH !!! PACKET DISCARDED !!!-------\n");
			return 0;
		}
		else
		{

		  /*printf("\n ------DATA[33] = %i --------\n",data[33]);
			printf("\n ------DATA[33] & 2 = %i --------\n",data[33]&2);
			printf("\n ------DATA[33] & 16= %i --------\n",data[33]&16);*/
			if ((data[33] & 2) == 2)
			{
				synFlagOn = 1;
			}
            if ((data[33] & 16) == 16)
            {
                ackFlagOn = 1;
            } 
            if (synFlagOn && ackFlagOn)
            {
				firstSeqNo = ((data[24]*((int)pow(2,24))) + (data[25]*((int)pow(2,16))) + (data[26]*((int)pow(2,8))) + (data[27])) + 1;
				firstAckNo = ((data[28]*((int)pow(2,24))) + (data[29]*((int)pow(2,16))) + (data[30]*((int)pow(2,8))) + (data[31]));
			}
            else
            {
				printf("\n---- SERVER DID NOT SEND SYN-ACK----\n---- PROBLEM IN 3-WAY-HANDSHAKE-----\n -----TRY RUNNING PROGRAM AGAIN-----\n ------ YOU MAY TRY CHANGING PORT NO WITH -p OPTION");
				exit(0);
            }
			int firstAck = ((data[28]*((int)pow(2,24))) + (data[29]*((int)pow(2,16))) + (data[30]*((int)pow(2,8))) + (data[31]));
			if((firstSeq + 1) != firstAck)
			{
				printf("\n---- Wrong ack in 3 way handshake----\n---- PROBLEM IN 3-WAY-HANDSHAKE-----\n -----TRY RUNNING PROGRAM AGAIN-----\n");
				exit(0);
			}
			return -2;
		}
	}


	//printf("tcpheader source port: %i, \n", (data[20]*256)+data[21]);
	//printf("tcpheader destination port: %i, \n", (data[22]*256)+data[23]);


  /*
	printf("data[24]: %i \n", data[24]);
	printf("data[25]: %i \n", data[25]);
	printf("data[26]: %i \n", data[26]);
	printf("data[27]: %i \n", data[27]);

	printf("data[28]: %i \n", data[28]);
	printf("data[29]: %i \n", data[29]);
	printf("data[30]: %i \n", data[30]);
	printf("data[31]: %i \n", data[31]);*/

	firstSeqNo = ((data[24]*((int)pow(2,24))) + (data[25]*((int)pow(2,16))) + (data[26]*((int)pow(2,8))) + (data[27])) + 1;
	firstAckNo = ((data[28]*((int)pow(2,24))) + (data[29]*((int)pow(2,16))) + (data[30]*((int)pow(2,8))) + (data[31]));


	//printf("sequence number: %i\n",(data[24]*((int)pow(2,24))) + (data[25]*((int)pow(2,16))) + (data[26]*((int)pow(2,8))) + (data[27]));
	//printf("Ack number: %i\n",(data[28]*((int)pow(2,24))) + (data[29]*((int)pow(2,16))) + (data[30]*((int)pow(2,8))) + (data[31]));

	nextSeqNumber = firstAckNo;
	nextAckNumber = firstSeqNo - 1 + dataLength;

	//printf("\n------------------Packet Ends------------------\n");

	//printf("\n-----------------Data In Packet----------------- \n");

	int dataOffset = data[32] >> 4;

	//printf("data offset from TCP header: %d \n", dataOffset);
	//printf("data[33] = %i\n", data[33]);

  /*
	if (data[33] & 8 == 8)
	{
        int dataStart = totalPacketWords - (iphl + dataOffset);
		printf("data start %i \n", dataStart);
	}*/

////////////////////////////////////// OPERATION ON DATA /////////////////////////////////////////////
	if(threeWayHandshakeCompleted == 1)
	{
		if (ipPacketTotalLength > 40)
		{
			char *http = &data[40];

			char lines [5000][1000];
			int count = 0;

			char httpcontent[1500];
			char initialHtmlBody[1500];
			char parseCont[5000];

			strcpy(parseCont, http);
			strncpy(httpcontent, parseCont, 1460);
			strcpy(initialHtmlBody, httpcontent);

			strcat(parseCont, "\0");
			//printf("\n HTTP DATA length %d \n", strlen(httpcontent));
			//printf("\n HTTP DATA %s \n", httpcontent);

        

			char *token = NULL;
			token = strtok(httpcontent, "\r\n");

			if((strcmp(token, "HTTP/1.1 404 NOT FOUND")) == 0)
			{
				printf("\n --------- HTTP/1.1 404 NOT FOUND ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.1 400 Bad Request")) == 0)
			{
				printf("\n --------- HTTP/1.1 400 Bad Request ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.1 301 Moved Permanently")) == 0)
			{
				printf("\n --------- HTTP/1.1 301 Moved Permanently ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.1 404 Not Found")) == 0)
			{
				printf("\n --------- HTTP/1.1 404 NOT FOUND ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.1 400 BAD REQUEST")) == 0)
			{
				printf("\n --------- HTTP/1.1 400 Bad Request ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.1 301 MOVED PERMANENTLY")) == 0)
			{
				printf("\n --------- HTTP/1.1 301 Moved Permanently ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 404 NOT FOUND")) == 0)
			{
				printf("\n --------- HTTP/1.0 404 NOT FOUND ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 400 Bad Request")) == 0)
			{
				printf("\n --------- HTTP/1.0 400 Bad Request ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 301 Moved Permanently")) == 0)
			{
				printf("\n --------- HTTP/1.0 301 Moved Permanently ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 404 Not Found")) == 0)
			{
				printf("\n --------- HTTP/1.0 404 NOT FOUND ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 400 BAD REQUEST")) == 0)
			{
				printf("\n --------- HTTP/1.0 400 Bad Request ----------------\n");
				return -3;
			}

			if((strcmp(token, "HTTP/1.0 301 MOVED PERMANENTLY")) == 0)
			{
				printf("\n --------- HTTP/1.0 301 Moved Permanently ----------------\n");
				return -3;
			}

			if(((strcmp(token, "HTTP/1.1 200 OK")) == 0) || ((strcmp(token, "HTTP/1.0 200 OK")) == 0))
			{
				httpOkFound = 1;
                iteration = 1;
				while (token != NULL) 
				{
					strcpy(lines[count], token);
					token = strtok(NULL, "\r\n");
					count ++;
				}
			}

        // READ DATA FROM PACKET
		if(httpOkFound)
		{
			char htmlStart[10] = "</html>";
			char * retVal;
			retVal = strstr(initialHtmlBody,htmlStart);
			if(retVal == NULL)
			{
				char htmlStart[10] = "<html";
				char *htmlBody;
				if(iteration == 1)
				{
					htmlBody = strstr(initialHtmlBody, htmlStart);
					//printf("\n --------------- THIS PRESENT HERE HTTP CONTENT-------------\n %s \n",htmlBody);
					//printf("\n SIZE = %d \n", strlen(htmlBody));
             

					strcpy(toWrite,htmlBody);
					//printf("\n ---------- Written Bytes BEFORE--------- %d \n", writtenBytes);
					writtenBytes = strlen(htmlBody);
					//printf("\n ---------- Written Bytes --------- %d \n", writtenBytes);
					iteration = 0;
				}
				else
				{
					//printf("\n --------------- HTTP CONTENT-------------\n %s \n",initialHtmlBody);
					//printf("\n SIZE = %d \n", strlen(initialHtmlBody));
					strcat(toWrite,initialHtmlBody);
					writtenBytes = writtenBytes + strlen(initialHtmlBody);
					//printf("\n ---------- Written Bytes --------- %d \n", writtenBytes);
				}
			}
			else
			{
				char htmlStart[10] = "<html";
				char *htmlBody;
				htmlBody = strstr(initialHtmlBody, htmlStart);
				if (htmlBody == NULL)
				{
					//printf("\n SIZE = %d \n", strlen(initialHtmlBody));
					strcat(toWrite,initialHtmlBody);
					writtenBytes = writtenBytes + strlen(initialHtmlBody);
					//printf("\n ---------- Written Bytes --------- %d \n", writtenBytes);
				}
				else
				{
					strcpy(toWrite,htmlBody);
					writtenBytes = strlen(htmlBody);
				}
				return -1;
			}
		} 
      }
    }
     return 0;
}



	 
	
///////////  checksum calculation function //////////////

unsigned short calculateCheckSum (unsigned short *ptr, int bytenum)
{
    register long sum;
    unsigned short oddbyte;
    register short resultCSum;

    sum=0;
    // add each 16 bits
    while (bytenum > 1) 
    {
        sum = sum + *ptr++;
        bytenum -= 2;
    }
 
    // for odd number of bytes case
    if (bytenum == 1) 
    {
        oddbyte = 0;
        *((u_char*)&oddbyte) = *(u_char*)ptr;
        sum = sum + oddbyte;
    }
	 
    sum = (sum >> 16) + (sum & 0xffff);
    sum = sum + (sum >> 16);
    // take one's cpmplement of sum
    resultCSum = (short)~sum;
	     
    return resultCSum;
}


void getTcpHeader(struct tcphdr * tcpheader, int seq, int ack_seq, int syn_flag, 
                  int fin_flag, int push_flag, int ack_flag, int reset_flag)
{

	tcpheader->source = htons (portNumber);
    tcpheader->dest = htons (destPort);
    //tcpheader->dest = htons (80);
	tcpheader->seq = htonl(seq);
	tcpheader->ack_seq = htonl(ack_seq);
	tcpheader->doff = 5;  
	tcpheader->fin = fin_flag;
	tcpheader->syn = syn_flag;
	tcpheader->rst = reset_flag;
	tcpheader->psh = push_flag;
	tcpheader->ack = ack_flag;
	tcpheader->urg = 0;
	tcpheader->window = htons (5840); 
	tcpheader->check = 0; 
	tcpheader->urg_ptr = 0;
}


void getIpHeader(struct iphdr * ipheader, int headerLength, int version, int typeOfService,
                 int totalLangth, int packetId, int fragOffset, int ttl, int protocol,
                 int checkSum, in_addr_t sourceIp, in_addr_t destIp)
{

	ipheader->ihl = headerLength;
	ipheader->version = version;
	ipheader->tos = typeOfService;
	ipheader->tot_len = totalLangth;
	ipheader->id = htonl (packetId);
	ipheader->frag_off = fragOffset;
	ipheader->ttl = ttl;
	ipheader->protocol = protocol;
	ipheader->check = checkSum;
	ipheader->saddr = sourceIp;
	ipheader->daddr = destIp;
}

struct dummyHeader getPsudoHeader(in_addr_t sourceIP, in_addr_t destIp, int placeHolder, int protocol, int tcpLength)
{
    struct dummyHeader psudoHeader;
	psudoHeader.saddr = sourceIP;
	psudoHeader.daddr = destIp;
	psudoHeader.pholder = placeHolder;
	psudoHeader.protocol = protocol;
	psudoHeader.tcp_length = htons(tcpLength);

    return psudoHeader;
}

void writeContentsToFile(char *htmlContent)
{
	FILE* fd = NULL;
	
    fd = fopen(fileName,"w");
        
    if(NULL == fd)
    {
		printf("\n Error Opening file!!!\n");
		exit(0);
    }
	
    fwrite(htmlContent, strlen(htmlContent), 1, fd) ;

    //fclose(fd);
}


in_addr_t getLocalIpAddress()
{
	in_addr_t localIpAddress;
	char fetchedIpAddress[200];

	struct ifaddrs *interface;
	struct ifaddrs *interface_list;



	if (getifaddrs (&interface_list) < 0) 
	{
		printf("\n ERROR GETTING LOCAL IP .... EXIT\n");
        exit(0);
	}

	for (interface = interface_list; interface; interface = interface->ifa_next) 
	{
        int address_family = interface->ifa_addr->sa_family;
        const void *address;
		char addr_char[INET6_ADDRSTRLEN];

        switch (address_family) 
        {
            case AF_INET:
                address = &((struct sockaddr_in *)interface->ifa_addr)->sin_addr;
                break;
            default:
                address = NULL;
        }

        if (address) 
        {
            if (inet_ntop(address_family, address, addr_char, sizeof addr_char) == NULL) 
            {
                printf("inet_ntop");
                continue;
            }

            //printf("Interface %s has address %s\n", interface->ifa_name, addr_char);
            if((strcmp(interface->ifa_name, "eth0")) == 0)
            {
                 strcpy(fetchedIpAddress, addr_char);
            }
        }
    }

	// freeifaddrs(interface_list);
	if(fetchedIpAddress == NULL)
	{
       printf("\n ADDRESS OF eth0 NOT FOUND .... EXITING !!!");
       exit(0);
	}

	//printf("\n ADDRESS OF eth0 %s\n", fetchedIpAddress);
	localIpAddress = inet_addr(fetchedIpAddress);
	return localIpAddress;
}


void getHttpRequestDetails(char input[])
{
    // extracting host and address from command line argument
    // host contains host
    // address contains get path
	
	//printf("\n in function reached here \n");
	int memorySize = strlen(input) + 100;
    char  *url = (char *)malloc(memorySize);
    char  *urlCopy = (char *)malloc(memorySize);
    char  *fileNamePointer = (char *)malloc(memorySize);
    char  *fileNameExtractor;
    char  *flag3;
    char url1[40][100];
    char *flag2;
    char host[200];
    char address[200];
    char searchScheme[5];
    strcpy(searchScheme, "://");
    char *schemeFound;

    strcpy(url, input);
    strcpy(urlCopy, input);
    strcpy(fileNamePointer, input);


	////// Extract file name ///////////////////
	fileNameExtractor = strstr(fileNamePointer, ".html");
    int k = 0;
	if (fileNameExtractor == NULL)
	{ 
      	strcpy(fileName,"index.html");
       	//printf("\n %s \n", fileName);
	} 
         
    else
    {
		flag3 = strtok(fileNamePointer,"/");
       	//printf("\n %s \n", flag3);               
       	while(flag3 != NULL)
       	{
			flag3 = strtok(NULL,"/");
       	    if(flag3 != NULL)
            {
				strcpy(url1[k], flag3);
	            //printf("\n filename[%d] - %s \n", k, url1[k]);
           		k++;
          	}
        }
	    //printf("\n %s \n", url1[k-1]);
		strcpy(fileName,url1[k-1]);

    }
	//printf("\n ********************** %s ******************\n", fileName);

    //printf("\n ------- urlCopy -------- %s \n",urlCopy);

	/////////////// Extract file host and path ///////////////////
    schemeFound = strstr(url, searchScheme);
    if (schemeFound != NULL)
    {
        int i = 0;
        flag2 = strtok(url,searchScheme);
        //printf("\n FOUND SCHEME: %s \n", flag2);
                   
        while(i != 1)
        {
			flag2 = strtok(NULL,"/");
            //printf("\n FOUND HOST: %s \n", flag2);
            strcpy(host, flag2);
            i++;
        }
        char *pathStart;
        // strcpy(urlCopy, input);
        //printf("\n ------- urlCopy -------- %s \n",urlCopy);
        pathStart = strstr(urlCopy,host);
        //printf("\n ------- urlCopy -------- %s \n",urlCopy);
        //printf("\n pathstart %s \n", pathStart);
        pathStart = pathStart + strlen(host);
        strcpy(address,pathStart);
        //printf("\n FOUND PATH: %s \n", address);
    }
    else
    {
        flag2 = strtok(url,"/");
        //printf("\n FOUND HOST: %s \n", flag2);
        strcpy(host, flag2);
        //printf("\n FOUND SCHEME: %s \n", flag2);
        char *pathStart;
        pathStart = strstr(urlCopy, host);
        //printf("\n ------- urlCopy -------- %s \n",urlCopy);
        //printf("\n pathstart %s \n", pathStart);
        pathStart = pathStart + strlen(host);
        strcpy(address, pathStart);
        //printf("\n FOUND PATH: %s \n", address);
    }

    //printf("\n FOUND PATH****************: %s \n", address);
    //printf("\n FOUND PATH****************: %d \n", strlen(address));

	if ((strlen(address)) == 0)
    {
	    strcpy(address, "/");
    }

    strcpy(httpRequestAddress, address);
    strcpy(httpHostName,host);

    //printf("\n FOUND HOST: %s \n", httpHostName);
    //printf("\n FOUND PATH: %s \n", httpRequestAddress);
}

int sendFinishPacket(int fin_flag, int ack_flag, int last_ack)
{

	char datagram[8096], *data , *pseudogram;

	int s = socket (PF_INET, SOCK_RAW, IPPROTO_TCP);
    if(s == -1)
	{
	    printf("Failed to create socket");
	    exit(1);
	}
    //printf("\n socket ID = %d \n",s);
    struct timeval timeout;

	/* 60 Secs Timeout */
	timeout.tv_sec = 60;  
	timeout.tv_usec = 0; 
    setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(struct timeval));

	memset (datagram, 0, 4096);
	struct iphdr *iph = (struct iphdr *) datagram;

	//TCP header
	struct tcphdr *tcph = (struct tcphdr *) (datagram + sizeof (struct ip));
    if (last_ack == 0)
    {
		getTcpHeader(tcph, nextSeqNumber, nextAckNumber, 0, fin_flag, 0, ack_flag, 0);
    }
    else
    {
       getTcpHeader(tcph, nextSeqNumber, nextAckNumber+1, 0, fin_flag, 0, ack_flag, 0);
    }

    data = datagram + sizeof(struct iphdr) + sizeof(struct tcphdr);
    int totalPacketLength = sizeof (struct iphdr) + sizeof (struct tcphdr) + strlen(data);

	getIpHeader(iph, 5, 4, 0, totalPacketLength, 41708, 0, 225, 
                   IPPROTO_TCP, 0, SourceIpAddress, destinationIpAddress);


    iph->check = calculateCheckSum ((unsigned short *) datagram, iph->tot_len);
	
	//Now the TCP checksum

    int tcp_length = sizeof(struct tcphdr) + strlen(data);
    struct dummyHeader psh;
    psh = getPsudoHeader(SourceIpAddress, destinationIpAddress, 0, IPPROTO_TCP, tcp_length);
	     
	int psize = sizeof(struct dummyHeader) + sizeof(struct tcphdr) + strlen(data);
	pseudogram = malloc(psize);
	     
	memcpy(pseudogram, (char*) &psh, sizeof (struct dummyHeader));
	memcpy(pseudogram + sizeof(struct dummyHeader), tcph, sizeof(struct tcphdr) + strlen(data));

    //printf("\n hdr size %i", sizeof(pseudogram));	     
	tcph->check = calculateCheckSum((unsigned short*) pseudogram , psize);
            
	     
	//IP_HDRINCL to tell the kernel that headers are included in the packet
	int var = 1;
	const int *val = &var;
	     
	if (setsockopt (s, IPPROTO_IP, IP_HDRINCL, val, sizeof (var)) < 0)
	{
	    printf("Error setting IP_HDRINCL");
	    exit(0);
	}

           
	     
	// SEND FINISH PACKET and TERMINATE CONNECTION
    //Send the packet
	if (sendto (s, datagram, iph->tot_len ,  0, (struct sockaddr *) &sin_address, sizeof (sin_address)) < 0)
	{
	   printf("\n sendto failed \n");
	}
	//Data send successfully
	else
	{
	  //printf ("Packet Sent. Length : %d \n" , iph->tot_len);
	}
    return s;

}

