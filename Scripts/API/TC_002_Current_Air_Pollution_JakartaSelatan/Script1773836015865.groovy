import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonSlurper
import internal.GlobalVariable as GlobalVariable

/**
 * ============================================================
 * TC_002 - Get Current Air Pollution Jakarta Selatan
 * API : GET /data/2.5/air_pollution
 * Free Tier: 1,000 calls/day — this test = 1 call
 * ============================================================
 */

def parseJson = { resp ->
    new JsonSlurper().parseText(resp.getResponseBodyContent())
}
def assertInRange = { value, name, double min, double max ->
    double v = Double.parseDouble(value.toString())
    assert v >= min && v <= max : "${name} = ${v} is out of range [${min}, ${max}]"
    println("✅ ${name} = ${v} (range ${min}–${max})")
}

// ── 1. BUILD REQUEST ─────────────────────────────────────────
String url = GlobalVariable.BASE_URL + '/air_pollution' +
    '?lat='   + GlobalVariable.LAT_JAKARTA_SELATAN +
    '&lon='   + GlobalVariable.LON_JAKARTA_SELATAN +
    '&appid=' + GlobalVariable.API_KEY

RequestObject req = new RequestObject('TC_002_AirPollution')
req.setRestUrl(url)
req.setRestRequestMethod('GET')
req.setProperties([])

