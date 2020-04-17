InGrid Base Webapp
=========

This library is part of the InGrid software package. It provides common functionality for all administration GUIs for all InGrid iPlugs.


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-base-webapp/issues
- Source Code: https://github.com/informationgrid/ingrid-base-webapp
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.

### Tests

The tests do not work out of the box, since they are adapted
to run inside a docker container with jenkins.

To enable the tests, start a elastic seaarch node:

```docket-compose up -d```

Adapt the file `src/test/resources/config.override.properties` (see `elastic.remoteHosts`).

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
