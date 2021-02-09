# CySeC main repo

This is the main repo for the SMESEC project.

## Development dependencies

* Java 8
* Tomcat 9
* Maven

## Branching

Branching was loosely based on the Gitflow branching model, which should be adhered
to more rigidly as the project moves to a Beta phase.

The Gitflow workflow is described f.ex. [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

This model should make for stable release branches and avoid any merge conflict issues.

The "master" branch is for stable releases and should rarely see commits.

The "dev" branch is for testing and should see regular changes, with additional commits
as releases approach.

Feature and bugfix branches are based on "dev" unless the bugfix is specifically meant to be applied to
a specific already-released version.

## License
See MIT license