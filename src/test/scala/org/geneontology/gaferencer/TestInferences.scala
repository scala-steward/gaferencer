package org.geneontology.gaferencer

import scala.collection.JavaConverters._
import scala.io.Source

import utest._

import org.prefixcommons.CurieUtil
import org.semanticweb.owlapi.apibinding.OWLManager
import org.phenoscape.scowl._

object TestInferences extends TestSuite {

  val InvolvedIn = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002331")
  val OccursIn = ObjectProperty("http://purl.obolibrary.org/obo/BFO_0000066")
  val Regulates = ObjectProperty("http://purl.obolibrary.org/obo/RO_0002211")

  val tests = Tests {

    "Check inferred annotations" - {
      val manager = OWLManager.createOWLOntologyManager()
      val ontology = manager.loadOntologyFromOntologyDocument(this.getClass.getResourceAsStream("go_xp_predictor_test_subset.ofn"))
      val cu = MultiCurieUtil(Seq(new CurieUtil(Map("GO" -> "http://purl.obolibrary.org/obo/GO_").asJava)))
      val gaferences = Gaferencer.processGAF(Source.fromInputStream(this.getClass.getResourceAsStream("xp_inference_test.gaf"), "UTF-8"), ontology, cu)
      val grouped = gaferences.groupBy(_.tuple)
      assert(grouped(GAFTuple(Class("http://purl.obolibrary.org/obo/NCBITaxon_10090"), Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_0006412")), Set(Link(OccursIn, Class("http://purl.obolibrary.org/obo/GO_0005739")))))
        .head.inferences(Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_0032543"))))
      assert(grouped(GAFTuple(Class("http://purl.obolibrary.org/obo/NCBITaxon_10090"), Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_0048585")), Set(Link(Regulates, Class("http://purl.obolibrary.org/obo/GO_0051594")))))
        .head.inferences(Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_2000970"))))
    }

    "Check taxon constraints" - {
      val manager = OWLManager.createOWLOntologyManager()
      val ontology = manager.loadOntologyFromOntologyDocument(this.getClass.getResourceAsStream("taxon_constraint_test.ofn"))
      val cu = MultiCurieUtil(Seq(new CurieUtil(Map("GO" -> "http://purl.obolibrary.org/obo/GO_").asJava)))
      val gaferences = Gaferencer.processGAF(Source.fromInputStream(this.getClass.getResourceAsStream("taxon_constraint_test.gaf"), "UTF-8"), ontology, cu)
      val grouped = gaferences.groupBy(_.tuple)
      assert(grouped(GAFTuple(Class("http://purl.obolibrary.org/obo/NCBITaxon_10090"), Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_0009272")), Set.empty)).head.satisfiable == false)
      assert(grouped.get(GAFTuple(Class("http://purl.obolibrary.org/obo/NCBITaxon_40296"), Link(InvolvedIn, Class("http://purl.obolibrary.org/obo/GO_0009272")), Set.empty)).isEmpty)
    }

  }

}