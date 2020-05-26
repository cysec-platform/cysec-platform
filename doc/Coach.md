# Coach

This guide documents how to write coach.xml files.

## Include media in block/question instruction
Even though blocks and questions do have an attachment attribute it is not possible to specify where in the instruction text they should appear.\
For this case, the `Resource` Rest API serves as an endpoint to fetch Library media.

Access the endpoint with /api/rest/resources/{coach}/{libraryId}/{resource} where
`coach` is the id of the coach file, `libraryId` is the FQDN as specified in the xml file and `resource` is a path to a file contained in the resources folder of the specified library. Usually the file resides in "assets/images/" (if its an image)

For example, the below would be a png:

```
[smesec_instance]/api/rest/resources/company/eu.smesec.library.FirstLibrary/sign-check-icon.png
```

Make sure to define media in the coach xml like below. The "[smesec_instance]" text is placeholder for the webapplication context and must be present!

The questionnaireController.js checks the content of all instruction.text elements for occurences of that placeholder and replaces them globally. 
```
<img src="[smesec_instance]/api/rest/resources/company/eu.smesec.library.FirstLibrary/sign-check-icon.png" height="200">
```
 