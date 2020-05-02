# Sauron

Distributed Systems 2019-2020, 2nd semester project

## Authors

**Group A41**

# Demonstration Guide

## 1. Introduction to Sauron System

The Sauron system helps finding lost objects and missing people. Videovigilance cameras are devices that are increasingly present on our day-to-day.
Traditionally, its function is to record images that can then be consulted by authorities, if necessary. For example, recorded images can help to investigate a crime.
Thanks to the advances in image recognition, it has become possible to have smart cameras that can "see" and "recognize". For example, it is possible to recognize vehicles from their license plate.

----

## 2. Installation (Step by step guide)

* Make sure you are in the demo folder and follow all commands exactly as provided here. If you are an experience user, you can go to the main README.md to see a more abstract guide.

#### Installing - First Step (Installing the System)


* Open a linux terminal. You will execute the following commands to go to the main directory and install the contract and other modules:

```
cd ..
mvn clean install -DskipTests
```

#### Installing - Second Step (Running Server)

* To start the server you wil execute the following commands:

```
cd silo-server
mvn clean compile exec:java
```

After this the server will be left running in this terminal. Open a different terminal, but don't close this one.

#### Installing - Third Step (Option 1) Running Eye

* To start the client eye (assuming that you are in the demo folder):

```
cd ..
./eye/target/appassembler/bin/eye localhost 2181 Alameda 38.737000 -9.136596
```

#### Installing - Third Step (Option 2) Running Spotter


* To start the client spotter (assuming that you are in the demo folder):

```
cd ..
./spotter/target/appassembler/bin/spotter localhost 2181
```

----

## 3. Uses Cases

### 3.1. Register Camera (Eye) Observations 

* Initially, start by registering a camera at the execution of the Eye program, on the folder 

```
/eye/src/main/java/pt/tecnico/sauron/eye/
```

* After picking a Camera name, Latitude and Longitude, start the program with ```#address = localhost``` and ```#port = 2181```, for instance:

```
./eye/target/appassembler/bin/eye localhost 2181 CamaraPortao 38.737613 -9.303164
```

* Through this type of execution, the program will be waiting direct Observation input from the terminal.
If the goal is to see a demo of several Observation inputs, run the program with ```data.txt``` file in its execution command, like this:

```
./eye/target/appassembler/bin/eye localhost 2181 CamaraPortao 38.737613 -9.303164 < data.txt
```

---

### 3.2. List an object's most recent observation

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Insert two persons and one car:

```
person,123456
car,AB1234
person,125678
(Press enter)
```

* Close client eye and run it again, simulating a different camera joining:

```
exit
./target/appassembler/bin/eye localhost 2181 Trabalho 1 1
```

* Insert one person and exit client:

```
person,123456
(Press enter)
exit
```

* Navigate to folder spotter and run client spotter with arguments host port:

```
cd ..
cd spotter
./target/appassembler/bin/spotter localhost 2181
```

* Ask to list most recent observation for person with identifier 123456:

```
spot person 123456
```

* On spotter app, the following output will be printed:

```
person,123456,(data),Trabalho,1,1
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----

### 3.3. List most recent observation of all objects that match a partial identifier

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Insert two persons and one car:

```
person,123456
car,AB1234
person,125678
(Press enter)
```

* Close client eye and run it again, simulating a different camera joining:

```
exit
./target/appassembler/bin/eye localhost 2181 Trabalho 1 1
```

* Insert one person and exit client:

```
person,123456
(Press enter)
exit
```

* Navigate to folder spotter and run client spotter with arguments host port:

```
cd ..
cd spotter
./target/appassembler/bin/spotter localhost 2181
```

* Ask for most recent observation for each object with identifier that matches a partial identifier:

```
spot person 12*
```

* On spotter app, the following output will be printed:

```
person,123456,(data),Trabalho,1,1
person,125678,(data),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----

### 3.4. List an object's observations

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Insert two persons and one car:

