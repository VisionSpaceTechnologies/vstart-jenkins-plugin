# Vstart Jenkins plugin v1.0

### Main Features for Version 1.0
- Connection with a VSTART server.
- Integration with the Credentials plugin in order to store user information.
- Listing of the user's VSTART projects on the configuration page of a Jenkins Job and ability to choose one of the projects.
- Listing of the projects' test cases on the configuration page of a Jenkins Job and ability to choose one of the test cases.
- A VSTART publisher that builds an HTML report with relevant information about the VSTART execution that occured during a build on Jenkins with one or more VSTART buildsteps.
- Integration with the JUnit trend graphic to display test case statistics.
- Customized console output for each buildstep containing relevant information about the VSTART execution.

### Packaging

To package the project use maven:
```
$> mvn package
```