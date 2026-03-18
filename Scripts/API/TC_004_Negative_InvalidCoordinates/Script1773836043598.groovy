import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonSlurper
import internal.GlobalVariable as GlobalVariable

/**
 * ============================================================
 * TC_004 - Negative: Missing & Out-of-Range Coordinates
 * Free Tier: this test = 4 calls
 * ============================================================
 */

def parseJson = { resp ->
    try { new JsonSlurper().parseText(resp.getResponseBodyContent()) }
    catch (Exception e) { null }
}

def testScenarios = [
    [desc: 'Missing lat',      url: GlobalVariable.BASE_URL + '/forecast?lon=106.8106&appid=' + GlobalVariable.API_KEY],
    [desc: 'Missing lon',      url: GlobalVariable.BASE_URL + '/forecast?lat=-6.2615&appid='  + GlobalVariable.API_KEY],
    [desc: 'Lat out of range', url: GlobalVariable.BASE_URL + '/forecast?lat=999&lon=106.8106&appid=' + GlobalVariable.API_KEY],
    [desc: 'Non-numeric lat',  url: GlobalVariable.BASE_URL + '/forecast?lat=abc&lon=106.8106&appid=' + GlobalVariable.API_KEY],
]

testScenarios.each { scenario ->
    KeywordUtil.logInfo("=== Negative: ${scenario.desc} ===")

    RequestObject req = new RequestObject("TC_004_${scenario.desc}")
    req.setRestUrl(scenario.url)
    req.setRestRequestMethod('GET')
    req.setProperties([])

    def response = WS.sendRequest(req)
    int status = response.getStatusCode()
    println("${scenario.desc} → Status: ${status}")
    println("Body: ${response.getResponseBodyContent()?.take(200)}")

    assert status == 400 || status == 401 :
        "${scenario.desc}: Expected 400 or 401, got ${status}"
    println("✅ ${scenario.desc}: Status = ${status}")

    def json = parseJson(response)
    if (json != null && json instanceof Map) {
        assert json.containsKey('cod') : "${scenario.desc}: error body missing 'cod'"
        println("✅ ${scenario.desc}: cod='${json.cod}' message='${json.message}'")
    } else {
        println("⚠️ ${scenario.desc}: non-JSON response body (status=${status})")
    }
}

KeywordUtil.logInfo("✅ TC_004 PASSED — Negative: Invalid/Missing Coordinates")
