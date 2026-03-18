package keywords

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import internal.GlobalVariable as GlobalVariable

public class WeatherApiHelper {

    /**
     * Parse JSON response body from a ResponseObject
     * @param response - the WS response object
     * @return parsed JSON object
     */
    @Keyword
    def static parseJson(def response) {
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(response.getResponseBodyContent())
    }

    /**
     * Assert HTTP status code
     * @param response - the WS response object
     * @param expectedCode - expected HTTP status code (e.g. 200)
     */
    @Keyword
    def static assertStatusCode(def response, int expectedCode) {
        int actualCode = response.getStatusCode()
        assert actualCode == expectedCode : 
            "Expected status code ${expectedCode} but got ${actualCode}"
        println("✅ Status Code: ${actualCode}")
    }

    /**
     * Assert response time is within acceptable limit
     * @param response - the WS response object
     * @param maxMs - maximum acceptable response time in milliseconds
     */
    @Keyword
    def static assertResponseTime(def response, long maxMs = 5000) {
        long elapsed = response.getElapsedTime()
        assert elapsed <= maxMs : 
            "Response time ${elapsed}ms exceeded limit of ${maxMs}ms"
        println("✅ Response Time: ${elapsed}ms (limit: ${maxMs}ms)")
    }

    /**
     * Assert that a JSON field exists and is not null
     * @param json - parsed JSON object
     * @param fieldPath - dot-separated field path e.g. "city.name"
     */
    @Keyword
    def static assertFieldExists(def json, String fieldPath) {
        def parts = fieldPath.split('\\.')
        def current = json
        for (String part : parts) {
            assert current != null : "Field '${fieldPath}' not found — null at '${part}'"
            if (current instanceof Map) {
                assert current.containsKey(part) : 
                    "Field '${fieldPath}' not found — missing key '${part}'"
                current = current[part]
            } else {
                throw new AssertionError("Field '${fieldPath}' not found — not a map at '${part}'")
            }
        }
        assert current != null : "Field '${fieldPath}' exists but is null"
        println("✅ Field exists: ${fieldPath} = ${current}")
        return current
    }

    /**
     * Assert field value is within numeric range
     * @param value - the numeric value
     * @param fieldName - name for logging
     * @param min - minimum acceptable value
     * @param max - maximum acceptable value
     */
    @Keyword
    def static assertInRange(def value, String fieldName, double min, double max) {
        double numVal = Double.parseDouble(value.toString())
        assert numVal >= min && numVal <= max : 
            "Field '${fieldName}' value ${numVal} out of range [${min}, ${max}]"
        println("✅ Range check: ${fieldName} = ${numVal} (range: ${min}–${max})")
    }

    /**
     * Assert field is of expected type
     * @param value - the value to check
     * @param fieldName - name for logging
     * @param expectedType - expected class type e.g. String, Integer, List
     */
    @Keyword
    def static assertType(def value, String fieldName, Class expectedType) {
        assert value instanceof expectedType : 
            "Field '${fieldName}' expected type ${expectedType.simpleName} but got ${value?.getClass()?.simpleName}"
        println("✅ Type check: ${fieldName} is ${expectedType.simpleName}")
    }

    /**
     * Assert Content-Type header contains expected value
     * @param response - the WS response object
     * @param expected - expected content type string (e.g. "application/json")
     */
    @Keyword
    def static assertContentType(def response, String expected = 'application/json') {
        String contentType = response.getHeaderFields()?.find { 
            it.getName()?.equalsIgnoreCase('Content-Type') 
        }?.getValue() ?: ''
        assert contentType.toLowerCase().contains(expected.toLowerCase()) : 
            "Content-Type '${contentType}' does not contain '${expected}'"
        println("✅ Content-Type: ${contentType}")
    }

    /**
     * Log response summary for debugging
     */
    @Keyword
    def static logResponse(def response, String testName) {
        println("=" * 60)
        println("TEST: ${testName}")
        println("Status: ${response.getStatusCode()}")
        println("Time:   ${response.getElapsedTime()}ms")
        println("Body (first 500 chars):")
        println(response.getResponseBodyContent()?.take(500))
        println("=" * 60)
    }

    /**
     * Validate AQI value is within OpenWeatherMap scale (1-5)
     */
    @Keyword
    def static assertValidAqi(def aqiValue) {
        int aqi = Integer.parseInt(aqiValue.toString())
        assert aqi >= 1 && aqi <= 5 : 
            "AQI value ${aqi} is outside valid range [1, 5]"
        def aqiLabels = [1: 'Good', 2: 'Fair', 3: 'Moderate', 4: 'Poor', 5: 'Very Poor']
        println("✅ AQI: ${aqi} (${aqiLabels[aqi]})")
    }

    /**
     * Validate Unix timestamp is recent (not too far in past or future)
     */
    @Keyword
    def static assertRecentTimestamp(def unixTs, String fieldName, int toleranceDays = 7) {
        long ts = Long.parseLong(unixTs.toString())
        long now = System.currentTimeMillis() / 1000
        long toleranceSec = toleranceDays * 86400
        assert Math.abs(now - ts) <= toleranceSec : 
            "Timestamp '${fieldName}' (${ts}) is more than ${toleranceDays} days from now"
        println("✅ Timestamp valid: ${fieldName}")
    }
}
