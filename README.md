# Sauron

Distributed Systems 2019-2020, 2nd semester project


## Authors

**Group A41**

### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.  
This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.

### Team members

| Number | Name                   | User                              | Email                               |
| -------|------------------------|-----------------------------------| ------------------------------------|
| 89549  | Tiago Barroso          | <https://github.com/TiagoMAB>     | <mailto:tiago.agostinho.barroso@tecnico.ulisboa.pt>   |
| 89425  | Daniel Pereira         | <https://github.com/DanielPereira890>     | <mailto:daniel.r.pereira@tecnico.ulisboa.pt>     |
| 89445  | Francisco Serralheiro  | <https://github.com/Serralheiro> | <mailto:francisco.serralheiro@tecnico.ulisboa.pt> |

### Task leaders

| Task set | To-Do                         | Leader              |
| ---------|-------------------------------| --------------------|
| core     | protocol buffers, silo-client | _(whole team)_      |
| T1       | cam_join, cam_info, eye       | _Francisco Serralheiro_ |
| T2       | report, spotter               | _Daniel Pereira_       |
| T3       | track, trackMatch, trace      | _Tiago Barroso_     |
| T4       | test T1                       | _Tiago Barroso_     |
| T5       | test T2                       | _Francisco Serralheiro_ |
| T6       | test T3                       | _Daniel Pereira_       |


## Getting Started (Initialization and configuration guide)

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](https://github.com/tecnico-distsys/Sauron/blob/master/README.md) for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

* To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```
### Installing

#### Installing - First Step

* To compile and install all modules (execute this command before executing all others):

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require the servers to be running.

#### Installing - Running Server

* To start the server (assuming that you are in the current directory):

```
cd silo-server
mvn clean compile exec:java
```

#### Installing - Running Tests

* To run the integration tests (assuming that you are in the current directory and the server is already running):

```
cd silo-server
mvn verify
```

#### Installing - Running Eye

* To start the client eye (assuming that you are in the current directory):

```
./eye/target/appassembler/bin/eye #address #port #camera_name #camera_latitude #camera_longitude
```

Where #address is the address of the server (the predefined value for this server is: localhost)

Where #port is the port of the server (the predefined value for this server is: 8080)

Where #camera_name is the name of the camera reporting (example: Tagus)

Where #camera_latitude is the latitude of the camera reporting (example: 23) (must be a value between -90 e 90)

Where #camera_longitude is the longitude of the camera reporting (example: 93) (must be a value between -180 e 180)

* Usage example:
```
./eye/target/appassembler/bin/eye localhost 8080 Alameda 38.737000 -9.136596
```

#### Installing - Running Spotter

* To start the client spotter (assuming that you are in the current directory):

```
./spotter/target/appassembler/bin/spotter #address #port
```

Where #address is the address of the server (the defined value for this server is: localhost)
Where #port is the port of the server (the defined value for this server is: 8080)

* Usage example:
```
./spotter/target/appassembler/bin/spotter localhost 8080
```

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
