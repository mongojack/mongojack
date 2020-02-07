# How to contribute

The community is essential to spread the bliss of development. We improve our open source libraries as far as we need to use them in our projects. But we can't think of all the use cases you might have. So we'd like to have you participating in our bliss. We want to keep it as easy as possible to contribute changes. There are just a few guidelines that we need contributors to follow so that we can have a chance of keeping on top of things.

## Writing issues

* Submit a new ticket for your issue, assuming [one does not already exist](https://github.com/devbliss/mongojack/issues/new).
* Clearly describe the issue including, in the case of bugs, steps to reproduce it.

## Making Changes

* [Fork this repository](https://help.github.com/articles/fork-a-repo) into your own github account
* Create a local copy of this repository on your machine. `git clone git@github.com:{your_username}/mongojack.git`
* Create a topic branch on which you want to base your work. `git checkout -b {branchname}`
  * This is usually the master branch.
  * Only target release branches if you are certain your fix must be on that branch.
  * Please avoid working directly on the `master` branch.
* Import the `mongojack-format.xml` (Eclipse IDE format definitions) file into your IDE of choice to have the correct file formatting in place.
* Make commits of logical units. `git add {file}; git commit -m "{commit message}"`
* Make sure your commit messages are informative.
* Make sure you have added the necessary tests for your changes.
* Run _all_ the tests to ensure nothing else was broken accidentally. `mvn test`
* Add your contribution information
  * Add your name and email to the contributors section in the pom.xml
  * If you added new files make sure you added your name to it with the author tag `@author`
  * If you added new files make sure you added a license of your choice to that file.

## Submitting Changes

* Push your changes to the topic branch in your fork of the repository. `git push origin {branchname}`
* Submit a pull request to the repository in the devbliss organization. [see this link](https://help.github.com/articles/creating-a-pull-request)
* Write an informative pull request description.
* Please don't add information to `changelog.md`.

## How to make a release

User with appropriate rights can create two kind of releases: snaphost and normal releases. We are using the [maven -release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/). Be sure your sonatype credentials are present in your settings.xml:

```
<server>
  <id>sonatype-nexus-snapshots</id>
  <username>{YOUR_USERNAME}</username>
  <password>{YOUR_PASSWORD}</password>
</server>
<server>
  <id>sonatype-nexus-staging</id>
  <username>{YOUR_USERNAME}</username>
  <password>{YOUR_PASSWORD}</password>
</server>
```

### For a normal release:
* mvn release:clean
* mvn release:prepare <-> prepare for a release in SCM
* mvn release:perform <-> perform a release from SCM

You can now check that there is a new tag on [github](https://github.com/devbliss/mongojack/releases). Then, on [oss.sonatype.org](http://oss.sonatype.org), after logging in, the mongojack release can be found under `Staging Repositories`. Select it and click on `Close` then on `Release`. After a while, the Maven Central repository will be updated and you can find the new version of mongojack under [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22mongojack%22).

To update the documentation, be sure you have the correct server configuration in your settings.xml:
```
<server>
  <id>github-project-site</id>
  <username>git</username>
</server>
```
Then checkout the tag you just created and run run `mvn clean site-deploy` to create and deploy the site. Check on [mongojack.org](http://mongojack.org) that the update was successful.

### For a snapshot release:
* mvn clean deploy <-> deploy a snapshot version of mongojack on https://oss.sonatype.org/index.html
