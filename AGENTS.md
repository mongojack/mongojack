# AGENTS.md

Guidance for AI coding agents working in this repository. It applies to human contributors too, alongside
[CONTRIBUTING.md](CONTRIBUTING.md).

## The project

MongoJack maps MongoDB documents to Java objects using Jackson. It is a library published to Maven Central, so a change
here can affect applications, and data already stored in their databases.

- Java 17, built with Maven
- Jackson 3 (`tools.jackson`) and the MongoDB synchronous driver (`mongodb-driver-sync`) 5.x

| Path | Contents |
|---|---|
| `src/main/java/org/mongojack/` | Public API |
| `src/main/java/org/mongojack/internal/` | Implementation details, not exported from the OSGi bundle |
| `src/test/java/org/mongojack/` | Tests, with shared model classes in `mock/` and database setup in `testing/` |
| `src/site/` | Project website, including the changelog |
| `specs/` | Specs, implementation plans and review walkthroughs, created by the first change that needs one (see [Making changes](#making-changes)) |

## Build and test

Tests start MongoDB in a container through Testcontainers, so Docker must be running.

```bash
mvn test
```

```bash
mvn test -Dtest=TestDBRefHandling
```

Before calling a change finished, run the same command as [CI](.github/workflows/build.yml). CI runs it on Java 17, 21
and 25.

```bash
mvn -B -ntp clean verify -Prelease -Dgpg.skip -Dmaven.javadoc.failOnError=true
```

Do not run `mvn deploy`, `mvn release:*` or `mvn site-deploy` unless a maintainer asks for it. They publish artifacts,
tags or the website. Releases are described in [RELEASE.md](RELEASE.md).

## Design practices

- **Keep it simple.** Solve the problem in front of you. Do not add options, abstractions or extension points for needs
  nobody has yet.
- **Give each class one clear job.** Prefer composition and small, focused methods.
- **Protect the public API.** Anything public in `org.mongojack` is used by other people's code. Deprecate before
  removing. A breaking change needs a maintainer's agreement and an entry in [MIGRATING.md](MIGRATING.md).
- **Protect stored data.** A change to how values are serialized or deserialized can break reading existing documents.
  Call it out even when no method signature changes.
- **Honor the driver's contracts.** `JacksonMongoCollection` is a `MongoCollection`. It should behave like one, adding
  Jackson mapping and nothing surprising.
- **Apply mapping consistently.** Operations in `MongoCollectionDecorator` pass filters, updates, pipelines and bulk
  writes through the `manage...` methods. New or changed operations must do the same, including every overload (with and
  without a `ClientSession`). After a driver upgrade, check for new interface methods, including default methods.
- **Assume concurrent use.** Collections, codec registries and codecs are shared between threads. Prefer immutable
  state, and use concurrent collections for caches.
- **Prefer public APIs of dependencies.** The remaining calls to underscore-prefixed `ObjectMapper` methods are marked
  `TODO jackson3`. Do not add calls to a dependency's internal methods without a comment explaining why there is no
  alternative.
- **Add dependencies reluctantly.** A new runtime dependency needs a maintainer's agreement. The `javax.persistence` and
  `jakarta.persistence` dependencies are optional, and the library must work without them.

## Security practices

- Treat values from callers and documents from the database as untrusted.
- Keep data separate from query syntax.
- Do not enable Jackson default typing, or otherwise let input choose which class is instantiated.
- Do not log document contents, connection strings or credentials.
- This repository is public. Do not describe an unfixed vulnerability in a spec, commit, issue or pull request. Raise it
  privately with a maintainer.

## Coding practices

- Format Java with [mongojack-format.xml](mongojack-format.xml): spaces, 4 per indent, lines up to 150 characters.
  [.editorconfig](.editorconfig) sets 2-space indents for `pom.xml`.
- Match the surrounding code. Do not reformat or refactor lines unrelated to your change.
- Use names that explain intent. Write comments to explain why, not to restate what the code does.
- Do not use language features or APIs newer than Java 17.
- Fail clearly. Keep the original cause when wrapping an exception, and do not silently swallow errors. Mapping failures
  use `MongoDatabindException` where it fits.
- Log through SLF4J, never `System.out` or `printStackTrace()`.
- Document public classes and methods with Javadoc. Javadoc errors fail CI.
- New files need a license header and an `@author` tag, as described in [CONTRIBUTING.md](CONTRIBUTING.md). Ask for the
  author's name rather than inventing one.
- Do not edit the changelog. Maintainers update it.

## Testing practices

- Every change in behavior comes with a test. For a bug, first write a test that fails because of it.
- Use JUnit Jupiter, with AssertJ for new assertions.
- Tests that need a database extend `MongoDBTestBase`.
- Test observable behavior. When serialization changes, check the stored document as well as the round trip.
- Never delete, disable or weaken a test just to make the build pass.

## Making changes

This project uses spec-driven development: agree on what to build, then how to build it, then build it.

### When a spec is needed

Follow the full workflow for any change to behavior, public API, stored data format, runtime dependencies, or the build
and release process.

It is not needed for typo, comment and documentation fixes, test-only cleanup, or dependency version bumps that need no
code changes. If you are unsure, write the spec. These smaller changes still follow
[Git and pull requests](#git-and-pull-requests).

### Where documents live

Each change gets its own folder, named for the date work started and a short description:

```
specs/YYYY-MM-DD-short-name/
  spec.md     what to build and why
  plan.md     how to build it
  review.md   walkthrough for the code reviewer
```

Commit these on the same branch as the code, so they are reviewed and merged together. Put any supporting material, such
as diagrams or research notes, in the same folder.

### 1. Write the spec

Start `spec.md` with a status line (`Draft`, `Approved` or `Implemented`), then cover:

- **Summary:** the change in a few sentences
- **Problem:** what is wrong or missing, and who it affects
- **Goals and non-goals**
- **Requirements:** a numbered list of behaviors, each one testable
- **Compatibility:** effects on public API, stored data and dependencies
- **Security:** new risks, or why there are none
- **Open questions**

**Stop and ask a maintainer to approve the spec.** Do not plan or implement while questions that affect the design are
open.

### 2. Write the implementation plan

In `plan.md`, cover:

- **Approach:** the design, and alternatives you rejected
- **Changes:** the files and classes involved
- **Test plan:** how each numbered requirement will be tested
- **Tasks:** a checklist of small steps, in order
- **Risks:** what could go wrong

**Stop and ask a maintainer to approve the plan.**

### 3. Implement

Work through the tasks, writing tests as you go, and check them off in `plan.md`. If the work shows that the spec or plan
is wrong, update the document. Ask for approval again if requirements or design change.

### 4. Verify

Run the CI command from [Build and test](#build-and-test). Check each requirement against a test or another concrete
result. Report failures and anything you could not verify. Then set the spec's status to `Implemented`.

### 5. Write the review walkthrough

Write `review.md` **before opening a pull request**. Its reader may not know this part of the code, so use short
sentences and plain language, and explain any term they might not know.

```markdown
# Review walkthrough: <title>

## Executive summary

Two to five sentences on the intent of the change: the problem, what is different now, and what it means for users.

[Spec](spec.md) · [Plan](plan.md)

## Changes

### 1. <What this group of changes achieves>

- [JacksonMongoCollection.java](../../src/main/java/org/mongojack/JacksonMongoCollection.java): what changed and why.

## Verification

What was run, the results, and what was not tested.

## Where to look closely

Risks, judgment calls and compatibility effects that deserve the reviewer's attention.

## Not included

Related work deliberately left out.
```

Rules for the walkthrough:

- Start with the executive summary.
- Group changes by purpose and order them for reading, with the core change first.
- Cover and link every changed file. Compare against `git diff --name-status master...HEAD`. Files with the same simple
  change may share an explanation, but each still gets a link.
- Use relative links, which start with `../../` for files at the repository root and below. Add line anchors such as
  `#L40-L62` where they help, and check them after the last code change.
- Name deleted files without linking them. For renamed files, link the new path and mention the old one.
- Describe what the change does, not just which lines moved.
- Check that every link resolves.
- If the code changes later, update the walkthrough.

### 6. Open the pull request

Put the executive summary in the pull request description, followed by a link to `review.md` on GitHub. Use a full URL,
because relative links do not work from a pull request description.

## Git and pull requests

- Branch from an up-to-date `master`. Never commit to it directly. (The Maven release process is the exception.)
- Make commits of logical units, with messages that explain the change.
- Keep a pull request to one purpose.
- Do not commit, push or open a pull request unless you have been asked to.
