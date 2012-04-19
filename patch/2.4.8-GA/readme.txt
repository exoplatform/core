Summary
	* Issue title: Allow to use ConversationStateListener with StandaloneContainer
	* CCP Issue:  n/a
	* Product Jira Issue: COR-249.
	* Complexity: Low

Proposal
	 
Problem description

What is the problem to fix?
	* Allow to use ConversationStateListener with StandaloneContainer

Fix description

Problem analysis
	* We used different ExoContainer but always return PortalContainer

How is the problem fixed?
	* Added by determining the current container and return required ExoConteiner

Tests to perform

Reproduction test
	* no

Tests performed at DevLevel
	* Manual testing, functional testing

Tests performed at Support Level
	* Build and check regression in PLF server

Tests performed at QA
	* n/a 
		
Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* n/a

Changes in Selenium scripts 
	* n/a

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* no

Configuration changes

Configuration changes:
	* no

Will previous configuration continue to work?
	* yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    Function or ClassName change: no
    Data (template, node type) migration/upgrade: no

Is there a performance risk/cost?
	* no

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
