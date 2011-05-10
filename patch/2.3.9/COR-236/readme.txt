Summary

    Status: Case sensitivity Problem with Oracle Virtual Directory and SQL Server
    CCP Issue: CCP-910, Product Jira Issue: COR-236. Backport of COR-233.
    Complexity: Low

The Proposal
Problem description

What is the problem to fix?
Using Oracle Virtual Directory + MS SQL Server, there are problems with upper-case LDAP prefix "OU".
Queries must be lower-cased to avoid such problems.
Fix description

How is the problem fixed?

    Replace uppercased "OU" and "CN" by lower cased on "ou" and "cn" respectively.

Patch information:
Patch files: COR-236.patch

Tests to perform

Reproduction test

    Reproduction can be only performed manually with Oracle Virtual Directory + MS SQL Server

Tests performed at DevLevel

    Tomcat AS + LDAP Organization service + AD

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
    No

Configuration changes

Configuration changes:

    By default, group and user DN keys are in lower case, but it is possible to configure via initialization params if needed:

    <value-param>
      <name>ldap.userDN.key</name>
      <description>The key used to compose user DN</description>^
      <value>cn</value>
    </value-param>
    <value-param>
      <name>ldap.groupDN.key</name>
      <description>The key used to compose group DN</description>^
      <value>ou</value>
    </value-param>

Will previous configuration continue to work?
    Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
    No

Is there a performance risk/cost?
    No

Validation (PM/Support/QA)

PM Comment
* Approved

Support Comment
* Validated

QA Feedbacks
*

