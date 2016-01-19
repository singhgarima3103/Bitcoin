BitCoin
=======
The goal is to mine bitcoins using akka actor model so that it runs well on multiple core machines.

Team members:
=======
Garima Singh (UFID : 5197-5877)
Balaji Iyer  (UFID : 0443-5000)

Prerequisites:
=======
Scala 2.11.2
Akka 2.3.6 distribution for Scala 2.11

Instructions to execute:
=======
Compile:
	scalac -cp  "../lib/scala-library-2.11.2.jar:../lib/akka/*:." BitCoinMaster.scala

Run:
	Server Mode: scala -cp  "../lib/scala-library-2.11.2.jar:../lib/akka/*:." BitCoinMaster <number of zeros>
	Client mode: scala -cp  "../lib/scala-library-2.11.2.jar:../lib/akka/*:." BitCoinMaster <server ip>

Observations:
=======

1. Size of the work unit:
	
	For 10,000 work unit
	_____________________
	2 machines(with 8 cores each- 16 actors) could process 7.263*10^7 inputs with a throughput of 242100 messages/ second

	for 100,000 work unit
	_____________________
	2 machines(with 8 cores each - 16 actors) could process 7.46*10^7 inputs with a throughput of 248666 messages/ second 

	for 1000,000 work unit
	______________________
	2 machines(with 8 cores each - 16 actors) could process 8.70*10^7 inputs with a throughput of 290000 messages/ second 
	
	for load sizes above this, we would overshoot the time limit(of 5 mins) while waiting for each actor to finish their set of inputs.
	
	Also, better performance was obtained by running a single actor on a single core.
	

2. For : scala project1.scala 4

	For 1000,000 work unit
	_____________________
	Result file: results/result_4zeros.txt
	actorCount is 8
	Total Number of bitcoins mined is 582
	Total number of messages processed is 383000000
	Throughtput is 1276666 messages/sec
	2162.19user 23.59system 5:05.33elapsed 715%CPU (0avgtext+0avgdata 198036maxresident)k
	0inputs+768outputs (0major+84042minor)pagefaults 0swaps


	For 100,000 work unit
	_____________________
	Result file: results/4_zeroes.txt
	actorCount is 8
	Total Number of bitcoins mined is 586
	Total number of messages processed is 37700000
	Throughtput is 125666.666 messages/sec
	2095.64user 24.28system 5:04.34elapsed 696%CPU (0avgtext+0avgdata 228476maxresident)k
	0inputs+976outputs (0major+74386minor)pagefaults 0swaps

   For : scala project1.scala 5

   	For 1000,000 work unit
	_____________________
	Result file: results/result_5zeros.txt
	actorCount is 8
	Total Number of bitcoins mined is 36
	Total number of messages processed is 391000000
	Throughtput is 1303333 messages/sec
	2157.76user 23.93system 5:05.19elapsed 714%CPU (0avgtext+0avgdata 211420maxresident)k
	0inputs+552outputs (0major+85629minor)pagefaults 0swaps

	For 100,000 work unit
	_____________________
    Result file: results/5_zeroes.txt
    actorCount is 8
	Total Number of bitcoins mined is 27
	Total number of messages processed is 37000000
	Throughtput is 123333.333 messages/sec
	2000.01user 26.43system 5:04.64elapsed 665%CPU (0avgtext+0avgdata 243052maxresident)k
	0inputs+608outputs (0major+93682minor)pagefaults 0swaps



3. The coin with the most 0s : 7 (bitcoin:balajiiyer264988346511439    hash:0000000477cc20915e86e466ddd5ee0a244115cfad118364ba2f6c7756d293ea)

4. The largest number of working machines you were able to run your code with : 6






