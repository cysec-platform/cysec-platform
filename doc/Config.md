# Config


## File
The settings are stored in `cysec.cfgResources` usually in `<baseDir>/etc`.

If you have your own settings, then change your local config file as required.

### Content
Fallowing entries are currently used:

| type | name | default | description |
| ---- | ---- | ------- | ----------- |
||||| 
| string | cysec_base_path | /var/lib/cysec/ | The path to be used to find other subdirectories of cysec |
| string | cysec_data_path | /var/lib/cysec/data/ | The path to be used to find data files of cysec | 
| string | cysec_coach_path | /var/lib/cysec/coaches/ | The path to be used to find new coaches |
||||| 
| string | cysec_authentication_scheme | Basic | Basic username & password authentication |
| string | cysec_authentication_property | Authorization | The auth header |
| string | cysec_header_username | oidc_claim_preferred_username | The OAuth username |
| string | cysec_header_email | oidc_claim_email | The OAuth email |
| string | cysec_header_company | oidc_claim_company | The OAuth company |
| string | cysec_header_firstname | oidc_claim_given_name | The OAuth firstname |
| string | cysec_header_lastname | oidc_claim_family_name | The OAuth lastname |
||||| 
|numeric | cysec_recommend_count | 3 | The number of displayed recommendations |