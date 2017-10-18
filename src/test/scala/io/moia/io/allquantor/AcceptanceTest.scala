package io.allquantor

import org.scalatest.{ Matchers, SequentialNestedSuiteExecution, GivenWhenThen, FeatureSpec }

/**
  * Acceptance tests for classpath-error-showcase
  */
abstract class AcceptanceTest extends FeatureSpec with GivenWhenThen with SequentialNestedSuiteExecution with Matchers