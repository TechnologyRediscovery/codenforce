# Migrating from glassfish to wildfly/JBOSS
Glassfish was the server I learned on during graduate school. As the saying goes:

	relationships based on intense experiences rarely work out

...and such is the case with me and glassfish. After running into known bugs with JDBC configurations down in 14.1 and hitting known bugs on the community-ported release called Payara that screwed over our user authenticiation--and dozens of hours of attempted debugging--and we are burying all servers named after fish.


## Migration steps

detailed below:

### take snapshot of digital ocean VPS
Powered down the droplet via ssh and then ordered DO to take a snapshot via the browser GUI.

### review resources
Wildfly [official deployment instructions][1]
Blog posting about installing [nginx and wildfly side-by-side][2]

[1]: http://docs.wildfly.org/14/Admin_Guide.html#application-deployment "official documentation"
[2]: https://www.rosehosting.com/blog/install-wildfly-with-nginx-as-a-reverse-proxy-on-ubuntu-16-04/ "Simple install tutorial"

### SSL Configuration
ssl.com's [official documentation][3] on installing their cert in a java environment is sparse but authoritative


[3]: https://www.ssl.com/how-to/install-ssl-java-web-server/

### port config
RedHat's [discussion of ports and interfaces][4] is excellent

[4]: https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.1/html/configuration_guide/network_and_port_configuration
