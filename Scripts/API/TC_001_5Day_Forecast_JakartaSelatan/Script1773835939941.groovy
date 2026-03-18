import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonSlurper
import internal.GlobalVariable as GlobalVariable

/**
 * ============================================================
 * TC_001 - Get 5 Day Weather Forecast Jakarta Selatan
 * API : GET /data/2.5/forecast
 * Free Tier: 1,000 calls/day — this test = 1 call
 * ============================================================
 */

// ── HELPER CLOSURES ──────────────────────────────────────────
def parseJson = { resp ->
    new JsonSlurper().parseText(resp.getResponseBodyContent())
}
def assertInRange = { value, name, double min, double max ->
    double v = Double.parseDouble(value.toString())
    assert v >= min && v <= max : "${name} = ${v} is out of range [${min}, ${max}]"
    println("✅ ${name} = ${v} (range ${min}–${max})")
}

// ── 1. BUILD REQUEST ─────────────────────────────────────────
String url = GlobalVariable.BASE_URL + '/forecast' +
    '?lat='    + GlobalVariable.LAT_JAKARTA_SELATAN +
    '&lon='    + GlobalVariable.LON_JAKARTA_SELATAN +
    '&appid='  + GlobalVariable.API_KEY +
    '&units='  + GlobalVariable.UNITS +
    '&lang=id' +
    '&cnt=40'

RequestObject req = new RequestObject('TC_001_Forecast')
req.setRestUrl(url)
req.setRestRequestMethod('GET')
req.setProperties([])

// ── 2. SEND REQUEST ──────────────────────────────────────────
println("=" * 60)
println("TC_001 - 5 Day Forecast Jakarta Selatan")
println("URL: " + url.replace(GlobalVariable.API_KEY, '***'))
def response = WS.sendRequest(req)
println("Status : ${response.getStatusCode()}")
println("Time   : ${response.getElapsedTime()}ms")
println("Body   : ${response.getResponseBodyContent()?.take(400)}")
println("=" * 60)

// ── 3. STATUS CODE ───────────────────────────────────────────
assert response.getStatusCode() == 200 :
    "Expected HTTP 200, got ${response.getStatusCode()}"
println("✅ Status Code: 200")

// ── 4. RESPONSE TIME ─────────────────────────────────────────
assert response.getElapsedTime() <= 5000 :
    "Response time ${response.getElapsedTime()}ms exceeded 5000ms"
println("✅ Response Time: ${response.getElapsedTime()}ms")

// ── 5. CONTENT-TYPE (safe HashMap access) ────────────────────
// getHeaderFields() returns Map<String,List<String>> in this Katalon version
def headers = response.getHeaderFields()
String contentType = ''
if (headers != null) {
    // key may be 'Content-Type' or 'content-type'
    def ctKey = headers.keySet().find { it?.toLowerCase() == 'content-type' }
    if (ctKey != null) {
        def ctVal = headers[ctKey]
        contentType = (ctVal instanceof List) ? ctVal[0] : ctVal.toString()
    }
}
assert contentType.toLowerCase().contains('application/json') :
    "Unexpected Content-Type: '${contentType}'"
println("✅ Content-Type: ${contentType}")

// ── 6. PARSE BODY ────────────────────────────────────────────
def json = parseJson(response)
assert json != null : "Response body is null or unparseable"

// ── 7. SCHEMA — TOP LEVEL ────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: Top-Level Fields ===")
['cod', 'cnt', 'list', 'city'].each { field ->
    assert json.containsKey(field) : "Missing top-level field: '${field}'"
    println("✅ '${field}' present")
}
assert json.cod.toString() == '200' : "Expected cod='200', got '${json.cod}'"
println("✅ cod = '200'")

// ── 8. SCHEMA — CITY ─────────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: City Object ===")
def city = json.city
assert city != null && city instanceof Map : "city is null or not an object"
['id', 'name', 'coord', 'country', 'population', 'timezone', 'sunrise', 'sunset'].each { f ->
    assert city.containsKey(f) : "city missing: '${f}'"
    println("✅ city.${f} present")
}
assert city.coord?.containsKey('lat') : "city.coord missing 'lat'"
assert city.coord?.containsKey('lon') : "city.coord missing 'lon'"
println("✅ city.coord lat & lon present")

// ── 9. SCHEMA — LIST ─────────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: Forecast List ===")
assert json.list instanceof List    : "list is not an array"
assert json.list.size() > 0         : "list is empty"
assert json.list.size() <= 40       : "list has more than 40 entries"
println("✅ list has ${json.list.size()} entries")

