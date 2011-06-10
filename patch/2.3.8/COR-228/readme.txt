Summary

    Status: Make PdfDocumentReader.getProperties() use PdfBox instead of iText
    CCP Issue: N/A, Product Jira Issue: COR-228.
    Complexity: hard

The Proposal
Problem description

What is the problem to fix?

    Implementation that uses iText does not support many XMP metadata. Make new implementation of PdfDocumentReader.getProperties() using PdfBox instead of iText.

Fix description

How is the problem fixed?

    Use PdfBox to extract XMP metadata.
    iText was removed from code.

Patch information:
Patch file(s): COR-228.patch

Tests to perform:
Test performed at Support level
Test on 3 pdf files (metro.pdf, pfs_accapp.pdf, Train_union.06.Mai_2009.pdf): using Webdav or ECMS Content explorer (in a Content folder)

    Upload file from local to eXo PLF server
    Copy-paste file inside JCR folders
    Check how its name and title display

Tests performed at DevLevel

    Add these 3 PDF files into src/test/resources
    TestPropertiesExtraction and other core.document tests

Tests performed at QA
*

Documentation changes

Documentation changes:
    none

Configuration changes

Configuration changes:
    none

Will previous configuration continue to work?
    yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
    The behavior of PDFDocumentReader.getProperties based on PdfBox may be different than based on iText

Is there a performance risk/cost?
    none

Validation (PM/Support/QA)

PM Comment
* Patch approved by the PL

Support Comment
* Support review : patch validated

QA Feedbacks
*

