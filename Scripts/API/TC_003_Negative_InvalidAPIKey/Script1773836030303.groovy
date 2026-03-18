import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonSlurper
import internal.GlobalVariable as GlobalVariable

/**
 * ============================================================
 * TC_003 - Negative: Invalid API Key
 * Free Tier: this test = 2 calls
 * ============================================================
 */

def parseJson = { resp ->
    try { new JsonSlurper().parseText(resp.getResponseBodyContent()) }
    catch (Exception e) { null }
}
def getContentType = { resp ->
    def hdrs = resp.getHeaderFields()
    if (hdrs == null) return ''
    def key = hdrs.keySet().find { it?.toLowerCase() == 'content-type' }
    if (key == null) return ''
    def val = hdrs[key]
    return (val instanceof List) ? val[0] : val.toString()
}

def endpoints = [
    [name: 'Forecast',      path: '/forecast'],
    [name: 'Air Pollution', path: '/air_pollution'],
]

endpoints.each { ep ->
    KeywordUtil.logInfo("=== Negative: ${ep.name} — Invalid API Key ===")

    String url = GlobalVariable.BASE_URL + ep.path +
        '?lat='   + GlobalVariable.LAT_JAKARTA_SELATAN +
        '&lon='   + GlobalVariable.LON_JAKARTA_SELATAN +
        '&appid=INVALID_KEY_00000000000'

    RequestObject req = new RequestObject("TC_003_${ep.name}")
    req.setRestUrl(url)
    req.setRestRequestMethod('GET')
    req.setProperties([])

    def response = WS.sendRequest(req)
    println("${ep.name} → Status: ${response.getStatusCode()}")
    println("Body: ${response.getResponseBodyContent()?.take(200)}")

    assert response.getStatusCode() == 401 :
        "${ep.name}: Expected 401, got ${response.getStatusCode()}"
    println("✅ ${ep.name}: Status = 401")

    String ct = getContentType(response)
    assert ct.toLowerCase().contains('application/json') :
        "${ep.name}: Unexpected Content-Type: '${ct}'"
    println("✅ ${ep.name}: Content-Type = application/json")

    def json = parseJson(response)
    assert json != null : "${ep.name}: could not parse error body"
    assert json.containsKey('cod')     : "${ep.name}: error body missing 'cod'"
    assert json.containsKey('message') : "${ep.name}: error body missing 'message'"
    assert json.cod.toString() == '401' :
        "${ep.name}: expected cod='401', got '${json.cod}'"
    assert json.message?.toString()?.trim()?.length() > 0 :
        "${ep.name}: error message is empty"
    println("✅ ${ep.name}: cod='401' message='${json.message}'")
}

KeywordUtil.logInfo("✅ TC_003 PASSED — Negative: Invalid API Key")