// ── 10. SCHEMA — LIST[0] ─────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: list[0] Fields ===")
def item0 = json.list[0]
['dt', 'main', 'weather', 'clouds', 'wind', 'visibility', 'pop', 'sys', 'dt_txt'].each { f ->
    assert item0.containsKey(f) : "list[0] missing: '${f}'"
    println("✅ list[0].${f} present")
}
['temp', 'feels_like', 'temp_min', 'temp_max',
 'pressure', 'sea_level', 'grnd_level', 'humidity', 'temp_kf'].each { f ->
    assert item0.main.containsKey(f) : "list[0].main missing: '${f}'"
    println("✅ list[0].main.${f} present")
}
assert item0.weather instanceof List && item0.weather.size() > 0
['id', 'main', 'description', 'icon'].each { f ->
    assert item0.weather[0].containsKey(f) : "weather[0] missing: '${f}'"
    println("✅ list[0].weather.${f} present")
}
['speed', 'deg'].each { f ->
    assert item0.wind.containsKey(f) : "wind missing: '${f}'"
    println("✅ list[0].wind.${f} present")
}

// ── 11. BODY VALUES — CITY ───────────────────────────────────
KeywordUtil.logInfo("=== BODY: City Values ===")
double expLat = Double.parseDouble(GlobalVariable.LAT_JAKARTA_SELATAN)
double expLon = Double.parseDouble(GlobalVariable.LON_JAKARTA_SELATAN)
assert Math.abs((city.coord.lat as Double) - expLat) <= 0.5 :
    "Lat mismatch: expected ~${expLat}, got ${city.coord.lat}"
assert Math.abs((city.coord.lon as Double) - expLon) <= 0.5 :
    "Lon mismatch: expected ~${expLon}, got ${city.coord.lon}"
println("✅ Coordinates near Jakarta Selatan (${city.coord.lat}, ${city.coord.lon})")

assert city.country == 'ID' : "Expected country='ID', got '${city.country}'"
println("✅ country = 'ID'")

assert city.timezone == 25200 : "Expected timezone=25200 (WIB/UTC+7), got ${city.timezone}"
println("✅ timezone = 25200 (WIB/UTC+7)")

assert (city.sunrise as Long) < (city.sunset as Long) :
    "sunrise (${city.sunrise}) should be before sunset (${city.sunset})"
println("✅ sunrise < sunset")

assert city.name?.toString()?.trim()?.length() > 0 : "city.name is empty"
println("✅ city.name = '${city.name}'")

// ── 12. BODY VALUES — EACH FORECAST ENTRY ────────────────────
KeywordUtil.logInfo("=== BODY: Validating All ${json.list.size()} Forecast Entries ===")
json.list.eachWithIndex { item, idx ->
    assert (item.dt as Long) > 0 : "list[${idx}].dt invalid"
    assert item.dt_txt?.toString() ==~ /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/ :
        "list[${idx}].dt_txt format invalid: '${item.dt_txt}'"

    assertInRange(item.main.temp,       "list[${idx}].temp",       15.0, 45.0)
    assertInRange(item.main.feels_like, "list[${idx}].feels_like", 10.0, 55.0)
    assertInRange(item.main.temp_min,   "list[${idx}].temp_min",   15.0, 45.0)
    assertInRange(item.main.temp_max,   "list[${idx}].temp_max",   15.0, 45.0)
    assert (item.main.temp_min as Double) <= (item.main.temp_max as Double) :
        "list[${idx}]: temp_min > temp_max"

    assertInRange(item.main.humidity, "list[${idx}].humidity", 0.0, 100.0)
    assertInRange(item.main.pressure, "list[${idx}].pressure", 900.0, 1100.0)
    assert (item.wind.speed as Double) >= 0.0 : "list[${idx}].wind.speed is negative"
    assertInRange(item.wind.deg,      "list[${idx}].wind.deg", 0.0, 360.0)
    assertInRange(item.pop,           "list[${idx}].pop",      0.0, 1.0)
    assertInRange(item.clouds.all,    "list[${idx}].clouds",   0.0, 100.0)

    int vis = item.visibility as Integer
    assert vis >= 0 && vis <= 10000 : "list[${idx}].visibility out of range: ${vis}"

    assert item.weather[0].icon?.toString() ==~ /\d{2}[dn]/ :
        "list[${idx}].weather.icon invalid: '${item.weather[0].icon}'"
    assert item.sys.pod in ['d', 'n'] :
        "list[${idx}].sys.pod invalid: '${item.sys.pod}'"
}
println("✅ All ${json.list.size()} forecast entries passed")

// ── 13. BUSINESS RULES ───────────────────────────────────────
KeywordUtil.logInfo("=== BUSINESS RULES ===")
long firstDt  = json.list.first().dt as Long
long lastDt   = json.list.last().dt  as Long
long spanDays = (lastDt - firstDt) / 86400
assert spanDays >= 4 : "Forecast spans only ${spanDays} days, expected >= 4"
println("✅ Forecast spans ${spanDays} days")

for (int i = 1; i < Math.min(json.list.size(), 5); i++) {
    long diffH = ((json.list[i].dt as Long) - (json.list[i-1].dt as Long)) / 3600
    assert diffH == 3 : "Timestamps [${i-1}]→[${i}] not 3h apart: ${diffH}h"
}
println("✅ Timestamps are 3h apart (verified first 5)")

KeywordUtil.logInfo("✅ TC_001 PASSED — 5 Day Forecast Jakarta Selatan")
