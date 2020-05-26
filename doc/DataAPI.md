# DataAPI

Note that all recommendations, whether specific to a coach or general, are associated with the "company" coach. Users have to enroll for this coach on the dashboard of Cysec before we can save the metadata which is offered on the Data API. In addition, the rating metadata (Score, Grade) are specific to a given coach. If a user hasn't crunched any questions there will be no score and grade.
 
## Preconditions
All Rest endpoints only support GET requests and deliver JSON output. If no valid keycloak token is included in the request header, you will be redirected to the keycloak login page. After successful authentication, the request will be automatically redirected.
 
For completeness: A valid user requires all of the below attributes:
* oidc_claim_preferred_username
* oidc_claim_email
* oidc_claim_company
* oidc_claim_given_name
* oidc_claim_family_name
 
Otherwise, the application will respond with 400 Bad Request. (We are working on a decent error page to inform users about this).
 
## Skills
https://wwwtest.smesec.eu/cysec-eauth/api/rest/coaches/skills
Example:
```
[  
   {  
      "mvalue":[  
         {  
            "key":"strength",
            "stringValue":"20"
         },
         {  
            "key":"know-how",
            "stringValue":"20"
         },
         {  
            "key":"endurance",
            "stringValue":"20"
         }
      ],
      "key":"_cysec.skills"
   }
]
```
Rating
https://wwwtest.smesec.eu/cysec-eauth/api/rest/coaches/{id}/rating
Where {id} is a path parameter to be replaced with a specific coach id.
Example:
```
{  
   "mvalue":[  
      {  
         "key":"micro_score",
         "stringValue":"20"
      },
      {  
         "key":"micro_grade",
         "stringValue":"C"
      },
      {  
         "key":"image",
         "binaryValue":"iVBOR......"
      }
   ],
   "key":"_cysec.rating"
}
 ```
## Recommendations:
For all recommendations:
https://wwwtest.smesec.eu/cysec-eauth/api/rest/coaches/recommendations
 
for general ones:
https://wwwtest.smesec.eu/cysec-eauth/api/rest/coaches/recommendations?show=generel
 
for specific ones:
https://wwwtest.smesec.eu/cysec-eauth/api/rest/coaches/recommendations?show=specific
 
Example:
```
[  
   {  
      "mvalue":[  
         {  
            "key":"name",
            "stringValue":"Coach recommendation2"
         },
         {  
            "key":"description",
            "stringValue":"Please fill out Company coach 2 first"
         },
         {  
            "key":"order",
            "stringValue":"1"
         }
      ],
      "key":"_cysec.recommended.MyRecommendation2"
   },
   {  
      "mvalue":[  
         {  
            "key":"name",
            "stringValue":"Coach recommendation"
         },
         {  
            "key":"description",
            "stringValue":"Please continue with Patch management coach."
         },
         {  
            "key":"order",
            "stringValue":"3"
         }
      ],
      "key":"_cysec.recommended"
   }
]
```