```
person,123456
car,AB1234
person,125678
(Press enter)
```

* Close client eye and run it again, simulating a different camera joining:

```
exit
./target/appassembler/bin/eye localhost 2181 Trabalho 1 1
```

* Insert one person and exit client:

```
person,123456
(Press enter)
exit
```

* Navigate to folder spotter and run client spotter with arguments host port:

```
cd ..
cd spotter
./target/appassembler/bin/spotter localhost 2181
```

* Ask to list observations for person with identifier 123456:

```
trail person 123456
```

* On spotter app, the following output will be printed:

```
person,123456,(data),Trabalho,1,1
person,123456,(data),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----

## 4. Replication and fault tolerance

### 4.1. Propagation of information through Gossip Protocol

First, open up 2 different terminals with 2 different instances

###### Terminal 1
```
cd silo-server
mvn clean compile exec:java -D instance=1
```
###### Terminal 2
```
cd silo-server
mvn clean compile exec:java -D instance=2
```
* Then, open up a different terminal in main directory, run an Eye Client and connect it to replica 1

```
./eye/target/appassembler/bin/eye localhost 2181 Casa 10 30 1
```

* Eye will connect with replica 1. Insert 1 persons with id 12345 and exit client:

```
person,12345
(Press enter)
exit
```
* Now, in the same client terminal, run a Spotter Client and connect it to replica 2. Wait 30 seconds.
```
./spotter/target/appassembler/bin/spotter localhost 2181 2
```

* Ask to spot the person for person with identifier 12345:
```
spot person 12345
```

* As is, the following output will be printed:

```
person,12345,(data1),Casa,10,30
```

Which means that the information was propagated from replica 1 to replica 2.

### 4.2. Fault tolerance with track command

* With a server terminal, in folder A41-Sauron, navigate to folder silo-server and run the server with instance number 1:

```
cd silo-server
mvn clean compile exec:java -D instance=1
```

* With a client terminal, in folder A41-Sauron, go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Eye will connect with replica 1. Insert one person with id 12345 and exit client:

```
person,12345
(Press enter)
exit
```

* Navigate to folder spotter and run client spotter with arguments host port:

```
cd ..
cd spotter
./target/appassembler/bin/spotter localhost 2181
```

* Ask to list most recent observation for person with identifier 12345:

```
spot person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data1),Casa,10,30
```

* On server terminal, press enter to exit the server and initiate new server with instance number 2
```
(Press enter)
mvn clean compile exec:java -D instance=2

```
* Ask again to list most recent observation for person with identifier 12345:

```
spot person 12345
```
* On spotter app, the following output will be printed (cached observation):

```
person,12345,(data1),Casa,10,30
```

* Ask again to list most recent observations for person with identifier 12345:

```
trail person 12345
```
* On spotter app, the following output will be printed (cached observation):

```
person,12345,(data1),Casa,10,30
```

* On a different client terminal, assuming you are in main folder, navigate to eye folder and connect the same camera as before
```
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Eye will connect with replica 2, Insert one person with id 12345 and exit client:
```
person,12345
(Press enter)
exit
```

* Using the spotter terminal, ask to list most recent observation for person with identifier 12345:

```
spot person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data2),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----

### 4.3. Fault tolerance with trackMatch command

* With a server terminal, in folder A41-Sauron, navigate to folder silo-server and run the server with instance number 1:

```
cd silo-server
mvn clean compile exec:java -D instance=1
```

* With a client terminal, in folder A41-Sauron, go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Eye will connect with replica 1. Insert two persons with id 12345 and 12300 and exit client:

```
person,12345
(Press enter)
person,12300
(Press enter)
exit
```

* Navigate to folder spotter and run client spotter with arguments host port:

```
cd ..
cd spotter
./target/appassembler/bin/spotter localhost 2181
```

* Ask for most recent observation for each object with identifier that matches a partial identifier:

```
spot person 123*
```

* On spotter app, the following output will be printed:

```
person,12300,(data2),Casa,10,30
person,12345,(data1),Casa,10,30
```

* On server terminal, press enter to exit the server and initiate new server with instance number 2
```
(Press enter)
mvn clean compile exec:java -D instance=2

