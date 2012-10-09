eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var module = new Module();
  
  module.version = "${project.version}" ;//
  module.relativeMavenRepo = "org/exoplatform/core" ;
  module.relativeSRCRepo = "core" ;
  module.name = "core" ;

  module.component = {}
     
  module.component.ldap = new Project("org.exoplatform.core", "exo.core.component.ldap", "jar", module.version) ;

  module.component.database = 
    new Project("org.exoplatform.core", "exo.core.component.database", "jar", module.version) .
    addDependency(new Project("org.hibernate", "hibernate-core", "jar", "4.1.6.Final")).
    addDependency(new Project("org.hibernate", "hibernate-c3p0", "jar", "4.1.6.Final")).
    addDependency(new Project("org.javassist", "javassist", "jar", "3.15.0-GA")).
    addDependency(new Project("org.antlr", "antlr-runtime", "jar", "3.2")).
    addDependency(new Project("javax.transaction", "jta", "jar", "1.1")).
    addDependency(new Project("org.hsqldb", "hsqldb", "jar", "2.0.0")).
    addDependency(new Project("javax.resource", "connector-api", "jar", "1.5"));

  module.component.documents =
    new Project("org.exoplatform.core", "exo.core.component.document", "jar", module.version).
    addDependency(new Project("org.apache.pdfbox", "pdfbox", "jar", "1.6.0")).
    addDependency(new Project("org.apache.pdfbox", "fontbox", "jar", "1.6.0")).
    addDependency(new Project("org.apache.pdfbox", "jempbox", "jar", "1.6.0")).    
    addDependency(new Project("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec", "jar", "1.0.1")).
    addDependency(new Project("org.apache.poi", "poi", "jar", "3.8")).
    addDependency(new Project("org.apache.poi", "poi-scratchpad", "jar", "3.8")).
    addDependency(new Project("org.apache.poi", "poi-ooxml", "jar", "3.8")).
    addDependency(new Project("org.apache.tika", "tika-core", "jar", "1.1")).    
    addDependency(new Project("org.apache.tika", "tika-parsers", "jar", "1.1")).    
    addDependency(new Project("org.apache.xmlbeans", "xmlbeans", "jar", "2.3.0"));
    
  module.component.organization = 
    new Project("org.exoplatform.core", "exo.core.component.organization.api", "jar", module.version).
    addDependency(new Project("org.exoplatform.core", "exo.core.component.organization.jdbc", "jar", module.version));
	
  module.component.organization.ldap =
	new Project("org.exoplatform.core", "exo.core.component.organization.ldap", "jar", module.version);
	
  module.component.security = {}
  module.component.security.core = 
    new Project("org.exoplatform.core", "exo.core.component.security.core", "jar", module.version) ;

  module.component.xmlProcessing = 
    new Project("org.exoplatform.core", "exo.core.component.xml-processing", "jar", module.version) ;

  module.component.resources = new Project("org.exoplatform.core", "exo.core.component.database", "jar", module.version);
    
  module.component.scriptGroovy = new Project("org.exoplatform.core", "exo.core.component.script.groovy", "jar", module.version) ;
  return module;
}
