### Description

=>> General rule: Adapt from SNMPMediator to SoapMediator

### Prerequisites

The description bases on the environment: Ubuntu 16.04, Maven 3.3.9, Java 1.8, git 2.7.4, curl

### Build and Install

**These steps need to be done after every git pull to reflect the new changes**

Builds the executable jar for the Netconf2SNMPMediator

  * The executable Netconf2SNMPMediator.jar is placed in the /build subdirectory on replaces the previouse version.
  * The java and docker build instructions are in the pom.xml.
  * (Today 17/05/12) The dana-i2cat repository is not available. As a workarround the required netconf4j artifact is stored in the project local repository *repo*.

Build command:

```commandline
mvn clean install
```

### Usage

#### Mediator Config File


```json
{
	"Name":"<mediatorname-in-odl>",
	"DeviceType":<int-of-enum of device type>,
	"DeviceIP":"<remote Ip Address>",
	"TrapPort":<trap port>,
	"NeXMLFile":"<rel-path-to-xml-ne-filename>",
	"NcPort":<netconf-port>,
	"ODLConfig":[{"Server":"<odl-server-ip>","Port":<odl-http-port>,"User":"<odl-username>","Password":"<odl-password>"}],
	"IsNCConnected":false
}
```

#### Stand alone JAR

The NetconfServerSimulator can be directly started from the command line. It is recommended to use screen for remote ssh/putty sessions.
The jar parameter are:
    1. config filename
    2. directory (mandatory) with all yang files, used by the simulation
    3. uuid (optional) to provide an individual id

```Script
java -classpath Netconf2SNMPMediator.jar  $1 $2 $3
```
In the *build* directory there are some examples for start commands.

```Script
./Netconf2SNMPMediator.sh ../test.config ../yang/yangNeModel
```

#### Routing setup for SNMP mediator with iptables

This configuration is useful:
- the SNMP Device does not support traps to configurable ports and send them always to port 162.
- the jar shouldnt' run with admin rights
- there a more mediator instance running

alternative for larger configurations doing similar: trapsportmapper


##### Basic info
iptables is active if ubuntu is running.
Its not a service and reseted by a reboot.. set the configuration again after reboot.


##### iptables command list

|You like             |  Command              |   Description  |
|-----------------------|-----------------------|---------------------|
|Show tables | iptables -t nat --line-numbers -L | Show NAT configuration with line number
|Delete a rule | iptables -t nat -D PREROUTING 6 | Delete a rule (her line 6) from the nat

Example of rule for redirection for port 162 for different sources (s) to individual ports
```
sudo iptables -t nat -A PREROUTING -s 172.16.199.101 -p udp --dport 162 -j REDIRECT --to-port 10101
sudo iptables -t nat -A PREROUTING -s 172.16.199.102 -p udp --dport 162 -j REDIRECT --to-port 10102
```

##### Parameter marking and mapping, conversions

###### Attributes:

Available attributes are:
NETCONF/XML Type is always a String


  Name   |   parameter   |   Description   |
---------------|---------------------------------------------------|-------------------------------------------------------|
oid | oid=".1.2.3.4" | Attribute with SNMP mapping for given oid. For NETCONF-get, request content from Device.
oid | "" | Marked as potential used for SNMP, but not yet.
access | access="read-only" | Attribut can be read from NETCONF-get.
access | access="read-write" | Attribute can be read by NETCONF-get and  written by NETCONF edit-config. Conversion must be bidirectional.
access | no access attribute | Default is Read-Only

  Name   |   SNMP Type   |   parameter   |   Description   |
---------------|------|---------------------------------------------------|-------------------------------------------------------|
conversion | Integer32 | conversion="int-to-boolean" | Convert 1-true and not 1-false between boolean and int
conversion | Integer32 | conversion="int-to-boolean-dd,dd,dd-true" | Convert listed numbers to true
conversion | Integer32 | conversion="int-to-boolean-dd,dd,dd-false" | Convert listed numbers to false
conversion | Integer32 | conversion="if-dd,dd,dd-term1-term2" | if value listed, result is *term1*, if not *term2*
conversion | Integer32 |conversion="map-dd1,dd2,dd3-term1-term2" | Bidirectional map dd1 to term1, dd2 to term2 and soon
conversion | Octetstring | no conversion attribute | SNMP -> Netconf from String to String

Examples

      <polarization oid="" access="read-write">not-specified</polarization>
      <air-interface-name oid=".1.2.3.4" access="read-only">Air interface ID not yet defined.</air-interface-name>
      <air-interface-name oid="1.2.3.4" access="read-only">Air interface ID not yet defined.</air-interface-name>
      <tx-power oid=".1.2.3.4" conversion="divide-10" access="read-only">99</tx-power>
      <adaptive-modulation-is-on oid="1.2.3.4.5" conversion="int-to-boolean-2-true" access="read-only">false</adaptive-modulation-is-on>
      <link-is-up oid=".1.2.3.4" conversion="int-to-boolean-1-true" access="read-only">false</link-is-up>
      <transmitter-is-on oid=".1.2.3.4" conversion="map-1,2-false,true" access="read-write">false</transmitter-is-on>
      <loop-back-kind-up oid=".1.2.3.4" conversion="if-1-none-rf" access="read-only">none</loop-back-kind-up>

##### Trap notifications
Example for notifications by traps

```  <snmptrap-notifications>
        <!--
        Netconf notification, generated frm SNMP-Trap as "SNMP::VarBind" or single Trap.
        Trap is used if one of the binded traps the key=oid+value is matching.
        If this is the case values can be taken over from Traps. But this is only possible if the
        type is the compatible between SNMP and NETCONF.
        -->
        <problem-notification snmpTrapOid="1.2.3.4.5" snmpTrapValue="Any text" xmlns="urn:onf:params:xml:ns:yang:microwave-model">
            <counter>$OIDVALUE=1.2.3.4.5.6.7</counter>
            <time-stamp>$TIME</time-stamp>
            <problem>HAAMRunningInLowerModulation</problem>
            <object-id-ref>LP-MWPS-RADIO</object-id-ref>
            <severity>major</severity>
        </problem-notification>
        <problem-notification snmpTrapOid="1.2.3.4.5" snmpTrapValue="Modulation is down shift" xmlns="urn:onf:params:xml:ns:yang:microwave-model">
          <counter>$COUNTER</counter>
          <time-stamp>$TIME</time-stamp>
          <problem>ModulatIsDownShift</problem>
          <object-id-ref>LP-MWPS-RADIO</object-id-ref>
          <severity>critical</severity>
        </problem-notification>
      </snmptrap-notifications>
```


####TODO


