@Grapes([
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.3'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.5'),
          @Grab(group='org.apache.jena', module='apache-jena-libs', version='3.1.0', type='pom')
        ])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*
import org.apache.jena.rdf.model.*
import org.apache.jena.util.*

MAX_DEPTH=50

def cli = new CliBuilder()
cli.with {
  usage: 'Self'
  h longOpt:'help', 'this information'
  i longOpt:'input', 'input OWL file', args:1, required:true
  o longOpt:'output', 'output file',args:1, required:true
}

def opt = cli.parse(args)
if( !opt ) {
  //  cli.usage()
  return
}
if( opt.h ) {
    cli.usage()
    return
}

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
def ont = manager.loadOntologyFromOntologyDocument(new File(opt.i))
OWLDataFactory fac = manager.getOWLDataFactory()
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
ElkReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)
ShortFormProvider prov = new SimpleShortFormProvider()

def fout = new PrintWriter(new BufferedWriter(new FileWriter(opt.o)))
ont.getClassesInSignature(false).each { cl ->
  def count = 0
    fout.print(prov.getShortForm(cl).replace("GO_", "go:"))
  reasoner.getSuperClasses(cl, false).getFlattened().each { sup ->
	if (sup == fac.getOWLThing()) return;
	fout.print("\t"+prov.getShortForm(sup).replace("GO_", "go:"))
  }
  fout.println("")
}
fout.flush()
fout.close()
