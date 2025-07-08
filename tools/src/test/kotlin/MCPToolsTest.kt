import com.mattbobambrose.mcp_utils.tools.LLMTool
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MCPToolsTest {

  // Test classes with various tool methods
  class TestTools {
    @LLMTool("Simple string tool")
    fun simpleStringTool(): String = "Hello World"

    @LLMTool("Tool with string parameter")
    fun stringParameterTool(message: String): String = "Echo: $message"

    @LLMTool("Tool with optional parameter")
    fun optionalParameterTool(message: String, suffix: String = "!!"): String = "$message$suffix"

    @LLMTool("Tool with integer parameter")
    fun integerParameterTool(number: Int): String = "Number: $number"

    @LLMTool("Tool with boolean parameter")
    fun booleanParameterTool(flag: Boolean): String = "Flag: $flag"

    @LLMTool("Tool with double parameter")
    fun doubleParameterTool(value: Double): String = "Value: $value"

    @LLMTool("Tool with float parameter")
    fun floatParameterTool(value: Float): String = "Value: $value"

    @LLMTool("Tool with list parameter")
    fun listParameterTool(items: List<String>): String = "Items: ${items.joinToString(", ")}"

    @LLMTool("Tool with set parameter")
    fun setParameterTool(items: Set<String>): String = "Items: ${items.joinToString(", ")}"

    @LLMTool("Tool with map parameter")
    fun mapParameterTool(data: Map<String, String>): String =
      "Data: ${data.entries.joinToString("; ") { "${it.key}=${it.value}" }}"

    @LLMTool("Tool returning list")
    fun listReturnTool(): List<String> = listOf("item1", "item2", "item3")

    @LLMTool("Tool returning set")
    fun setReturnTool(): Set<String> = setOf("unique1", "unique2", "unique3")

    @LLMTool("Tool that throws exception")
    fun exceptionTool(): String = throw RuntimeException("Test exception")

    // Method without annotation - should be ignored
    fun unannotatedMethod(): String = "Should not be registered"
  }

  class EmptyTools {
    // Class with no annotated methods
    fun regularMethod(): String = "Not a tool"
  }

  @Test
  fun testLLMToolAnnotationExists() {
    // Test that the LLMTool annotation class exists and has correct properties
    val annotation = LLMTool::class
    assertNotNull(annotation)

    // Test annotation on a method
    val testMethod = TestTools::class.functions.find { it.name == "simpleStringTool" }
    assertNotNull(testMethod)

    val llmToolAnnotation = testMethod.findAnnotation<LLMTool>()
    assertNotNull(llmToolAnnotation)
    assertEquals("Simple string tool", llmToolAnnotation.description)
  }

  @Test
  fun testAnnotatedMethodsDetection() {
    val testTools = TestTools()
    val objectClass = testTools::class

    val annotatedMethods = objectClass.functions.filter {
      it.findAnnotation<LLMTool>() != null
    }

    // Should find 13 annotated methods
    assertEquals(13, annotatedMethods.size)

    // Check specific methods are found
    val methodNames = annotatedMethods.map { it.name }
    assertTrue(methodNames.contains("simpleStringTool"))
    assertTrue(methodNames.contains("stringParameterTool"))
    assertTrue(methodNames.contains("optionalParameterTool"))
    assertTrue(methodNames.contains("listReturnTool"))
    assertTrue(methodNames.contains("setReturnTool"))
    assertTrue(methodNames.contains("exceptionTool"))

    // Check unannotated method is not found
    assertFalse(methodNames.contains("unannotatedMethod"))
  }

  @Test
  fun testParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "stringParameterTool" }
    assertNotNull(testMethod)

    val parameters = testMethod.valueParameters
    assertEquals(1, parameters.size)

    val param = parameters[0]
    assertEquals("message", param.name)
    assertEquals("java.lang.String", param.type.javaType.typeName)
    assertFalse(param.isOptional)
  }

  @Test
  fun testOptionalParameterDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "optionalParameterTool" }
    assertNotNull(testMethod)

    val parameters = testMethod.valueParameters
    assertEquals(2, parameters.size)

    val requiredParam = parameters.find { it.name == "message" }
    assertNotNull(requiredParam)
    assertFalse(requiredParam.isOptional)

    val optionalParam = parameters.find { it.name == "suffix" }
    assertNotNull(optionalParam)
    assertTrue(optionalParam.isOptional)
  }

  @Test
  fun testIntegerParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "integerParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("number", param.name)
    assertTrue(param.type.javaType.typeName in listOf("int", "java.lang.Integer"))
  }

  @Test
  fun testBooleanParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "booleanParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("flag", param.name)
    assertTrue(param.type.javaType.typeName in listOf("boolean", "java.lang.Boolean"))
  }

  @Test
  fun testDoubleParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "doubleParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("value", param.name)
    assertTrue(param.type.javaType.typeName in listOf("double", "java.lang.Double"))
  }

  @Test
  fun testListParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "listParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("items", param.name)
    assertTrue(
      param.type.javaType.typeName.startsWith("java.util.List") ||
          param.type.javaType.typeName.startsWith("kotlin.collections.List")
    )
  }

  @Test
  fun testSetParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "setParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("items", param.name)
    assertTrue(
      param.type.javaType.typeName.startsWith("java.util.Set") ||
          param.type.javaType.typeName.startsWith("kotlin.collections.Set")
    )
  }

  @Test
  fun testMapParameterTypeDetection() {
    val testMethod = TestTools::class.functions.find { it.name == "mapParameterTool" }
    assertNotNull(testMethod)

    val param = testMethod.valueParameters[0]
    assertEquals("data", param.name)
    assertTrue(
      param.type.javaType.typeName.startsWith("java.util.Map") ||
          param.type.javaType.typeName.startsWith("kotlin.collections.Map")
    )
  }

  @Test
  fun testEmptyToolsClass() {
    val emptyTools = EmptyTools()
    val objectClass = emptyTools::class

    val annotatedMethods = objectClass.functions.filter {
      it.findAnnotation<LLMTool>() != null
    }

    assertEquals(0, annotatedMethods.size)
  }

  @Test
  fun testMethodInvocation() {
    val testTools = TestTools()

    // Test simple method call
    val result1 = testTools.simpleStringTool()
    assertEquals("Hello World", result1)

    // Test method with parameter
    val result2 = testTools.stringParameterTool("test")
    assertEquals("Echo: test", result2)

    // Test method with optional parameter (using default)
    val result3 = testTools.optionalParameterTool("hello")
    assertEquals("hello!!", result3)

    // Test method with optional parameter (providing value)
    val result4 = testTools.optionalParameterTool("hello", "??")
    assertEquals("hello??", result4)
  }

  @Test
  fun testListReturnType() {
    val testTools = TestTools()
    val result = testTools.listReturnTool()

    assertTrue(result is List<*>)
    assertEquals(3, result.size)
    assertEquals("item1", result[0])
    assertEquals("item2", result[1])
    assertEquals("item3", result[2])
  }

  @Test
  fun testSetReturnType() {
    val testTools = TestTools()
    val result = testTools.setReturnTool()

    assertTrue(result is Set<*>)
    assertEquals(3, result.size)
    assertTrue(result.contains("unique1"))
    assertTrue(result.contains("unique2"))
    assertTrue(result.contains("unique3"))
  }

  @Test
  fun testExceptionHandling() {
    val testTools = TestTools()

    try {
      testTools.exceptionTool()
      assertTrue(false, "Expected exception was not thrown")
    } catch (e: RuntimeException) {
      assertEquals("Test exception", e.message)
    }
  }

  @Test
  fun testComplexParameterTypes() {
    val testTools = TestTools()

    // Test with list parameter
    val listResult = testTools.listParameterTool(listOf("a", "b", "c"))
    assertEquals("Items: a, b, c", listResult)

    // Test with set parameter
    val setResult = testTools.setParameterTool(setOf("x", "y", "z"))
    assertTrue(setResult.contains("x"))
    assertTrue(setResult.contains("y"))
    assertTrue(setResult.contains("z"))

    // Test with map parameter
    val mapResult = testTools.mapParameterTool(mapOf("key1" to "value1", "key2" to "value2"))
    assertTrue(mapResult.contains("key1=value1"))
    assertTrue(mapResult.contains("key2=value2"))
  }

  @Test
  fun testTypeConversionMapping() {
    // Test the type mapping logic used in AddTools
    fun getJsonSchemaType(javaTypeName: String): String {
      return when {
        javaTypeName == "java.lang.String" -> "string"
        javaTypeName in listOf("int", "java.lang.Integer") -> "integer"
        javaTypeName in listOf("boolean", "java.lang.Boolean") -> "boolean"
        javaTypeName in listOf("double", "java.lang.Double", "float", "java.lang.Float") -> "number"
        javaTypeName.startsWith("java.util.List") ||
            javaTypeName.startsWith("java.util.Set") ||
            javaTypeName.startsWith("kotlin.collections.List") ||
            javaTypeName.startsWith("kotlin.collections.Set") -> "array"

        javaTypeName.startsWith("java.util.Map") ||
            javaTypeName.startsWith("kotlin.collections.Map") -> "object"

        else -> "string"
      }
    }

    // Test using the actual type names that are now constants in AddTools
    assertEquals("string", getJsonSchemaType("java.lang.String"))
    assertEquals("integer", getJsonSchemaType("int"))
    assertEquals("integer", getJsonSchemaType("java.lang.Integer"))
    assertEquals("boolean", getJsonSchemaType("boolean"))
    assertEquals("boolean", getJsonSchemaType("java.lang.Boolean"))
    assertEquals("number", getJsonSchemaType("double"))
    assertEquals("number", getJsonSchemaType("java.lang.Double"))
    assertEquals("number", getJsonSchemaType("float"))
    assertEquals("number", getJsonSchemaType("java.lang.Float"))
    assertEquals("array", getJsonSchemaType("java.util.List"))
    assertEquals("array", getJsonSchemaType("kotlin.collections.List"))
    assertEquals("array", getJsonSchemaType("java.util.Set"))
    assertEquals("array", getJsonSchemaType("kotlin.collections.Set"))
    assertEquals("object", getJsonSchemaType("java.util.Map"))
    assertEquals("object", getJsonSchemaType("kotlin.collections.Map"))
    assertEquals("string", getJsonSchemaType("some.unknown.Type"))
  }
}