```

* On a different client terminal, assuming you are in main folder, navigate to eye folder and connect the same camera as before
```
cd eye
./target/appassembler/bin/eye localhost 2181 Casa 10 30
```

* Eye will connect with replica 2, Insert one person with id 12321 and exit client:
```
person,12321
(Press enter)
exit
```

* * Using the spotter terminal, ask for most recent observation for each object with identifier that matches a partial identifier:

```
spot person 123*
```

* On spotter app, the following output will be printed:

```
person,12300,(data2),Casa,10,30
person,12321,(data3),Casa,10,30
person,12345,(data1),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----
### 4.4. Fault tolerance with trace command

* With a server terminal, in folder A41-Sauron, navigate to folder silo-server and run the server with instance number 1:

```
cd silo-server
mvn clean compile exec:java -D instance=1
```

* With a client terminal, in folder A41-Sauron, go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd eye
./target/appassembler/bin/eye localhost 8080 Casa 10 30
```

* Eye will connect with replica 1. Insert three persons with id 12345 and exit client:

```
person,12345
(Press enter)
person,12345
(Press enter)
person,12345
(Press enter)
exit
```

* With a client terminal, in folder A41-Sauron, navigate to folder spotter, run spotter with arguments host port:

```
cd spotter
./target/appassembler/bin/spotter localhost 8080
```

* Ask to list observations for person with identifier 12345:

```
trail person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data3),Casa,10,30
person,12345,(data2),Casa,10,30
person,12345,(data1),Casa,10,30
```

* On server terminal, press enter to exit the server and initiate new server with instance number 2
```
(Press enter)
mvn clean compile exec:java -D instance=2

```

* On eye terminal, connect to the same camera as before
```
./target/appassembler/bin/eye localhost 8080 Casa 10 30
```

* Eye will connect with replica 2, Insert one person with id 12345 and exit client:
```
person,12345
(Press enter)
exit
```

* On spotter terminal, ask to list observations for person with identifier 12345:

```
trail person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data4),Casa,10,30
person,12345,(data3),Casa,10,30
person,12345,(data2),Casa,10,30
person,12345,(data1),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----

### 4.5. Fault tolerance with spot and trace commands

* With a server terminal, in folder A41-Sauron, navigate to folder silo-server and run the server with instance number 1:

```
cd silo-server
mvn clean compile exec:java -D instance=1
```

* With a client terminal, in folder A41-Sauron, go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd eye
./target/appassembler/bin/eye localhost 8080 Casa 10 30
```

* Eye will connect with replica 1. Insert one person with id 12345:

```
person,12345
(Press enter)
```

* With a different client terminal, in folder A41-Sauron, go to folder spotter and run client spotter with arguments host port:

```
cd spotter
./target/appassembler/bin/spotter localhost 8080
```

* Ask to list most recent observation for person with identifier 12345:

```
spot person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data1),Casa,10,30
```

* On eye client terminal, insert one person with id 12345 and exit client:

```
person,12345
(Press enter)
exit
```

* On spotter client terminal, ask to list observations for person with identifier 12345:

```
trail person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data2),Casa,10,30
person,12345,(data1),Casa,10,30
```

* On the server terminal, exit the server and run the server again with instance number 1:

```
(Press enter)
mvn clean compile exec:java -D instance=1
```

* On spotter client terminal, ask to list most recent observation for person with identifier 12345:

```
spot person 12345
```

* On spotter app, the following output will be printed:

```
person,12345,(data2),Casa,10,30
```

* Execute the clear command to clear the server and exit spotter app:

```
clear
exit
```

----
