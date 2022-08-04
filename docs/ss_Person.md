# Subsystem IV Persons

&lt;- [home](index.md)

## Person linking
`PersonBB` manages linking. If you are working with an object that contains linked persons, it should implement the `IFace_humanListHolder` which specifies a getter and setter for these human links. 

When you want to initiate the linking process, you'll call 
```
 public void onSelectAndLinkPersonsInit(IFace_humanListHolder hlh)
 ```
 on the `PersonBB` and while doing so, send a request parameter with the name of the form component that should be updated after the linking process is done. For example, when linking a person during the NOV creation process, the following key-value pair is included in the HTTP request:

 ```
 <f:param name="person-list-component-to-update" value="nov-chooseperson-list-form" />
 ```
 When the linking process is done, the value of this param will be included in the `update=` attribute of the commiting commadn button. The effect of this update call will be for the getters to be called on all the components inside the form included in the request param when the linking process was iniiated.

 To make the update process work, the `PersonBB` injects the new linked human list on a session bean member called 



