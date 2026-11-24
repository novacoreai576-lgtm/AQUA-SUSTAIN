package com.example

import com.example.data.model.AquaSustainData
import com.example.data.model.SoilStatus
import com.example.data.model.TankLevelCategory
import com.example.data.model.WaterQualityInput
import com.example.data.model.WaterUsageRecord
import com.example.data.model.WaterUsageSummary
import com.example.data.repository.AquaSustainRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testIrrigationLogicRule1_RainDetected_PumpOff() {
        // 1. Rain detected -> Pump OFF
        val data = AquaSustainData(
            rain = true,
            rainStatus = "RAIN DETECTED",
            soilMoisture = 35,
            waterLevel = 72,
            pump = false,
            pumpStatus = "OFF",
            systemStatus = "RAIN DETECTED - IRRIGATION OFF"
        )
        assertTrue(data.rain)
        assertEquals(0, data.activeRuleIndex)
        assertEquals("PUMP OFF", data.pumpStatusText)
        assertEquals("RAIN DETECTED", data.rainStatusText)
        assertEquals("RAIN DETECTED - IRRIGATION OFF", data.activeRuleDescription)
    }

    @Test
    fun testIrrigationLogicRule2_LowWater_PumpOff() {
        // Low water protection: water level < 20% -> Pump OFF
        val data = AquaSustainData(
            rain = false,
            rainStatus = "NO RAIN",
            soilMoisture = 35,
            waterLevel = 15,
            pump = false,
            pumpStatus = "OFF"
        )
        assertFalse(data.rain)
        assertTrue(data.isWaterLow)
        assertEquals(1, data.activeRuleIndex)
        assertEquals(false, data.targetAutoPumpState)
    }

    @Test
    fun testIrrigationLogicRule3_NoRain_SoilUnder50_PumpOn() {
        // No rain + water level >= 20% + soil moisture < 50% -> Pump ON
        val data = AquaSustainData(
            rain = false,
            rainStatus = "NO RAIN",
            soilMoisture = 35,
            waterLevel = 72,
            pump = true,
            pumpStatus = "ON",
            systemStatus = "NO RAIN + SOIL < 50% - IRRIGATION ON"
        )
        assertFalse(data.rain)
        assertTrue(data.isSoilDry) // 35 < 50
        assertEquals(SoilStatus.DRY, data.soilStatus)
        assertEquals(2, data.activeRuleIndex)
        assertEquals("PUMP ON", data.pumpStatusText)
        assertEquals("NO RAIN", data.rainStatusText)
        assertEquals("Irrigation Active", data.irrigationSubtext)
        assertEquals("NO RAIN + SOIL < 50% - IRRIGATION ON", data.activeRuleDescription)
        assertEquals(true, data.targetAutoPumpState)
    }

    @Test
    fun testIrrigationLogicRule4_NoRain_SoilOver50_PumpOff() {
        // No rain + soil moisture >= 50% -> Pump OFF
        val data = AquaSustainData(
            rain = false,
            rainStatus = "NO RAIN",
            soilMoisture = 65,
            waterLevel = 72,
            pump = false,
            pumpStatus = "OFF",
            systemStatus = "NO RAIN + SOIL >= 50% - IRRIGATION OFF"
        )
        assertFalse(data.rain)
        assertFalse(data.isSoilDry) // 65 >= 50
        assertEquals(SoilStatus.WET, data.soilStatus)
        assertEquals(3, data.activeRuleIndex)
        assertEquals("PUMP OFF", data.pumpStatusText)
        assertEquals("NO RAIN + SOIL >= 50% - IRRIGATION OFF", data.activeRuleDescription)
        assertEquals(false, data.targetAutoPumpState)
    }

    @Test
    fun testResolvedSystemStatus_DefaultFallback() {
        val dataDry = AquaSustainData(rain = false, soilMoisture = 30, waterLevel = 70, pump = true, systemStatus = null)
        assertEquals("NO RAIN + SOIL < 50% - IRRIGATION ON", dataDry.resolvedSystemStatus)

        val dataRain = AquaSustainData(rain = true, soilMoisture = 30, waterLevel = 70, pump = false, systemStatus = null)
        assertEquals("RAIN DETECTED - IRRIGATION OFF", dataRain.resolvedSystemStatus)

        val dataWet = AquaSustainData(rain = false, soilMoisture = 60, waterLevel = 70, pump = false, systemStatus = null)
        assertEquals("NO RAIN + SOIL >= 50% - IRRIGATION OFF", dataWet.resolvedSystemStatus)
    }

    @Test
    fun testUndergroundTankThresholds() {
        val lowTank = AquaSustainData(waterLevel = 18)
        assertEquals(TankLevelCategory.LOW, lowTank.tankLevelCategory)

        val normalTank = AquaSustainData(waterLevel = 50)
        assertEquals(TankLevelCategory.NORMAL, normalTank.tankLevelCategory)

        val highTank = AquaSustainData(waterLevel = 88)
        assertEquals(TankLevelCategory.HIGH, highTank.tankLevelCategory)
    }

    @Test
    fun testWeatherInfoAndGeoLocation() {
        val location = com.example.data.model.GeoLocation(
            name = "Ludhiana",
            country = "India",
            admin1 = "Punjab",
            latitude = 30.9010,
            longitude = 75.8573
        )
        assertEquals("Ludhiana, Punjab, India", location.displayName)

        val weather = com.example.data.model.WeatherInfo(
            temperatureC = 31,
            condition = "Clear Sky",
            rainProbabilityPercent = 15,
            rainPredictionText = "Low precipitation probability",
            smartRecommendation = "Optimal conditions for automated irrigation.",
            humidityPercent = 55,
            feelsLikeC = 32,
            windSpeedKmh = 14.2,
            precipitationMm = 0.0,
            locationName = location.displayName,
            latitude = location.latitude,
            longitude = location.longitude,
            isLive = true
        )

        assertEquals(31, weather.temperatureC)
        assertEquals(32, weather.feelsLikeC)
        assertEquals("Clear Sky", weather.condition)
        assertEquals(15, weather.rainProbabilityPercent)
        assertEquals(55, weather.humidityPercent)
        assertEquals("Ludhiana, Punjab, India", weather.locationName)
        assertTrue(weather.isLive)
    }

    @Test
    fun testNoDemoMode_DisconnectedSensorDefaults() {
        val disconnected = AquaSustainRepository.disconnectedSensorState()
        assertFalse(disconnected.hasLiveData)
        assertEquals(0, disconnected.soilMoisture)
        assertEquals(0, disconnected.waterLevel)
        assertFalse(disconnected.pump)
        assertFalse(disconnected.rain)
        assertEquals("Device Not Connected.", disconnected.systemStatus)
    }

    @Test
    fun testWaterQualityInput_Validation() {
        assertFalse(WaterQualityInput(sourceName = "Tank").hasAnyMeasurement())
        assertTrue(WaterQualityInput(sourceName = "Tank", ph = 7.2).hasAnyMeasurement())
        assertTrue(WaterQualityInput(sourceName = "Well", tdsPpm = 220.0).hasAnyMeasurement())
    }

    @Test
    fun testWaterUsageSummary_RealAggregationAndEmptyState() {
        val empty = WaterUsageSummary.compute(emptyList())
        assertEquals(0, empty.totalRecords)
        assertEquals(0.0, empty.todayLiters, 0.001)
        assertEquals(0.0, empty.weeklyLiters, 0.001)
        assertTrue(empty.wastageAlerts.isEmpty())

        val now = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val records = listOf(
            WaterUsageRecord(id = "1", amountLiters = 400.0, category = "Irrigation", usageDateEpochMs = now),
            WaterUsageRecord(id = "2", amountLiters = 100.0, category = "Household", usageDateEpochMs = now - dayMs),
            WaterUsageRecord(id = "3", amountLiters = 120.0, category = "Household", usageDateEpochMs = now - 2 * dayMs)
        )
        val summary = WaterUsageSummary.compute(records, nowMs = now)
        assertEquals(3, summary.totalRecords)
        assertEquals(400.0, summary.todayLiters, 0.001)
        assertEquals(100.0, summary.yesterdayLiters, 0.001)
        assertEquals(620.0, summary.weeklyLiters, 0.001)
        assertEquals(620.0, summary.monthlyLiters, 0.001)
        assertEquals(300.0, summary.dailyDifferenceLiters, 0.001)
        assertEquals(7, summary.dailySeries.size)
        assertTrue(summary.wastageAlerts.isNotEmpty())
        assertEquals("Irrigation", summary.categoryBreakdown.first().category)
    }

    @Test
    fun testSidebarDestinations_ExactEightAquaSustainItems() {
        val titles = AppDestination.entries.map { it.title }
        assertEquals(
            listOf(
                "Home",
                "Water Dashboard",
                "Water Quality Analysis",
                "Water Usage",
                "AI Water Assistant",
                "History",
                "Reports",
                "Settings"
            ),
            titles
        )
    }
}
