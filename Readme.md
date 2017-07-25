# Padins

A workflow management system for scientific devops.

**Goal :** to develop a computational tool for science researcher. This tool
let them do three main things :
* Design a scientific worklow
* Execute the workflow on a distributed system
* Visualize the results, including matrices as videos

## Typical Use Cases
* User wants to produce a Boussinesq Simulation on a hillslope and see the results
on a chart, choosing the kind of chart. Then go through the chart.
* User wants to adjust its mathematic model comparing the simulation's results
and the data she picked up on the real case study.

## Getting Started

There are two way to run this project : using an already built docker image or using the binaries.

### Using docker :
**Prerequisites**

To run this project, you'll need :

*Docker*
```
https://www.docker.com/community-edition
```

**Then you just need to run it, no installation needed**

**UNIX-based OS**
1. Open a terminal
2.  `docker run -it --rm -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock antoinecheronirisa/padins`
3. Go to : http://localhost:8080

**WNIDOWS**
1. Open the Windows Cmd
2. Open port 445 of Windows trough the firewall, typing `netsh firewall set portopening tcp 445 enable`
3. Make sure docker is started, then `docker run -it --rm -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock antoinecheronirisa/padins`
4. Open another Windows Cmd and type the following command to get the padins container's IP address : `docker-machine ip default`. Info : default is the name of the open VirtualBox.
5. Go to : http://{{ container's IP address }}:8080

**Persisting data**

To persist data used in the container, mount a volume to :  /root/.padins/0-1-0.
Here is an example of the command you can use, supposing that you have a .padins/0-1-0 folder in your home directory :
`docker run -it --rm -p 8080:8080 -v $HOME/.padins/0-1-0:/root/.padins/0-1-0 -v /var/run/docker.sock:/var/run/docker.sock antoinecheronirisa/padins`


### Using the binaries (Linux and MacOS)
**[INFO]** MacOS doesn't have the package manager apt-get. Instead, on every command using apt-get, use brew. For instance, `apt-get install maven` becomes `brew install maven`.
If you don't know homebrew, you can download it [clicking this link](https://brew.sh/).

**Prerequisites**

To run this project, you'll need :

*Docker*
```
https://www.docker.com/community-edition
```

*A Java Virtual Machine*
```
https://java.com/en/download/
OR
apt-get install default-jdk
```

You will also need maven and npm
```
sudo apt install maven
sudo apt-get install npm
```

### Installing
First : open your terminal and go to the folder where you want to install this project

Then download the version you want,

For the stable version (latest release) :
```
wget "https://github.com/AntoineCheron/padins-backend/archive/v0.1.0.zip"
unzip v0.1.0.zip -d ./
cd padins-backend-0.1.0
```

For the last development version :
```
wget "https://github.com/AntoineCheron/padins-backend/archive/master.zip"
unzip master.zip -d ./
cd padins-backend-master
```

Then, just one step to install :
```
./install.sh
```

### Running
1. Go to the folder where you installed the project
2. Run the following command :
```
./run.sh
```
