/***********************************************************************
* Copyright (c) 2013-2016 Commonwealth Computer Research, Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0
* which accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*************************************************************************/

package org.locationtech.geomesa.accumulo.index

import org.junit.runner.RunWith
import org.locationtech.geomesa.accumulo.TestWithDataStore
import org.locationtech.geomesa.accumulo.data.{DEFAULT_ENCODING, WritableFeature}
import org.locationtech.geomesa.accumulo.index.attribute.AttributeIndex
import org.locationtech.geomesa.features.avro.AvroSimpleFeatureFactory
import org.locationtech.geomesa.utils.text.WKTUtils
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AttributeTableTest extends Specification with TestWithDataStore {

  override val spec =
    "name:String:index=true,age:Integer:index=true,*geom:Point:srid=4326,dtg:Date:index=true"

  "AttributeTable" should {

    "encode mutations for attribute index" in {
      val feature = AvroSimpleFeatureFactory.buildAvroFeature(sft, List(), "id1")
      val geom = WKTUtils.read("POINT(45.0 49.0)")
      feature.setDefaultGeometry(geom)
      feature.setAttribute("name","fred")
      feature.setAttribute("age",50.asInstanceOf[Any])

      val toWrite = WritableFeature.toWritableFeature(sft, DEFAULT_ENCODING, "")(feature)
      val mutations = AttributeIndex.writer(sft, ds)(toWrite)
      mutations.size mustEqual 2 // for null date
      mutations.map(_.getUpdates.size()) must contain(beEqualTo(1)).foreach
      mutations.map(_.getUpdates.get(0).isDeleted) must contain(beEqualTo(false)).foreach
    }

    "encode mutations for delete attribute index" in {
      val descriptors = sft.getAttributeDescriptors

      val feature = AvroSimpleFeatureFactory.buildAvroFeature(sft, List(), "id1")
      val geom = WKTUtils.read("POINT(45.0 49.0)")
      feature.setDefaultGeometry(geom)
      feature.setAttribute("name","fred")
      feature.setAttribute("age",50.asInstanceOf[Any])

      val toWrite = WritableFeature.toWritableFeature(sft, DEFAULT_ENCODING, "")(feature)
      val mutations = AttributeIndex.remover(sft, ds)(toWrite)
      mutations.size mustEqual 2 // for null date
      mutations.map(_.getUpdates.size()) must contain(beEqualTo(1)).foreach
      mutations.map(_.getUpdates.get(0).isDeleted) must contain(beEqualTo(true)).foreach
    }
  }

}
