# Authentication

CySeC platform supports Basic Auth (username&password) as well as an external provided by an authenticating reverse proxy.

## Developing alternate authentication filters
To enable request interception for specific classes a custom interface for NameBinding was created.
`@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {
}
`

The AuthFilter class is annotated with both the  @Provider and @Secured annotation. That way Jersey will automatically intercept requests to Resources marked with @Secured to AuthFilter.

`@Secured
@Provider
public class AuthFilter implements javax.ws.rs.container.ContainerRequestFilter, Filter {`

To enable simply add @Secured to an arbitrary Rest resource

`@Secured
@Path("rest/questionnaires")
@Produces(MediaType.APPLICATION_JSON)
@DenyAll
public class Questionnaires {`

The base class `AbstractAuthStrategy` defines the interface for specific AuthStrategies. There are three sub classes namely `BasicAuthStrategy`, `HeaderAuthStrategy` and `AdminAuthStrategy` which handle the protocol-specific authentication logic.

### Web.xml
Since we want to protect both the API and the /webapp content with Authentication by AuthFilter a filter has to be mapped to the appropriate URL pattern.
Unfortunately, the class provided in <filter> has to derive the Filter.class of `javax.servlet.Filter` and therefore implement "doFilter()". During the execution of doFilter(), the application is within the scope of Servlets, which is one layer underneath Jersey. That means there is no access to injected classes, such as CAL or Config. As a workaround, we place a Post call to the `Login` class which is an @Secured resource. That way the filter() method from the `ContainerRequestFilter` is executed.
`<filter>
        <filter-name>AuthFilter</filter-name>
        <filter-class>eu.smesec.cysec.platform.core.auth.AuthFilter</filter-class>
    </filter>

    <!-- Protects app from unauthorized users -->
    <filter-mapping>
        <filter-name>AuthFilter</filter-name>
        <url-pattern>/app/*</url-pattern>
    </filter-mapping>`

## Configuring Basic Auth
`/Users` endpoint allows CRUD operations for users stored in the XML backend. Those users may login to the webapp in /app with the username/email and password set during registration. Since neither email nor username are unique across companies, the company id has to be passed along with a trailing slash '/' like so: 'fhnw/nic' ('nic' as username and 'fhnw' as company).

The password can be reset on the URL `/public/resetPassword/`
The registration page is on the URL `/public/signUp/`

## Dummy Auth
If the application should not use an authenticator there is a dummy authenticator providing always static, valid credentials.

To enable dummy authentication `cysec_authentication_scheme` needs to be set to `dummy` (case sensitive). If done so the authenticator will return always `acme` as company and `anonymous` as username.

Furthermore, the company acme needs to be created. For this reason the following things need to be done:
1. create a directory named `<config_base>/<datadir>/<instance_name>/acme`
2. create a file `users.xml` in this directory containing the following content
   ```
   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <company xmlns="https://smesec.eu" companyname="ACME" id="acme">
      <user id="0" username="anonymous" email="test@example.com" firstname="" surname="" lock="NONE">
        <role>Admin</role>
      </user>
   </company>
   ```

## Admin Auth
The `/api/AdminPage` is accessible to statically defined users in the cfgResources file. Even if a user is already authenticated (eithe via Basic or OAuth) one has to enter the admin password.
- `cysec_admin_prefix, _admin_` The site admin identification prefix (serves as company filler)
- `cysec_admin_users` The users eligible to authenticate as admin
- `cysec_admin_password` The site admin password

## Configuring External Auth
Users may also login with an externally provided authentication based on header fields. The AuthFilter checks an incoming request for the required headers (set by a authentication proxy) and Basic Auth or Admin Auth if not all necessary headers fields are found. The following headers must be included in the header:
- `username` Display name for user
- `company` The company the user is registered with
- `email`  The email address used to register the account
- `First name` User first name
- `Last name` User last name

It is possible to configure the Oauth header names in a configuration file `/var/lib/cysec/etc/cysec.cfgResources.cfg` like below (TODO: add example from wwwtest)

- `cysec_authentication_scheme`, Basic, Basic username&password authentication
string, cysec_authentication_property, Authorization, The auth header
string, cysec_header_username, oidc_claim_preferred_username, The OAuth username
string, cysec_header_email, oidc_claim_email, The OAuth email
string, cysec_header_company, oidc_claim_company, The OAuth company
string, cysec_header_firstname, oidc_claim_given_name, The OAuth firstname
string, cysec_header_lastname, oidc_claim_family_name, The OAuth lastname
```


