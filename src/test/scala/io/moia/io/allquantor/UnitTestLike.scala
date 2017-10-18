package io.allquantor

import org.scalatest.{ Matchers, WordSpec, WordSpecLike }

/**
  * Unit tests for classpath-error-showcase.
  */
trait UnitTestLike extends WordSpecLike with Matchers

/**
  * Unit tests for classpath-error-showcase as an abstract class (improves compilation speed).
  */
abstract class UnitTest extends WordSpec with Matchers