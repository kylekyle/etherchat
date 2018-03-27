# EtherChat

![Etherchat](https://github.com/kylekyle/etherchat/blob/master/etherchat.png "EtherChat")

Etherchat is an ethernet chat client. It doesn't use TCP/IP and only operates on the local broadcast network. It is used to demonstrate networking concepts at West Point and it isn't terribly useful for anything else. Since it doesn't use TCP you'll find a message doesn't arrive every so often. In fact, it may arrive for one student but not for another. Welcome to layer two :)

## Installation

* On Windows
  * Install [WinPcap](https://www.winpcap.org/install/default.htm) or [Npcap](https://nmap.org/download.html).
  * Download [the etherchat-x.x.x.jar file from the releases page](https://github.com/kylekyle/etherchat/releases).
  * Double-click the the jar file you downloeded.

* On Mac or Linux
  * Download [the etherchat-x.x.x.jar file from the releases page](https://github.com/kylekyle/etherchat/releases).
  * You will probably need to run the program with `sudo`:

```bash
$ sudo java -jar etherchar-x.x.x.jar 
```

## Issues

It's really unlikely I'm going to fix any problems you have or add any features to this thing. I am happy to accept pull requests. Of course, you can always feel free to fork the repo and make it your own! Sky's the limit!
