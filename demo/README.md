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
./eye/target/appassembler/bin/eye localhost 8080 Alameda 38.737000 -9.136596
```

#### Installing - Third Step (Option 2) Running Spotter


* To start the client spotter (assuming that you are in the demo folder):

```
cd ..
./spotter/target/appassembler/bin/spotter localhost 8080
```

----

## 3. Uses Cases

### 3.1. Register Camera (Eye) Observations 

* Initially, start by registering a camera at the execution of the Eye program, on the folder 

```
/eye/src/main/java/pt/tecnico/sauron/eye/
```

* After picking a Camera name, Latitude and Longitude, start the program with ```#address = localhost``` and ```#port = 8080```, for instance:

```
./eye/target/appassembler/bin/eye localhost 8080 CamaraPortao 38.737613 -9.303164
```

* Through this type of execution, the program will be waiting direct Observation input from the terminal.
If the goal is to see a demo of several Observation inputs, run the program with ```data.txt``` file in its execution command, like this:

```
./eye/target/appassembler/bin/eye localhost 8080 CamaraPortao 38.737613 -9.303164 < data.txt
```

---

### 3.2 - List an object's most recent observation

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 8080 Casa 10 30
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
./target/appassembler/bin/eye localhost 8080 Trabalho 1 1
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
./target/appassembler/bin/spotter localhost 8080
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

### 3.3 - List most recent observation of all objects that match a partial identifier

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 8080 Casa 10 30
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
./target/appassembler/bin/eye localhost 8080 Trabalho 1 1
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
./target/appassembler/bin/spotter localhost 8080
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

### 3.4 - List an object's observations

* In folder A41-Sauron, navigate to folder silo-server and run the server:

```
cd silo-server
mvn clean compile exec:java
```

* Go to folder eye and run client eye with arguments host port cameraName latitude longitude:

```
cd ..
cd eye
./target/appassembler/bin/eye localhost 8080 Casa 10 30
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
./target/appassembler/bin/eye localhost 8080 Trabalho 1 1
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
./target/appassembler/bin/spotter localhost 8080
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