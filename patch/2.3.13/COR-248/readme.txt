Summary
	* Title: DocumentReadException and IOException related to PDFBOX
	* CCP Issue:  CCP-1233 
	* Product Jira Issue: COR-248.
	* Complexity: Low

Proposal
 
Problem description

What is the problem to fix?
	* When reindexing the JCR we encountered a large number of exceptions such as DocumentReadException, IOException, DocumentReadException

Fix description

Problem analysis
	* pdfbox 1.1.0 contains some bugs

How is the problem fixed?
	* Applied patch from https://issues.apache.org/jira/browse/PDFBOX-790 to the pdfbox-1.1.0 and deployed new pdfbox artifacts:
		* org.apache.pdfbox pdfbox 1.1.0-eXo01
		* org.apache.pdfbox fontbox 1.1.0-eXo01
		* org.apache.pdfbox jempbox 1.1.0-eXo01
	* The cmap-parser doesn't ignore additional whitespaces with in a begin char mapping

Patch file: COR-248.patch

Tests to perform

Reproduction test
	* Start AS, try to put pdf-file  from https://issues.apache.org/jira/browse/PDFBOX-790 via WebDAV, You can see exceptions on console

Tests performed at DevLevel
	* Manual testing, functional testing in core and jcr projects

Tests performed at Support Level
	*

Tests performed at QA
	*

Changes in Test Referential
	* N/A

Changes in SNIFF/FUNC/REG tests
	* N/A

Changes in Selenium scripts 
	* N/A

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

	* Function or ClassName change: No
	* Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*

