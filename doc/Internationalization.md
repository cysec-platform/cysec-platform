# Internationalization

This chapter is about how multriple language are implemented.

## Platform
Each session registers it's locale. The locale is set by fallowing 

1. SAML authentication
2. User settings
3. Browser

The platform uses `GNU gettext` to translate the forntend text.
If you are planning to translate some text, make sure [GNU gettext](https://www.gnu.org/software/gettext/manual/gettext.html) is installed on your target machine.

### Languages
All source text is defined in a po template file `src/main/resources/po/cysec.pot`.
The source language is english.
`GNU gettext` extracts all translation texts in java classes,
which are located in `src/main/java/eu/smesec/cysec/platform/core/messages`
and writes them into the po template file.

For each additional language, a po file must be created (eg. `de.po` for german).
The header of a new po file must be modified by the author itself:
```
msgid ""
msgstr ""
"Project-Id-Version: PACKAGE VERSION\n"
"Report-Msgid-Bugs-To: \n"
"POT-Creation-Date: 2019-11-15 11:07+0100\n"
"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\n"
"Last-Translator: FULL NAME <EMAIL@ADDRESS>\n"
"Language-Team: LANGUAGE <LL@li.org>\n"
"Language: \n"
"MIME-Version: 1.0\n"
"Content-Type: text/plain; charset=CHARSET\n"
"Content-Transfer-Encoding: 8bit\n"
"Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;\n"
```

`GNU gettext` can now merge the content of the po template file into another po file.
After that, you can translate the texts inside the po file.
And if all translation is done, `GNU gettext` can finally create the resource bundles,
which by be used at runtime.

#### Command line

- To extract the texts into the po template file, use the `xgettext` command.
- To merge the  po template file into the po files, use the `msgmerge` command.
- To create the java resource bundles, use the `msgfmt` command.

#### Maven plugin
If you are using [Maven](https://maven.apache.org/) you can use the maven gettext plugin to handle the commands.

- To extract the texts into the po template file, use `gettext:gettext`.
- To merge the  po template file into the po files, use `gettext:merge`.
- To create the java resource bundles, use `gettext:dist`.


### Change Settings

The user settings can be changed on the user management site.


## Coaches
TDB