// ── 2. SEND REQUEST ──────────────────────────────────────────
println("=" * 60)
println("TC_002 - Current Air Pollution Jakarta Selatan")
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
def headers = response.getHeaderFields()
String contentType = ''
if (headers != null) {
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
assert json.containsKey('coord') : "Missing top-level field: 'coord'"
assert json.containsKey('list')  : "Missing top-level field: 'list'"
println("✅ Top-level fields: coord, list")

// ── 8. SCHEMA — COORD ────────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: coord ===")
def coord = json.coord
assert coord instanceof Map     : "coord is not an object"
assert coord.containsKey('lat') : "coord missing 'lat'"
assert coord.containsKey('lon') : "coord missing 'lon'"
println("✅ coord: lat & lon present")

// ── 9. SCHEMA — LIST ─────────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: list ===")
assert json.list instanceof List : "list is not an array"
assert json.list.size() > 0     : "list is empty — no data returned"
println("✅ list has ${json.list.size()} entry(ies)")

// ── 10. SCHEMA — LIST[0] ─────────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: list[0] Fields ===")
def item = json.list[0]
assert item.containsKey('dt')         : "list[0] missing: 'dt'"
assert item.containsKey('main')       : "list[0] missing: 'main'"
assert item.containsKey('components') : "list[0] missing: 'components'"
println("✅ list[0]: dt, main, components present")

assert item.main instanceof Map     : "list[0].main is not an object"
assert item.main.containsKey('aqi') : "list[0].main missing 'aqi'"
println("✅ list[0].main.aqi present")

// ── 11. SCHEMA — COMPONENTS ──────────────────────────────────
KeywordUtil.logInfo("=== SCHEMA: Pollutant Components ===")
def components = item.components
assert components instanceof Map : "components is not an object"
def requiredPollutants = ['co', 'no', 'no2', 'o3', 'so2', 'pm2_5', 'pm10', 'nh3']
requiredPollutants.each { p ->
    assert components.containsKey(p) : "components missing: '${p}'"
    println("✅ components.${p} present")
}

// ── 12. BODY VALUES — COORDINATES ────────────────────────────
KeywordUtil.logInfo("=== BODY: Coordinate Values ===")
double expLat = Double.parseDouble(GlobalVariable.LAT_JAKARTA_SELATAN)
double expLon = Double.parseDouble(GlobalVariable.LON_JAKARTA_SELATAN)
assert Math.abs((coord.lat as Double) - expLat) <= 0.5 :
    "Lat mismatch: expected ~${expLat}, got ${coord.lat}"
assert Math.abs((coord.lon as Double) - expLon) <= 0.5 :
    "Lon mismatch: expected ~${expLon}, got ${coord.lon}"
println("✅ Coordinates near Jakarta Selatan (${coord.lat}, ${coord.lon})")

// ── 13. BODY VALUES — AQI & POLLUTANTS ───────────────────────
KeywordUtil.logInfo("=== BODY: AQI & Pollutant Values ===")
def aqiLabel = [1: 'Good', 2: 'Fair', 3: 'Moderate', 4: 'Poor', 5: 'Very Poor']

json.list.eachWithIndex { entry, idx ->
    // AQI 1–5
    int aqi = Integer.parseInt(entry.main.aqi.toString())
    assert aqi >= 1 && aqi <= 5 : "list[${idx}].aqi = ${aqi} out of range [1,5]"
    println("✅ list[${idx}].aqi = ${aqi} (${aqiLabel[aqi]})")

    // dt: positive, within last 24h
    long ts  = entry.dt as Long
    long now = System.currentTimeMillis() / 1000
    assert ts > 0 : "list[${idx}].dt is not positive: ${ts}"
    assert Math.abs(now - ts) <= 86400 : "list[${idx}].dt more than 24h from now"
    println("✅ list[${idx}].dt is recent")

    def comp = entry.components
    requiredPollutants.each { p ->
        assert (comp[p] as Double) >= 0.0 : "list[${idx}].${p} is negative: ${comp[p]}"
    }
    println("✅ list[${idx}] all pollutants >= 0")

    assertInRange(comp.co,    "list[${idx}].co",    0.0, 50000.0)
    assertInRange(comp.no,    "list[${idx}].no",    0.0,  1000.0)
    assertInRange(comp.no2,   "list[${idx}].no2",   0.0,  1000.0)
    assertInRange(comp.o3,    "list[${idx}].o3",    0.0,  1000.0)
    assertInRange(comp.so2,   "list[${idx}].so2",   0.0,  5000.0)
    assertInRange(comp.pm2_5, "list[${idx}].pm2_5", 0.0,  1000.0)
    assertInRange(comp.pm10,  "list[${idx}].pm10",  0.0,  1000.0)
    assertInRange(comp.nh3,   "list[${idx}].nh3",   0.0,   500.0)

    println("✅ list[${idx}] CO=${comp.co} NO2=${comp.no2} PM2.5=${comp.pm2_5} PM10=${comp.pm10}")
}

// ── 14. BUSINESS RULE — AQI vs PM2.5 ────────────────────────
KeywordUtil.logInfo("=== BUSINESS RULE: AQI vs PM2.5 Consistency ===")
int    currentAqi  = json.list[0].main.aqi         as Integer
double currentPm25 = json.list[0].components.pm2_5 as Double

if (currentAqi == 1) {
    assert currentPm25 < 10.0 :
        "AQI=1 (Good) inconsistent with PM2.5=${currentPm25} >= 10 μg/m³"
} else if (currentAqi == 5) {
    assert currentPm25 >= 75.0 :
        "AQI=5 (Very Poor) inconsistent with PM2.5=${currentPm25} < 75 μg/m³"
}
println("✅ AQI=${currentAqi} consistent with PM2.5=${currentPm25} μg/m³")

// ── 15. SUMMARY ──────────────────────────────────────────────
println("=" * 60)
println("📊 Air Quality — Jakarta Selatan")
println("   AQI   : ${currentAqi} (${aqiLabel[currentAqi]})")
println("   CO    : ${json.list[0].components.co} μg/m³")
println("   NO2   : ${json.list[0].components.no2} μg/m³")
println("   O3    : ${json.list[0].components.o3} μg/m³")
println("   SO2   : ${json.list[0].components.so2} μg/m³")
println("   PM2.5 : ${currentPm25} μg/m³")
println("   PM10  : ${json.list[0].components.pm10} μg/m³")
println("   NH3   : ${json.list[0].components.nh3} μg/m³")
println("=" * 60)

KeywordUtil.logInfo("✅ TC_002 PASSED — Current Air Pollution Jakarta Selatan")
