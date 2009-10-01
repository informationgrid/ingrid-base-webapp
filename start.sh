#!/bin/bash

# -Dindexing
#	true | false
#	de-/activates step 3
#	default: true

# -Dmapping
#	true | false
#	de-/activates redirect to specific steps
#	default: true

# -Dcommunication
#	string
#	location of communication
#	default: /conf/communication.xml

# -DplugDescription
#	string
#	location of plug description
#	default: /conf/plugDescription.xml

mvn jetty:run -Dindexing=false -Dmapping=false