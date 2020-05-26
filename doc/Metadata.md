# Metadata

Metadata objects are used to store `MValues` as Key-Value pairs.
The are generated from `questionnaire.xsd`.

## Namespaces
A namespace consists of the key of the metadata and the key of the mvalue.
Fallowing namespaces are used:


| Coaches  | metadata                      | mvalue      | description |
| -------- | ----------------------------: | ----------- | ----------- |
| Company  | _cysec.skills                 | strength    | The actual strength of the company |
|          |                               | know-how    | The actual know-how of the company |
|          |                               | endurance   | The actual endurance of the company |
| Company  | _cysec.recommended.\<action\> | name        | The title of the recommendation |
|          |                               | description | The description of the recommendation |
|          |                               | order       | The priority of the recommendation |
| Company  | _cysec.badges.\<class\>       | name        | The name of a badge class |
|          |                               | image       | The image of a badge class |
|          |                               | description | The description of a badge class |hboard (required) |
| All      | _cysec.rating                 | micro_score | The score which has been achieved |
|          |                               | micro_grade | The grade which has been achieved |
| All      | _cysec.resume                 | qid         | The last question the user was editing |

## Usage
The util class `MetadataUtils` provides some helptful methods to use the Metadata or Mavlues
and provides all metadata-keys and Mvalue-keys.

### Mvalues
Mvalues are the entries of a metadata. The have a unique `key` and a `value`. The value can be a normal String or a binary String.

- Create:
```
Mvalue stringValue = MetadataUtils.createMvalueStr(String key, String value);

Mvalue binaryValue = MetadataUtils.createMavlueBin(String key, String value);
```

- Parse:
```
MetadataUtils.SimpleMvalue sm = MetadataUtils.parseMvalue(Mvalue mvalue);
boolean isStringType = sm.isStringType();
String value = sm.getValue();
```

- Key
```
String key = mvalue.getKey();
```

### Metadata
Metadata stores multiple Mvalues. Each metadata has a unique key and some mvalues.

- Create
```
Metadata md = MetadataUtils.createMetadata(String key, Collection<Mvalue> mvalues);
```

- Parse Mvalues
```
List<Mvalues> mvalues = md.getMavlue();
Map<String, MetadataUtils.SimpleMvalue> simpleValues = MetadataUtils.parseMvalues(Collection<Mvalue> mvalues)
```

- Key
```
String key = md.getKey();
```