
Releasing
-----------

This section is relevant only for project maintainers.

NOTE: [do not release from any location which load balances outgoing HTTP requests between internet connections](https://issues.sonatype.org/browse/OSSRH-6262)

Make sure you have a server setup in the file `~/.m2/settings.xml` like:

    <settings>
      <servers>
        <server>
          <id>ossrh</id>
          <username>[your sonatype username]</username>
          <password>[your sonatype password]</password>
        </server>
      </servers>
    </settings>

You will also need a working GPG setup with a valid default signing key, the information for which has been publicly shared.

Now run the following:

    mvn release:prepare
    mvn release:perform

This should run all tests, build the release, tag the repository, deploy the release, and deploy and close the staging repository.

To deploy the latest version of the website:

    git checkout <release tag you want to build the site for>
    mvn clean site-deploy
