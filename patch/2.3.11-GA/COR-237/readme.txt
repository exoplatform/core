Summary

    * Status: minConnection and maxConnection params in the ldap configuration are not used
    * CCP Issue: CCP-1032, Product Jira Issue: COR-237.
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?

    * minConnection and maxConnection params in the ldap configuration are not used

Fix description

How is the problem fixed?
Use minConnection and maxConnection to set up com.sun.jndi.ldap.connect.pool.initsize and com.sun.jndi.ldap.connect.pool.prefsize, precisely:

    * com.sun.jndi.ldap.connect.pool.initsize and com.sun.jndi.ldap.connect.pool.prefsize are set to minConnection if minConnection > 0
    * com.sun.jndi.ldap.connect.pool.maxsize are set to maxConnection if maxConnection > 0

Patch file: COR-237.patch

Tests to perform

Reproduction test

    * org.exoplatform.services.ldap.impl.LDAPServiceImpl does not use minConnection and maxConnection params to init the ldap connection pool (should be used for com.sun.jndi.ldap.connect.pool.initsize and com.sun.jndi.ldap.connect.pool.maxsize env params).

Tests performed at DevLevel
  * Manual testing PLF 3.0.5 with configured LDAPOrganizationService

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

    * Patch approved.

Support Comment

    * Patch validated by QA on behalf of Support.

QA Feedbacks
* Patch validated

