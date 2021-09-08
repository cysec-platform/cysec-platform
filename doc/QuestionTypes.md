# Questions

The questionnaire platform supports a variety of question types to capture user information.

## Types
The following table lists the supported question types and their properties:


| type                     | id          | description |
| ------------------------ | ----------- | ----------- |
| yesno                    | yesno       | Only allows either yes or no as answers |
| A                        | astar       | Answers can be customized but only one may be selected |
| multiple choice                    | astar       | Extension of type A: Allows multiple answer to be stored |
| exclusive M.C          | astarexclusive       | Users may selected multiple answers or select a so called "None" option that disables all answers |
| text                     | text        | Saves user entered text. Doesnt allow control chars like <,> |
| date                     | date        | A special case of text. Takes input from a datepicker and saves in DD-MM-YYY format |
| likert scale                    | likert        | Takes a single input from a radio button rangeing from 1 to 5 |


## Attributes
The platform identifies the question based on its "id" attribute (see above table). In addition, like blocks a question can be enriched with documentation and images on the coach eu.smesec.totalcross.controllers.views. Use the "instruction" attribute for the documentation and "attachment" for pictures.
 ```
 <xs:sequence>
      <xs:element ref="text"/>
      <xs:element ref="attachments"/>
      <xs:element ref="options" minOccurs="0"/>
      <xs:element ref="listeners"/>
      <xs:element ref="metadata" minOccurs="0" maxOccurs="unbounded" />
      <xs:element ref="instruction"/>
  </xs:sequence>
  
  <xs:attribute name="id" type="xs:ID" use="required"/>
  <xs:attribute name="type" type="xs:string" use="required"/>
```

## Answers
The coach platform stores User input to questions in `Answer` objects. While most answers are in essence text based - and therefore use the `text` attribute to store value, there is one difference regarding `Astarexclusive` questions. Those questions make use of the `aid-list` atttribute as this is a List of `Option` objects that simplifies retrieval and storage of given options.
As a rule of thum: Focus on the `aid-list` and disregard the `text` attribute for Astarexclusive, respectively the opposite for all other types.
```
  <answer aid-list="q1oNone" qid="q1">
  <text>q1o1</text>
  </answer>
```
## Access of Coach questions
The library holds a reference to an `ILibCal` object that provides access to the coach templates. Note that this access is read-only and that new questions have to be added to the coach template.

```
  Questionnaire getCoach(String coachId);

  List<Questionnaire> getCoaches();
```

## Example
```
<question id="q1" type="Astarexclusive">
  <text>Do you code?</text>
  <attachments/>
  <options correct="">
    <option id="q1o1">
      <text>Java</text>
      <attachments></attachments>
    </option>
    <option id="q1o2">
      <text>C++</text>
      <attachments></attachments>
    </option>
    <option id="q1o3">
      <text>Perl</text>
      <attachments></attachments>
    </option>
    <option id="q1o4">
      <text>C</text>
      <attachments></attachments>
    </option>
    <option id="q1oNone">
      <text>None</text>
      <attachments></attachments>
    </option>
  </options>
  <listeners/>
  <instruction>
      <text>
          &lt;p&gt;&lt;strong&gt;Train your employees! &lt;/strong&gt;The best security system in the world is still vulnerable if employees don&amp;rsquo;t understand their roles and responsibilities in safeguarding sensitive data and protecting company resources.&lt;/p&gt; &lt;p style=&quot;text-align: right;&quot;&gt;&lt;a title=&quot;Is Your Staff Informed on Security Awareness Education? 05/10/2018 &amp;nbsp;|&amp;nbsp; By:&amp;nbsp;Brian Willis&quot; href=&quot;https://www.lbmcinformationsecurity.com/blog/is-your-staff-informed-on-security-awareness-education&quot; target=&quot;_blank&quot;&gt;More info ...&lt;/a&gt;&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Why training is important: &lt;/strong&gt;In 2015 a UK study has shown that inadvertent human error (48%), lack of staff awareness (33%), and weaknesses in assessing people (17%) were important factors in causing the worst successful attacks.&lt;/p&gt; &lt;p style=&quot;text-align: right;&quot;&gt;&lt;a title=&quot;Information Security Breaches Survey 2015 &quot; href=&quot;https://www.pwc.co.uk/assets/pdf/2015-isbs-technical-report-blue-03.pdf&quot; target=&quot;_blank&quot;&gt;More info ...&lt;/a&gt;&lt;/p&gt; &lt;p&gt;&lt;strong&gt;What to train:&lt;/strong&gt;&lt;/p&gt; &lt;ul&gt; &lt;li&gt;&lt;strong&gt;Introduction to Cybersecurity: &lt;/strong&gt;this block should introduce the relevant cyber threats, the costs of cleaning up after an attack, and allow participants to detect and understand attacks targeted at your company.&lt;/li&gt; &lt;li&gt;&lt;strong&gt;Attack Responses: &lt;/strong&gt;this block should train countermeasures to common attacks like password guessing, phishing, infected web pages, insecure software, and social engineering. The block should train your employees in how to prevent data leakage and to react to an incident.&lt;/li&gt; &lt;/ul&gt; &lt;p style=&quot;text-align: right;&quot;&gt;&lt;a title=&quot;Gardner, Bill, and Valerie Thomas.&amp;nbsp;Building an information security awareness program: Defending against social engineering and technical threats. Elsevier, 2014.&quot; href=&quot;https://www.researchgate.net/profile/Bill_Gardner3/publication/291092430_Building_an_Information_Security_Awareness_Program_Defending_Against_Social_Engineering_and_Technical_Threats_1st_Edition/links/59b5ae7a458515a5b4939fde/Building-an-Information-Security-Awareness-Program-Defending-Against-Social-Engineering-and-Technical-Threats-1st-Edition.pdf&quot; target=&quot;_blank&quot;&gt;More info ...&lt;/a&gt;&lt;/p&gt; &lt;p style=&quot;text-align: left;&quot;&gt;SMESEC offers you &lt;a href=&quot;https://www.securityaware.me/index.php&quot; target=&quot;_blank&quot;&gt;online training&lt;/a&gt;.&lt;/p&gt; &lt;p style=&quot;text-align: left;&quot;&gt;&amp;nbsp;&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Who in your company should do the training:&lt;/strong&gt;&lt;/p&gt; &lt;ul&gt; &lt;li&gt;&lt;strong&gt;Managers&lt;/strong&gt;: the managers will influence the employees. The training should allow them to become a role model.&lt;/li&gt; &lt;li&gt;&lt;strong&gt;Employees&lt;/strong&gt;: the employees will safeguard your company. The training should teach good behaviour, reduce risks, and mitigate the consequences of an incident.&lt;/li&gt; &lt;li&gt;&lt;strong&gt;IT Staff: &lt;/strong&gt;people who are handling sensitive information assets or take cyber security measures in your company should be updated to decrease the vulnerabilities.&lt;/li&gt; &lt;/ul&gt; &lt;p style=&quot;text-align: right;&quot;&gt;&lt;a title=&quot;Kaspersky Lab. Top 10 tips for educating employees about cybersecurity&quot; href=&quot;http://go.kaspersky.com/rs/kaspersky1/images/Top_10_Tips_For_Educating_Employees_About_Cybersecurity_eBook.pdf&quot; target=&quot;_blank&quot;&gt;More info...&lt;/a&gt;&lt;/p&gt;&lt;p&gt;&amp;nbsp;&lt;/p&gt;
      </text>
      </attachments>
      </instruction>
</question>
```
