Summary

    * Status: Improve performance of LDAP organization service
    * CCP Issue: CCP-1032, Product Jira Issue: COR-34.
    * Complexity: medium

The Proposal
Problem description

What is the problem to fix?

    * Improve performance of LDAP organization service

Fix description

How is the problem fixed?

    * Cache implementation

Patch file: COR-34.patch

Tests to perform

Reproduction test

    * LDAP organization service is pretty slow and doesn't support cache.

Tests performed at DevLevel
* Manual testing Tomcat AS with LDAP organization service

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
*

QA Feedbacks
*

